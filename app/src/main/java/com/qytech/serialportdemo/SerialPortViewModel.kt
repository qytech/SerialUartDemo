package com.qytech.serialportdemo

import android_serialport_api.SerialPort
import android_serialport_api.SerialPortFinder
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.collections.ArrayList

enum class CarStatus(val value: String, val message: String, val code: String, val color: Color) {
    SUBSCRIBE("LED0", "subscribe", "S0", Color.Blue),
    EMPTY("LED1", "empty", "S1", Color.Red),
    RUNNING("LED2", "running", "S2", Color.Yellow),
    REST("LED3", "rest", "S3", Color.Green),
    NULL("", "", "", Color.Black),
}

class SerialPortViewModel : ViewModel() {
    companion object {
        const val DEVICE_TTYS4 = "/dev/ttyS4"
        const val DEVICE_TTYS3 = "/dev/ttyS3"
    }

    private var serialPortList: MutableList<SerialPort> = ArrayList()


    private val devicesList = SerialPortFinder().allDevicesPath

    private val _readStatusS4 = MutableStateFlow(CarStatus.NULL)
    val readStatusS4: StateFlow<CarStatus> = _readStatusS4

    private val _readStatusS3 = MutableStateFlow(CarStatus.NULL)
    val readStatusS3: StateFlow<CarStatus> = _readStatusS3

    var isMarquee = false

    init {
        if (devicesList.isNotEmpty()) {
            selectDevice(DEVICE_TTYS4)
            selectDevice(DEVICE_TTYS3)
        }
    }

    fun selectDevice(path: String?, baudRate: Int = 115200, flags: Int = 0) {
        if (path.isNullOrEmpty() || path !in devicesList) {
            return
        }
        runCatching {
            val serialPort = SerialPort(File(path), baudRate, flags)
            serialPortList.add(serialPort)
            readSerialPort(serialPort, path)
        }
    }


    fun write(status: CarStatus) {
        //serialPortList.forEach { it.outputStream.write(status.value.toByteArray()) }
        write(status.value)
    }

    fun write(value: String) {
        Timber.d("write $value")
        serialPortList.forEach { it.outputStream.write(value.toByteArray()) }
    }

    fun startMarquee() {
        isMarquee = true
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive && isMarquee) {
                CarStatus.values().forEach { status ->
                    if (status != CarStatus.NULL && isMarquee) {
                        write(status)
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
        Timber.d("start read led status ")
        val buffer = ByteArray(32) { 32 }
        var result = ""
        var status: CarStatus = CarStatus.NULL
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                serialPort.inputStream.read(buffer)
                result = buffer.decodeToString()
                when {
                    result.startsWith(CarStatus.SUBSCRIBE.code) -> status = CarStatus.SUBSCRIBE
                    result.startsWith(CarStatus.EMPTY.code) -> status = CarStatus.EMPTY
                    result.startsWith(CarStatus.RUNNING.code) -> status = CarStatus.RUNNING
                    result.startsWith(CarStatus.REST.code) -> status = CarStatus.REST
                }
                when (path) {
                    DEVICE_TTYS3 -> {
                        _readStatusS3.emit(status)
                    }
                    DEVICE_TTYS4 -> {
                        _readStatusS4.emit(status)
                    }
                }
                delay(1000L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        serialPortList.forEach { it.close() }
    }
}