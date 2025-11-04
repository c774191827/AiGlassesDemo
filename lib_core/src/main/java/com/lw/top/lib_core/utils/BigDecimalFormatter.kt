package com.lw.top.lib_core.utils

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object BigDecimalFormatter {

    const val THRESHOLD_FOR_AUTOMATIC_GROUPING = 1000.0
    const val DEFAULT_MAX_FRACTION_DIGITS = 5
    private const val PRESET_DEFAULT_FOR_NULL_AS_ZERO = "0"
    private const val PRESET_DEFAULT_FOR_NULL_AS_EMPTY = ""

    private fun formatDecimalInternal(
        value: BigDecimal?,
        maxFractionDigits: Int,
        useGrouping: Boolean,
        actualDefaultValueIfNull: String
    ): String {
        if (value == null) {
            return actualDefaultValueIfNull
        }
        val absValue = value.abs() // 使用绝对值进行比较
        val adjustedMaxFractionDigits = when {
            absValue > BigDecimal("100000") -> 0
            absValue > BigDecimal("10000") -> 1
            absValue > BigDecimal("1000") -> 2
            else -> maxFractionDigits
        }

        val normalFormat = DecimalFormat()
        normalFormat.maximumFractionDigits = adjustedMaxFractionDigits
        normalFormat.isGroupingUsed = useGrouping
        normalFormat.decimalFormatSymbols = DecimalFormatSymbols(Locale.US)
        return normalFormat.format(value)
    }

    fun formatDecimalOrDefault(
        value: BigDecimal?,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        thresholdForGrouping: Double? = THRESHOLD_FOR_AUTOMATIC_GROUPING
    ): String {
        val shouldUseGrouping = thresholdForGrouping?.let {
            value != null && value.abs() >= BigDecimal.valueOf(it)
        } ?: false

        return formatDecimalInternal(
            value = value,
            maxFractionDigits = maxFractionDigits,
            useGrouping = shouldUseGrouping,
            actualDefaultValueIfNull = PRESET_DEFAULT_FOR_NULL_AS_ZERO
        )
    }

    fun formatDecimalOrEmpty(
        value: BigDecimal?,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        thresholdForGrouping: Double? = THRESHOLD_FOR_AUTOMATIC_GROUPING
    ): String {
        val shouldUseGrouping = thresholdForGrouping?.let {
            value != null && value.abs() >= BigDecimal.valueOf(it)
        } ?: false

        return formatDecimalInternal(
            value = value,
            maxFractionDigits = maxFractionDigits,
            useGrouping = shouldUseGrouping,
            actualDefaultValueIfNull = PRESET_DEFAULT_FOR_NULL_AS_EMPTY
        )
    }

    fun formatDecimalCustom(
        value: BigDecimal?,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        useGrouping: Boolean = true,
        defaultValueIfNull: String
    ): String {
        return formatDecimalInternal(
            value = value,
            maxFractionDigits = maxFractionDigits,
            useGrouping = useGrouping,
            actualDefaultValueIfNull = defaultValueIfNull
        )
    }
}