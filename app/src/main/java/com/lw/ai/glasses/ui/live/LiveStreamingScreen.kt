package com.lw.ai.glasses.ui.live

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LiveStreamingScreen(
    onNavigateBack: () -> Unit
) {
    Column {
        Text("直播流")
        Spacer(modifier = Modifier.height(20.dp))
        Text("连接抖音直播")
    }
}