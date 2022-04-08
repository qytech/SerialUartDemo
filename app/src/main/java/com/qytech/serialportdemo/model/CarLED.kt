package com.qytech.serialportdemo.model

import androidx.compose.ui.graphics.Color

object CarLED {

    const val COMMAND_BYTE_WRITE_STATUS: Byte = 0x01
    const val COMMAND_BYTE_READ_STATUS: Byte = 0x02
    const val COMMAND_BYTE_WRITE_CHARSET: Byte = 0x03
    const val COMMAND_BYTE_READ_CHARSET: Byte = 0x04
    const val COMMAND_BYTE_SHOW: Byte = 0x05
    const val COMMAND_BYTE_LOADER: Byte = 0x11
    const val COMMAND_BYTE_DOWNLOAD_FIRMWARE: Byte = 0x12

    const val RESULT_BYTE_WRITE_STATUS: Byte = 0x81.toByte()
    const val RESULT_BYTE_READ_STATUS: Byte = 0x82.toByte()
    const val RESULT_BYTE_WRITE_CHARSET: Byte = 0x82.toByte()
    const val RESULT_BYTE_READ_CHARSET: Byte = 0x84.toByte()
    const val RESULT_BYTE_SHOW: Byte = 0x85.toByte()
    const val RESULT_BYTE_LOADER: Byte = 0x91.toByte()
    const val RESULT_BYTE_DOWNLOAD_FIRMWARE: Byte = 0x92.toByte()

    const val COMMAND_BYTE_INDEX = 3
    const val DATA_LENGTH_INDEX = 4
    const val DATA_START_INDEX = 5

    const val LED0_CHARSET =
        "00 00 00 01 C1 FE 12 22 01 12 22 01 12 21 FE 11 C0 44 10 00 44 10 07 FF F7 E0 00 02 47 FF 02 40 00 00 00 00"
    const val LED1_CHARSET =
        "00 00 00 40 80 3F 20 87 24 18 A4 24 07 A4 24 08 A4 24 10 84 3F 20 84 00 00 04 00 FF E4 FF 04 00 00 04 00 00"
    const val LED2_CHARSET =
        "00 00 00 00 80 40 06 87 46 49 A4 49 A9 A4 49 A6 84 49 A0 85 C9 A0 04 49 AF E4 49 A1 04 46 4F E0 40 00 00 00"
    const val LED3_CHARSET =
        "00 00 00 10 01 04 13 C1 04 12 4F 24 12 41 55 12 41 55 F2 41 55 12 41 55 12 4F 24 13 C1 04 10 01 04 00 00 00"

    const val COLOR_RED: Byte = 0x00
    const val COLOR_GREEN: Byte = 0x01
    const val COLOR_BLUE: Byte = 0x02
    const val COLOR_YELLOW: Byte = 0x03
    const val COLOR_PINK: Byte = 0x04
    const val COLOR_TURQUOISE: Byte = 0x05
    const val COLOR_WHITE: Byte = 0x06

    val ledColors = mapOf<String, Byte>(
        "red" to 0x00,
        "green" to 0x01,
        "blue" to 0x02,
        "yellow" to 0x03,
        "pink" to 0x04,
        "turquoise" to 0x05,
        "white" to 0x06
    )

    enum class Status(
        val value: Byte,
        val message: String,
        val color: Color,
        val charsetColor: Byte,
        val charset: String
    ) {
        SUBSCRIBE(0x00, "subscribe", Color.Blue, COLOR_BLUE, LED0_CHARSET),
        EMPTY(0x01, "empty", Color.Red, COLOR_RED, LED1_CHARSET),
        RUNNING(0x02, "running", Color.Yellow, COLOR_YELLOW, LED2_CHARSET),
        REST(0x03, "rest", Color.Green, COLOR_GREEN, LED3_CHARSET),
        CLOSE(0x04, "close", Color.Black, COLOR_WHITE, ""),
    }


}