package com.qytech.serialportdemo

import android_serialport_api.SerialPort
import android_serialport_api.SerialPortFinder
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qytech.serialportdemo.model.BerryMed
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import kotlin.experimental.and

class SerialPortViewModel : ViewModel() {
    companion object {
        const val DEFAULT_DEVICE = "/dev/ttyS4"
        const val BYTE_SIZE = 7
    }

    private var serialPort: SerialPort? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null
    private val devicesList = SerialPortFinder().allDevicesPath

    private val _message = MutableStateFlow("暂无数据...")
    val message: StateFlow<String> = _message


    init {
        if (devicesList.isNotEmpty()) {
            selectDevice()
            read()
        }
    }

    private fun selectDevice(path: String? = DEFAULT_DEVICE) {
        if (path.isNullOrEmpty() || path !in devicesList) {
            return
        }
        runCatching {
            serialPort = SerialPort(File(path), 115200, 0)
            inputStream = serialPort?.inputStream
            outputStream = serialPort?.outputStream
        }
    }

    private fun read() {
        var berryMed: BerryMed
        var value: ByteArray
        val buffer = ByteArray(1024)
        var size = 0
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                runCatching {
                    // 每秒发送 50 个包加上起始位和停止位，7 字节格式
                    size = inputStream?.read(buffer) ?: 0
                    (0 until size step BYTE_SIZE).forEach { index ->
                        if (index + BYTE_SIZE >= size) return@forEach
                        value = buffer.copyOfRange(index, index + BYTE_SIZE)
                        berryMed = BerryMed(value)
                        withContext(Dispatchers.Main) {
                            Timber.d("message is ${value.toHexString()} ${berryMed.getMessage()}")
                            if (_message.value != berryMed.getMessage()) {
                                _message.emit(berryMed.getMessage())
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
        serialPort?.close()
    }
}

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

fun Byte.toBinaryString() = Integer.toBinaryString(this.toInt().and(0xff)).padStart(8, '0')

// 获取某一位的值 0x54 >> index & 1
fun Byte.getIndexBit(index: Int) = this.toInt().and(0xff).shr(index).and(1)

fun Byte.getValueBit(bit: Int = 127) = this.toInt().and(0xff).and(bit)
