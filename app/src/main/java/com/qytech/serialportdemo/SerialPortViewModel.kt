package com.qytech.serialportdemo

import android.content.Context
import android_serialport_api.SerialPort
import android_serialport_api.SerialPortFinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qytech.serialportbase.extensions.asHexLower
import com.qytech.serialportbase.extensions.hexAsByteArray
import com.qytech.serialportdemo.model.CarLED
import com.qytech.serialportbase.extensions.toByteArray
import com.qytech.serialportbase.extensions.toUInt32ByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.collections.ArrayList

class SerialPortViewModel : ViewModel() {
    companion object {
        const val DEVICE_TTYS4 = "/dev/ttyS4"
        const val DEVICE_TTYS3 = "/dev/ttyS3"
    }

    private val headerArray = ByteArray(5).apply {
        this[0] = 0x4C
        this[1] = 0x45
        this[2] = 0x44
    }

    private var serialPortList: MutableList<SerialPort> = ArrayList()


    private val devicesList = SerialPortFinder().allDevicesPath

    private val _readStatusS4 = MutableStateFlow(CarLED.Status.CLOSE)
    val readStatusS4: StateFlow<CarLED.Status> = _readStatusS4

    private val _readStatusS3 = MutableStateFlow(CarLED.Status.CLOSE)
    val readStatusS3: StateFlow<CarLED.Status> = _readStatusS3

    var isMarquee = false

    init {
        if (devicesList.isNotEmpty()) {
            selectDevice(DEVICE_TTYS4)
            selectDevice(DEVICE_TTYS3)
        }
    }

    private fun selectDevice(path: String?, baudRate: Int = 115200, flags: Int = 0) {
        if (path.isNullOrEmpty() || path !in devicesList) {
            return
        }
        runCatching {
            val serialPort = SerialPort(File(path), baudRate, flags)
            serialPortList.add(serialPort)
            readSerialPort(serialPort, path)
        }
    }


    private fun getCommandBytes(command: Byte, dataLength: Byte, vararg data: Byte): ByteArray {
        headerArray[CarLED.COMMAND_BYTE_INDEX] = command
        headerArray[CarLED.DATA_LENGTH_INDEX] = dataLength
        return headerArray.plus(data).let { bytearray ->
            bytearray.plus(bytearray.sum().toByte()).apply {
                Timber.d("send command ${this.asHexLower}")
            }
        }
    }


    private fun getResponseResult(byteArray: ByteArray): ByteArray {
        val dataLength = byteArray[CarLED.DATA_LENGTH_INDEX]
        val buffer = byteArray.copyOfRange(0, CarLED.DATA_START_INDEX + dataLength + 1)
        return buffer.copyOfRange(CarLED.DATA_START_INDEX, CarLED.DATA_START_INDEX + dataLength)
            .apply {
                Timber.d("result ${buffer.asHexLower} data ${this.asHexLower}")
            }
    }

    private fun writeCommands(commands: ByteArray) {
        serialPortList.forEach { it.outputStream.write(commands) }
    }

    fun writeCarStatus(status: CarLED.Status) {
        val statusBytes = getCommandBytes(
            CarLED.COMMAND_BYTE_WRITE_STATUS,
            0x01,
            status.value
        )
        writeCommands(statusBytes)
    }

    fun readCarStatus() {
        val statusBytes = getCommandBytes(CarLED.COMMAND_BYTE_READ_STATUS, 0x00)
        writeCommands(statusBytes)
    }


    fun writeCharset(status: CarLED.Status) {
        val charsetBytes = getCommandBytes(
            CarLED.COMMAND_BYTE_WRITE_CHARSET,
            0x26,/* 38*/
            status.value,
            status.charsetColor,
            *status.charset.hexAsByteArray /* 36*/
        )
        writeCommands(charsetBytes)
    }


    @Suppress("unused")
    fun readCharset(status: CarLED.Status) {
        val charsetBytes = getCommandBytes(CarLED.COMMAND_BYTE_READ_CHARSET, 0x01, status.value)
        writeCommands(charsetBytes)
    }

    fun startMarquee() {
        isMarquee = true
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive && isMarquee) {
                CarLED.Status.values().forEach { status ->
                    if (status != CarLED.Status.CLOSE && isMarquee) {
                        writeCarStatus(status)
                        delay(5000L)
                    }
                }
            }
        }
    }

    fun showCustomMessage() {
        val bytes = arrayOf(
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x02, 0x00,
            0x00, 0xc0, 0x00, 0x00, 0x06, 0x00, 0x00, 0xc0,
            0x00, 0x00, 0x06, 0x00, 0x03, 0xf8, 0x3c, 0x1f,
            0x1f, 0xc0, 0x00, 0xc0, 0x66, 0x31, 0x06, 0x00,
            0x00, 0xc0, 0xc2, 0x20, 0x06, 0x00, 0x00, 0xc0,
            0xc3, 0x30, 0x06, 0x00, 0x00, 0xc0, 0xff, 0x1e,
            0x06, 0x00, 0x00, 0xc0, 0x80, 0x03, 0x06, 0x00,
            0x00, 0xc0, 0xc0, 0x01, 0x06, 0x00, 0x00, 0x40,
            0x62, 0x23, 0x02, 0x00, 0x00, 0x78, 0x3e, 0x3e,
            0x03, 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        ).toByteArray()


        val loaderCommands = getCommandBytes(
            CarLED.COMMAND_BYTE_SHOW,
            0xC1.toByte(),
            CarLED.COLOR_WHITE,
            *bytes
        )
        writeCommands(loaderCommands)

    }

    fun updateFirmware(context: Context) {
        val firmware = File(context.filesDir.path, "ledMatrixApp.bin")
        if (!firmware.exists() || !firmware.canRead()) {
            return
        }
        val firmwareData = firmware.readBytes()
        val firmwareLength = firmwareData.size.toUInt32ByteArray()
        val sum = firmwareData.sum().toUInt32ByteArray()
        Timber.d("firmware size is ${firmwareLength.asHexLower}")
        Timber.d("firmware check sum is ${sum.asHexLower}")
        val loaderCommands = getCommandBytes(
            CarLED.COMMAND_BYTE_LOADER,
            0x08,
            *firmwareLength,
            *sum,
        )
        writeCommands(loaderCommands)
        var downloadCommands: ByteArray
        firmwareData.asSequence().chunked(0x80).forEachIndexed { index, list ->
            Timber.d("write data $index ${list.size}")
            downloadCommands = getCommandBytes(
                CarLED.COMMAND_BYTE_DOWNLOAD_FIRMWARE, //命令字 0x12
                (list.size + 5).toByte(), //数据区长度 5+N
                *(index * 0x80).toUInt32ByteArray(),// 文件偏移 4 字节
                list.size.toByte(),//数据块长度 固定 128 字节
                *list.toByteArray()//数据块
            )
            writeCommands(downloadCommands)
        }
    }


    fun stopMarquee() {
        isMarquee = false
    }

    private fun readSerialPort(serialPort: SerialPort, path: String) {
        val buffer = ByteArray(32) { 0x00 }
        var status: CarLED.Status
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                while (isActive) {
                    serialPort.inputStream.read(buffer)
                    val result = getResponseResult(buffer)
                    if (buffer[CarLED.COMMAND_BYTE_INDEX] == CarLED.RESULT_BYTE_READ_STATUS) {
                        status = when (result[0]) {
                            CarLED.Status.SUBSCRIBE.value -> CarLED.Status.SUBSCRIBE
                            CarLED.Status.EMPTY.value -> CarLED.Status.EMPTY
                            CarLED.Status.RUNNING.value -> CarLED.Status.RUNNING
                            CarLED.Status.REST.value -> CarLED.Status.REST
                            else -> CarLED.Status.CLOSE
                        }
                        when (path) {
                            DEVICE_TTYS3 -> {
                                _readStatusS3.emit(status)
                            }
                            DEVICE_TTYS4 -> {
                                _readStatusS4.emit(status)
                            }
                        }
                    }

                    delay(1000L)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        serialPortList.forEach { it.close() }
    }
}


