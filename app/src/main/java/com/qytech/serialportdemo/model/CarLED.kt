package com.qytech.serialportdemo.model

import androidx.compose.ui.graphics.Color

object CarLED {

    const val COMMAND_BYTE_WRITE_STATUS: Byte = 0x01
    const val RESULT_BYTE_WRITE_STATUS: Byte = 0x81.toByte()
    const val COMMAND_BYTE_READ_STATUS: Byte = 0x02
    const val RESULT_BYTE_READ_STATUS: Byte = 0x82.toByte()
    const val COMMAND_BYTE_WRITE_CHARSET: Byte = 0x03
    const val RESULT_BYTE_WRITE_CHARSET: Byte = 0x82.toByte()
    const val COMMAND_BYTE_READ_CHARSET: Byte = 0x04
    const val RESULT_BYTE_READ_CHARSET: Byte = 0x84.toByte()

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


    enum class Status(
        val value: Byte,
        val message: String,
        val color: Color,
        val charsetColor: Byte,
        val charset: String
    ) {
        SUBSCRIBE(0x00, "subscribe", Color.Blue, 0x02, LED0_CHARSET),
        EMPTY(0x01, "empty", Color.Red, 0x00, LED1_CHARSET),
        RUNNING(0x02, "running", Color.Yellow, 0x03, LED2_CHARSET),
        REST(0x03, "rest", Color.Green, 0x01, LED3_CHARSET),
        NULL(0x00, "", Color.Black, 0x00, ""),
    }

}