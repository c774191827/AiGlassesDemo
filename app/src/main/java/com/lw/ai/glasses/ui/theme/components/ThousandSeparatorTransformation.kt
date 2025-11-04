package com.lw.ai.glasses.ui.theme.components
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat

class ThousandSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(formatWithSeparator(text.text)),
            offsetMapping = ThousandSeparatorOffsetMapping(text.text)
        )
    }

    private fun formatWithSeparator(input: String): String {
        if (input.isEmpty()) return ""

        // 分割整数和小数部分
        val parts = input.split('.')
        val integerPart = parts[0].replace(",", "").filter { it.isDigit() }
        val decimalPart = if (parts.size > 1) ".${parts[1].filter { it.isDigit() }}" else ""

        return try {
            // 只对整数部分添加千位分隔符
            val formattedInteger = if (integerPart.isNotEmpty()) {
                NumberFormat.getNumberInstance().format(integerPart.toLong())
            } else {
                ""
            }
            formattedInteger + decimalPart
        } catch (e: NumberFormatException) {
            // 数字太大无法解析时，返回原始格式
            integerPart + decimalPart
        }
    }
}

class ThousandSeparatorOffsetMapping(private val originalText: String) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int {
        if (originalText.isEmpty()) return 0 // 关键：原始文本为空，转换后偏移量为0

        val parts = originalText.split('.')
        val integerPart = parts[0].replace(",", "")
        val decimalPart = if (parts.size > 1) ".${parts[1]}" else ""

        // 计算整数部分转换后的长度
        val formattedInteger = formatIntegerPart(integerPart)

        // 确定小数点的位置
        val decimalPointPos = if (decimalPart.isNotEmpty()) formattedInteger.length else -1

        if (offset <= integerPart.length) {
            // 光标在整数部分
            var originalCount = 0
            for (i in formattedInteger.indices) {
                if (formattedInteger[i].isDigit()) {
                    originalCount++
                    if (originalCount > offset) {
                        return i
                    }
                }
            }
            return formattedInteger.length
        } else {
            // 光标在小数部分
            return decimalPointPos + 1 + (offset - integerPart.length - 1)
        }
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (originalText.isEmpty()) return 0 // 关键：原始文本为空，原始偏移量也为0

        val transformed = formatWithSeparator(originalText)
        val decimalPos = transformed.indexOf('.')

        if (decimalPos == -1 || offset <= decimalPos) {
            // 处理整数部分
            var digitCount = 0
            for (i in 0 until offset) {
                if (i < transformed.length && transformed[i].isDigit()) {
                    digitCount++
                }
            }
            return digitCount
        } else {
            // 处理小数部分
            val integerDigits = transformed.substring(0, decimalPos).count { it.isDigit() }
            return integerDigits + 1 + (offset - decimalPos - 1)
        }
    }

    private fun formatIntegerPart(integerPart: String): String {
        return if (integerPart.isNotEmpty()) {
            try {
                NumberFormat.getNumberInstance().format(integerPart.toLong())
            } catch (e: NumberFormatException) {
                integerPart
            }
        } else {
            ""
        }
    }

    private fun formatWithSeparator(input: String): String {
        if (input.isEmpty()) return ""

        val parts = input.split('.')
        val integerPart = parts[0].replace(",", "")
        val decimalPart = if (parts.size > 1) ".${parts[1]}" else ""

        return formatIntegerPart(integerPart) + decimalPart
    }
}