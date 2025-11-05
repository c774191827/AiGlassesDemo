package com.lw.ai.glasses.ui.theme.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    textToAnimate: String,
    modifier: Modifier = Modifier,
    typingDelay: Long = 50L,
    previousLength: Int = 0,
    onAnimationEnd: (Int) -> Unit
) {
    var displayedText by remember { mutableStateOf(textToAnimate.take(previousLength)) }


    LaunchedEffect(textToAnimate) {
        val startIndex = displayedText.length
        if (startIndex >= textToAnimate.length) {
            onAnimationEnd(displayedText.length)
            return@LaunchedEffect
        }

        for (i in startIndex until textToAnimate.length) {
            displayedText = textToAnimate.substring(0, i + 1)
            delay(typingDelay)
        }

        onAnimationEnd(displayedText.length)
    }

    Text(
        text = displayedText,
        modifier = modifier
    )
}