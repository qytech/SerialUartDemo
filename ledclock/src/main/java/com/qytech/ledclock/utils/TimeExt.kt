package com.qytech.ledclock.utils

import android.annotation.SuppressLint
import com.qytech.serialportbase.extensions.toByteArray
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
object TimeUtils {
    fun getTimeBytes(): ByteArray {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yy MM dd HH mm ss")
        val formatted = current.format(formatter)
        return formatted.split(" ").map { it.toInt() }.toTypedArray().toByteArray()
    }
}