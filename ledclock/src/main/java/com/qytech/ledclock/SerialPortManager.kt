package com.qytech.ledclock

import android_serialport_api.SerialPort
import com.qytech.ledclock.utils.TimeUtils
import com.qytech.serialportbase.extensions.asHexUpper
import com.qytech.serialportbase.extensions.toByteArray
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import kotlin.coroutines.CoroutineContext

object LedClock {

    const val COMMAND_BYTE_INDEX = 3
    const val DATA_LENGTH_INDEX = 4
    const val DATA_START_INDEX = 5

    const val COMMAND_TIME: Byte = 0x06
    const val COMMAND_FLAG: Byte = 0x07
    const val COMMAND_LOADER: Byte = 0x11
    const val COMMAND_UPDATE: Byte = 0x12
}

class SerialPortManager : CoroutineScope {
    companion object {
        private const val DEV_PATH = "/dev/ttyS3"
    }

    private val serialPort by lazy {
        SerialPort(File(DEV_PATH), 115200, 0)
    }

    private val outputStream = serialPort.outputStream
    private val inputStream = serialPort.inputStream

    private val headerArray = ByteArray(5).apply {
        this[0] = 0x4C
        this[1] = 0x45
        this[2] = 0x44
    }

    fun setLedClockTime() {
        val data = TimeUtils.getTimeBytes()
        val command = getCommandBytes(LedClock.COMMAND_TIME, 0x06, *data)
        outputStream.write(command)
    }

    fun getLedClockTime() {
        val command = getCommandBytes(LedClock.COMMAND_TIME, 0x00)
        outputStream.write(command)
    }

    fun setLedClockFlag(isWifi: Boolean, isTel: Boolean) {
        Timber.d("isWifi connect $isWifi , is Tel connect $isTel")
        val data =
            arrayOf(isWifi, isTel).map { if (it) 0x01 else 0x00 }.toTypedArray().toByteArray()
        val command = getCommandBytes(LedClock.COMMAND_FLAG, 0x02, *data)
        outputStream.write(command)
    }

    fun getLedClockFlag() {
        val command = getCommandBytes(LedClock.COMMAND_FLAG, 0x00)
        outputStream.write(command)
    }

    private fun getCommandBytes(command: Byte, dataLength: Byte, vararg data: Byte): ByteArray {
        headerArray[LedClock.COMMAND_BYTE_INDEX] = command
        headerArray[LedClock.DATA_LENGTH_INDEX] = dataLength
        return headerArray.plus(data).let { bytearray ->
            bytearray.plus(bytearray.sum().toByte()).apply {
                Timber.d("send command ${this.asHexUpper}")
            }
        }
    }

    suspend fun readSerialPort() {
        val buffer = ByteArray(32) { 0x00 }

        withContext(Dispatchers.IO) {
            runCatching {
                while (isActive) {
                    inputStream.read(buffer)
                    Timber.d("readSerialPort ${buffer.asHexUpper}")

                    delay(1000L)
                }
            }
        }
    }

    fun onCleared() {
        inputStream.close()
        outputStream.close()
        serialPort.close()
        cancel()
    }

    override val coroutineContext: CoroutineContext = Dispatchers.IO
}