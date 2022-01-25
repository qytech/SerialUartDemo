package com.qytech.serialportdemo

import android_serialport_api.SerialPort
import android_serialport_api.SerialPortFinder
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qytech.serialportdemo.model.CarLED
import com.qytech.serialportdemo.utils.toHexByteArray
import com.qytech.serialportdemo.utils.toHexString
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

    private val byteArray = ByteArray(128).apply {
        this[0] = 0x4C
        this[1] = 0x45
        this[2] = 0x44
    }

    private var serialPortList: MutableList<SerialPort> = ArrayList()


    private val devicesList = SerialPortFinder().allDevicesPath

    private val _readStatusS4 = MutableStateFlow(CarLED.Status.NULL)
    val readStatusS4: StateFlow<CarLED.Status> = _readStatusS4

    private val _readStatusS3 = MutableStateFlow(CarLED.Status.NULL)
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
        byteArray[CarLED.COMMAND_BYTE_INDEX] = command
        byteArray[CarLED.DATA_LENGTH_INDEX] = dataLength
        data.forEachIndexed { index, byte ->
            byteArray[CarLED.DATA_START_INDEX + index] = byte
        }
        val checkIndex = CarLED.DATA_START_INDEX + dataLength
        byteArray[checkIndex] =
            byteArray.filterIndexed { index, _ -> index < checkIndex }.sum().toByte()
        return byteArray.copyOfRange(0, checkIndex + 1).apply {
            Timber.d("command ${this.toHexString()}")
        }
    }


    private fun getResponseResult(byteArray: ByteArray): ByteArray {
        val dataLength = byteArray[CarLED.DATA_LENGTH_INDEX]
        val buffer = byteArray.copyOfRange(0, CarLED.DATA_START_INDEX + dataLength + 1)
        return buffer.copyOfRange(CarLED.DATA_START_INDEX, CarLED.DATA_START_INDEX + dataLength)
            .apply {
                Timber.d("result ${buffer.toHexString()} data ${this.toHexString()}")
            }
    }

    fun writeCarStatus(status: CarLED.Status) {
        val statusBytes = getCommandBytes(
            CarLED.COMMAND_BYTE_WRITE_STATUS,
            0x01,
            status.value
        )
        serialPortList.forEach { it.outputStream.write(statusBytes) }
    }

    fun readCarStatus() {
        val statusBytes = getCommandBytes(CarLED.COMMAND_BYTE_READ_STATUS, 0x00)
        serialPortList.forEach { it.outputStream.write(statusBytes) }
    }


    fun writeCharset(status: CarLED.Status) {
        val charsetBytes = getCommandBytes(
            CarLED.COMMAND_BYTE_WRITE_CHARSET,
            0x26,/* 38*/
            status.value,
            status.charsetColor,
            *status.charset.toHexByteArray() /* 36*/
        )
        serialPortList.forEach { it.outputStream.write(charsetBytes) }
    }


    fun readCharset(status: CarLED.Status) {
        val charsetBytes = getCommandBytes(CarLED.COMMAND_BYTE_READ_CHARSET, 0x01, status.value)
        serialPortList.forEach { it.outputStream.write(charsetBytes) }
    }

    fun startMarquee() {
        isMarquee = true
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive && isMarquee) {
                CarLED.Status.values().forEach { status ->
                    if (status != CarLED.Status.NULL && isMarquee) {
                        writeCarStatus(status)
                        delay(5000L)
                    }
                }
            }
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
                    if (buffer[CarLED.COMMAND_BYTE_INDEX] == CarLED.RESULT_BYTE_READ_STATUS) {
                        status = when (getResponseResult(buffer)[0]) {
                            CarLED.Status.SUBSCRIBE.value -> CarLED.Status.SUBSCRIBE
                            CarLED.Status.EMPTY.value -> CarLED.Status.EMPTY
                            CarLED.Status.RUNNING.value -> CarLED.Status.RUNNING
                            CarLED.Status.REST.value -> CarLED.Status.REST
                            else -> CarLED.Status.NULL
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


