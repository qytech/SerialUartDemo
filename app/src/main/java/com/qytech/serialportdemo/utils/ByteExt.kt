package com.qytech.serialportdemo.utils

import java.util.*

/**
 * 将字符串转化为 16 进制编码的 ByteArray数组
 * @param size 想要返回数组的长度，默认情况下返回的就是转化数据的长度，如果设置的 size 比实际长度大会填充 0
 * @return ByteArray 转化的结果
 * */
fun String.toHexByteArray(size: Int = 0): ByteArray = this.let {
    val value = it.replace("\\n+".toRegex(), "").replace("\\s+".toRegex(), "")
    val len = value.length
    val data = ByteArray(if (len / 2 > size) len / 2 else size)
    (0 until len step 2).forEach { index ->
        data[index / 2] = ((Character.digit(value[index], 16) shl 4) +
                Character.digit(value[index + 1], 16)).toByte()
    }
    data
}

fun Byte.toHexString() = "%02x ".format(this).uppercase()

/**
 * 将ByteArray转化为 16 进制编码的字符串
 * @return string 编码后的字符串
 */
fun ByteArray.toHexString() = joinToString("") {
    it.toHexString()
}.uppercase(Locale.getDefault())

fun Int.toUInt32ByteArray(): ByteArray {
    val bytes = ByteArray(4)
    bytes[3] = (this and 0xFFFF).toByte()
    bytes[2] = ((this ushr 8) and 0xFFFF).toByte()
    bytes[1] = ((this ushr 16) and 0xFFFF).toByte()
    bytes[0] = ((this ushr 24) and 0xFFFF).toByte()
    return bytes
}

fun IntArray.toByteArray(): ByteArray =
    this.foldIndexed(ByteArray(this.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }

fun Array<out Int>.toByteArray(): ByteArray =
    this.foldIndexed(ByteArray(this.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }
