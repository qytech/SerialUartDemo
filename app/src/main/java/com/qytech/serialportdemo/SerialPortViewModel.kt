package com.qytech.serialportdemo

import android_serialport_api.SerialPort
import android_serialport_api.SerialPortFinder
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.InputStream
import java.io.OutputStream

enum class CarStatus(val value: String, val message: String, val color: Color) {
    EMPTY("LED1", "空车", Color.Red),
    SUBSCRIBE("LED0", "预约", Color.Blue),
    RUNNING("LED2", "运行", Color.Yellow),
    REST("LED3", "休息", Color.Green)
}

class SerialPortViewModel : ViewModel() {
    companion object {
        const val DEFAULT_DEVICE = "/dev/ttyS4"
        const val DEVICE_TTYS3 = "/dev/ttyS3"
    }

    private var serialPortList: MutableList<SerialPort> = ArrayList()


    private val devicesList = SerialPortFinder().allDevicesPath


    init {
        if (devicesList.isNotEmpty()) {
            selectDevice(DEFAULT_DEVICE)
            selectDevice(DEVICE_TTYS3)
        }
    }

    fun selectDevice(path: String?, baudRate: Int = 115200, flags: Int = 0) {
        if (path.isNullOrEmpty() || path !in devicesList) {
            return
        }
        runCatching {
            serialPortList.add(SerialPort(File(path), baudRate, flags))
        }
    }


    fun write(status: CarStatus) {
        serialPortList.forEach { it.outputStream.write(status.value.toByteArray()) }
    }

    override fun onCleared() {
        super.onCleared()
        serialPortList.forEach { it.close() }
    }
}