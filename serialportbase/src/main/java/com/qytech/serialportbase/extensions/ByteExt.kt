package com.qytech.serialportbase.extensions


val ByteArray.asHexLower
    inline get() = this.joinToString("") { it.toInt().and(0xFF).toString(16).padStart(2, '0') }

val ByteArray.asHexUpper inline get() = this.asHexLower.uppercase()

val String.hexAsByteArray
    inline get() = this.replace("[\\s\\n]".toRegex(), "")
        .chunked(2)
        .map { it.uppercase().toInt(16).toByte() }
        .toByteArray()

fun Int.toUInt32ByteArray(): ByteArray {
    val bytes = ByteArray(4)
    bytes[3] = (this and 0xFFFF).toByte()
    bytes[2] = ((this ushr 8) and 0xFFFF).toByte()
    bytes[1] = ((this ushr 16) and 0xFFFF).toByte()
    bytes[0] = ((this ushr 24) and 0xFFFF).toByte()
    return bytes
}

// arrayOf<Int>
fun Array<out Int>.toByteArray(): ByteArray =
    this.foldIndexed(ByteArray(this.size)) { i, a, v -> a.apply { set(i, v.toByte()) } }


