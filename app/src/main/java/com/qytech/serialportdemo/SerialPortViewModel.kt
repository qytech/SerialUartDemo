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
    RUNING("LED2", "运行", Color.Yellow),
    REST("LED3", "休息", Color.Green)
}

class SerialPortViewModel : ViewModel() {
    private var serialPort: SerialPort? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    val devicesList = SerialPortFinder().allDevicesPath

    init {
        if (devicesList.isNotEmpty()) {
            selectDevice(devicesList[0])
        }
    }

    fun selectDevice(path: String?) {
        if (path.isNullOrEmpty() || path !in devicesList) {
            return
        }
        runCatching {
            serialPort = SerialPort(File(path), 115200, 0)
            inputStream = serialPort?.inputStream
            outputStream = serialPort?.outputStream
            outputStream?.write(CarStatus.EMPTY.value.toByteArray())
        }
    }

    fun write(status: CarStatus) {
        outputStream?.write(status.value.toByteArray())
    }

    override fun onCleared() {
        super.onCleared()
        serialPort?.close()
    }
}