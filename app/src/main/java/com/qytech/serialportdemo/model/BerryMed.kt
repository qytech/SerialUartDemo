package com.qytech.serialportdemo.model

import com.qytech.serialportdemo.getIndexBit
import com.qytech.serialportdemo.getValueBit
import com.qytech.serialportdemo.toBinaryString
import com.qytech.serialportdemo.toHexString
import timber.log.Timber

class BerryMed(private val value: ByteArray) {
    /**
     * 校验数据是否可用
     * 0 第一个字节表示了信号强度等信息,同部位为 1
     * 1 第二个字节表示体积描记图，同部位为 0
     * 2 第三个字节表示棒图，同部位为 0
     * 3 第四个字节表示脉率，同部位为 0
     * 4 第五个字节表示需氧饱和度，同部位为 0
     * 5 第六个字节表示体温整数部分，同部位为 0
     * 6 第七个字节表示体温小叔部分，同部位为 0
     */
    fun isAvailable() = value[0].getIndexBit(7) == 1 &&
            value[1].getIndexBit(7) == 0 &&
            value[2].getIndexBit(7) == 0 &&
            value[3].getIndexBit(7) == 0 &&
            value[4].getIndexBit(7) == 0 &&
            value[5].getIndexBit(7) == 0 &&
            value[6].getIndexBit(7) == 0

    // 信号强度 0~8
    val signalStrength: Int
        get() = value[0].getValueBit("1111".toInt(2))

    // 搜索信号
    val signalSearch: Int
        get() = value[0].getIndexBit(4)

    // 探头是否脱落
    val probeFallOff: Int
        get() = value[0].getIndexBit(5)

    // 脉搏声音提示
    val pulseSound: Int
        get() = value[0].getIndexBit(6)

    // 脉搏波形
    val pulseContour: Int
        get() = value[1].getValueBit()

    // 棒图
    val stickFigure: Int
        get() = value[2].getValueBit("1111".toInt(2))

    // 检测手指是否插入
    val fingerInserted: Int
        get() = value[2].getIndexBit(4)

    // 搜索脉搏
    val pulseSearch: Int
        get() = value[2].getIndexBit(5)

    // 脉率第七位
    val pulseRateHigh: Int
        get() = value[2].getIndexBit(6)

    // 脉率
    val pulseRateFirst: Int
        get() = value[3].getValueBit()


    // 血氧饱和度
    val oxygenSaturation: Int
        get() = value[4].getValueBit()

    // 温度
    val temperature: String
        get() = "${value[5].getValueBit()}.${value[6].getValueBit()}"

    fun isSignalStrengthNormal() = signalStrength in 0..8

    fun isSignalSearchCompleted() = signalSearch == 0

    fun isProbeNormal() = probeFallOff == 0

    fun isFingerInserted() = fingerInserted == 0

    fun isPulseSearchCompleted() = pulseSearch == 0

    fun getPulseRate() = if (pulseRateHigh == 1) pulseRateFirst.or(128) else pulseRateFirst

    fun getMessage(): String {
        return if (isAvailable() &&
            isSignalSearchCompleted() &&
            isProbeNormal() &&
            isFingerInserted()
        ) {
            if (isPulseSearchCompleted() && getPulseRate() != 255 && oxygenSaturation != 127) {
                "脉率为: ${getPulseRate()}次/min\n血氧饱和度为：${oxygenSaturation}%\n温度为：${temperature}℃"
            } else {
                "正在检查脉率和血氧饱和度...\n温度为：${temperature}℃"
            }
        } else {
            "请插入手指开始检查脉率和血氧饱和度\n温度为：${temperature}℃"
        }
    }
}
