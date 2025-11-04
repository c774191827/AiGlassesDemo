package com.lw.ai.glasses.ui.theme.components

import android.icu.text.DecimalFormat // 使用 android.icu.text.DecimalFormat
import android.icu.text.NumberFormat // 使用 android.icu.text.NumberFormat
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale
import kotlin.math.max // 用于 OffsetMapping

class ThousandSeparatorVisualTransformation(
    private val locale: Locale = Locale.getDefault(),
    private val maximumFractionDigits: Int = 8 // 默认允许3位小数
) : VisualTransformation {
    private val formatter = (NumberFormat.getNumberInstance(locale) as DecimalFormat).apply {
        // isGroupingUsed = true 默认就是 true
        // 控制小数位数由构造函数参数传入
        this.maximumFractionDigits = this@ThousandSeparatorVisualTransformation.maximumFractionDigits
    }
    // 获取当前地区的分组分隔符和小数点符号
    private val groupingSeparator = formatter.decimalFormatSymbols.groupingSeparator
    private val decimalSeparator = formatter.decimalFormatSymbols.decimalSeparator

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text // 这是 ViewModel 传过来的纯数字字符串
        if (originalText.isEmpty()) {
            return TransformedText(AnnotatedString(""), OffsetMapping.Identity)
        }

        val formattedText: String
        val numberPart: String // 小数点前的部分（如果有点的话）或整个字符串
        val decimalPart: String? // 小数点后的部分
        val hasTrailingDecimalSeparator: Boolean // 是否以小数点结尾，例如 "123."

        if (originalText == decimalSeparator.toString()) {
            // 用户只输入了一个小数点
            formattedText = decimalSeparator.toString()
            numberPart = ""
            decimalPart = null
            hasTrailingDecimalSeparator = true
        } else if (originalText.endsWith(decimalSeparator) && originalText.length > 1) {
            // 用户输入了类似 "123."
            val beforeDecimal = originalText.dropLast(1)
            try {
                // 格式化小数点前的部分
                val formattedBeforeDecimal = formatter.format(DecimalFormat(locale.toLanguageTag()).parse(beforeDecimal)) // 使用 DecimalFormat 解析以避免本地化问题
                formattedText = "$formattedBeforeDecimal$decimalSeparator"
                numberPart = beforeDecimal
                decimalPart = null // 因为是尾随点，小数部分视为空
                hasTrailingDecimalSeparator = true
            } catch (e: Exception) {
                // 如果小数点前的内容无法解析，则不进行格式化
                return TransformedText(text, OffsetMapping.Identity)
            }
        } else if (originalText.contains(decimalSeparator)) {
            // 包含小数点，例如 "123.45"
            val parts = originalText.split(decimalSeparator, limit = 2)
            val intPart = parts[0]
            val fracPart = parts.getOrNull(1) ?: ""
            try {
                val formattedIntPart = if (intPart.isEmpty()) "0" else formatter.format(DecimalFormat(locale.toLanguageTag()).parse(intPart))
                // 确保小数部分不超过允许的最大位数 (VisualTransformation 不应该截断数据，这应该是ViewModel的职责)
                // 但为了显示正确，这里可以考虑
                val limitedFracPart = if (fracPart.length > maximumFractionDigits) fracPart.substring(0, maximumFractionDigits) else fracPart
                formattedText = "$formattedIntPart$decimalSeparator$limitedFracPart"
                numberPart = intPart
                decimalPart = limitedFracPart
                hasTrailingDecimalSeparator = false
            } catch (e: Exception) {
                return TransformedText(text, OffsetMapping.Identity)
            }
        } else {
            // 只有整数部分，例如 "12345"
            try {
                formattedText = formatter.format(DecimalFormat(locale.toLanguageTag()).parse(originalText))
                numberPart = originalText
                decimalPart = null
                hasTrailingDecimalSeparator = false
            } catch (e: Exception) {
                return TransformedText(text, OffsetMapping.Identity)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // 原始文本: 1000.50 (offset=5 -> '.')
                // 格式化文本: 1,000.50
                // 需要计算在原始offset之前有多少个字符被插入（分隔符）
                if (offset == 0) return 0
                if (offset > originalText.length) return formattedText.length

                var separatorsBeforeOriginalOffset = 0
                val partBeforeOriginalOffset = originalText.substring(0, offset)

                // 计数小数点前的分隔符
                val integerPartOfPart = partBeforeOriginalOffset.split(decimalSeparator).first()
                if (integerPartOfPart.isNotEmpty()) {
                    try {
                        val formattedIntegerPartOfPart = formatter.format(DecimalFormat(locale.toLanguageTag()).parse(integerPartOfPart))
                        separatorsBeforeOriginalOffset = formattedIntegerPartOfPart.count { it == groupingSeparator }
                    } catch (e: Exception) { /* no-op */ }
                }

                // 如果原始偏移量在小数点之后，则在格式化字符串中也应该在小数点之后
                // 并且小数点本身也算一个字符
                // 这部分逻辑非常复杂，需要精确计算
                // 一个更简单（但不完美）的方法是逐步构建映射
                // 让我们尝试一种更直接的计算方式：
                var transformedOffset = 0
                var originalCharsProcessed = 0
                var i = 0
                while (i < formattedText.length && originalCharsProcessed < offset) {
                    if (formattedText[i] == originalText[originalCharsProcessed]) {
                        originalCharsProcessed++
                    }
                    // else it's a separator, don't increment originalCharsProcessed
                    transformedOffset++
                    i++
                }
                // If originalCharsProcessed < offset, it means we consumed all formattedText
                // but not all originalText. This can happen if formatting shortens the text (unlikely for numbers).
                // Or if offset was beyond the part that got formatted (e.g. original "1000abc", offset at 'a')
                // For "123." -> "123." (length 4) or "1,234." -> "1,234." (length 6)
                // original "1234." offset 4 (at '.')
                // formatted "1,234." offset 5 (at '.')
                if (hasTrailingDecimalSeparator && offset == originalText.length) { // 光标在尾随小数点后
                    return formattedText.length
                }
                // If the loop finished because we processed enough original characters
                if(originalCharsProcessed == offset){
                    // We might need to add more to transformedOffset if the remaining originalText characters
                    // exactly match the remaining formattedText characters up to a point,
                    // but there are trailing separators in formattedText.
                    // This is still very tricky.
                }

                // 简化版：遍历格式化后的字符串，跳过分隔符，匹配原始字符
                // 找到原始offset对应的字符在格式化字符串中的位置
                var currentOriginalOffset = 0
                for (k in formattedText.indices) {
                    if (currentOriginalOffset < originalText.length && formattedText[k] == originalText[currentOriginalOffset]) {
                        if (currentOriginalOffset == offset -1) { // 找到了原始offset前一个字符的位置
                            return k + 1
                        }
                        currentOriginalOffset++
                    } else if (formattedText[k] != groupingSeparator) {
                        // 如果字符不匹配且不是分组符，说明原始文本和格式化逻辑有差异
                        // 这种情况不应发生如果originalText是纯净的
                    }
                    if (currentOriginalOffset == offset && offset == 0) return 0 //处理offset=0
                }
                // 如果原始文本是 "123." offset=4 (在点之后), 格式化为 "123."
                // 如果原始文本是 "1234." offset=5 (在点之后), 格式化为 "1,234."
                // 下面是一个相对健壮的思路：
                // 1. 计算原始字符串中，小数点前的字符数 `origIntLength` 和小数点后的字符数 `origFracLength`
                // 2. 格式化小数点前的部分，得到 `formattedIntPart`
                // 3. 如果原始 `offset <= origIntLength`，则在 `formattedIntPart` 中找到对应的位置
                // 4. 如果原始 `offset > origIntLength`，则转换后的位置是 `formattedIntPart.length + 1 (for decimal sep) + (offset - origIntLength -1)`

                val (origIntPartStr, origFracPartStr) = if (originalText.contains(decimalSeparator)) {
                    originalText.split(decimalSeparator, limit = 2).let { it[0] to (it.getOrNull(1) ?: "") }
                } else {
                    originalText to ""
                }

                val formattedIntPartStr = try {
                    if (origIntPartStr.isEmpty() && originalText.contains(decimalSeparator)) "0" // Handle ".5" -> "0.5"
                    else if (origIntPartStr.isEmpty()) "" // Handle ""
                    else formatter.format(DecimalFormat(locale.toLanguageTag()).parse(origIntPartStr))
                } catch (e: Exception) { origIntPartStr }


                if (offset <= origIntPartStr.length) { // 光标在整数部分或其末尾
                    var Ocount = 0
                    for(tIdx in formattedIntPartStr.indices) {
                        if(Ocount < origIntPartStr.length && formattedIntPartStr[tIdx] == origIntPartStr[Ocount]) {
                            Ocount++
                        }
                        if (Ocount == offset) return tIdx + 1
                    }
                    if (offset == 0) return 0 // 光标在最前面
                    return formattedIntPartStr.length // 光标在整数部分末尾
                } else { // 光标在小数点或小数部分
                    var transformed = formattedIntPartStr.length
                    if (originalText.contains(decimalSeparator)) transformed += 1 // 加上小数点本身
                    transformed += (offset - origIntPartStr.length - (if (originalText.contains(decimalSeparator)) 1 else 0))
                    return transformed.coerceAtMost(formattedText.length)
                }

            }

            override fun transformedToOriginal(offset: Int): Int {
                // 从格式化后的offset转回原始offset
                // "1,000.50" (offset=2 -> ',') -> "1000.50" (offset=1 -> '0')
                var originalOffset = 0
                var transformedCharsProcessed = 0
                var i = 0
                while (i < originalText.length && transformedCharsProcessed < offset) {
                    if (originalText[i] == formattedText[transformedCharsProcessed]) {
                        transformedCharsProcessed++
                        // Not a separator, advance originalOffset only if it's not a separator
                    } else if (formattedText[transformedCharsProcessed] == groupingSeparator) {
                        transformedCharsProcessed++ // Skip separator
                        // Don't increment originalOffset
                        continue
                    } else {
                        // Mismatch, should not happen ideally
                        break
                    }
                    originalOffset++
                    i++
                }
                // 更简单的方法： 计算在格式化后的offset之前有多少个分隔符
                var separatorCountInTransformed = 0
                for(k in 0 until offset.coerceAtMost(formattedText.length)) {
                    if(formattedText[k] == groupingSeparator) {
                        separatorCountInTransformed++
                    }
                }
                val calculatedOriginalOffset = offset - separatorCountInTransformed

                // 如果原始文本是 "123." 格式化 "123."
                // 如果是 "1234." 格式化 "1,234."
                // 转换后offset 5 (在点之后) -> 原始offset 5 (格式化前是"1234.", 点在第5个，index 4)
                if (hasTrailingDecimalSeparator && offset == formattedText.length) {
                    return originalText.length
                }

                return max(0, calculatedOriginalOffset.coerceAtMost(originalText.length))
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
