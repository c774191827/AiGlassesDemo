package com.lw.ai.glasses.ui.base.screen.popup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
fun CenteredFadeInPopup(
    visible: Boolean, // 控制整个 Popup 是否在组合中
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier, // Modifier for the content Box
    cornerRadius: Dp = 16.dp,
    animationDurationMillis: Int = 300,
    scrimColor: Color = Color.Black.copy(alpha = 0.6f), // 遮罩目标颜色
    contentAlignment: Alignment = Alignment.Center, // 允许自定义内容对齐
    content: @Composable ColumnScope.() -> Unit
) {
    // 控制内容动画 (淡入淡出 + 可选的轻微缩放)
    val contentTransitionState = remember { MutableTransitionState(false) }

    // 控制遮罩层 alpha 的动画状态
    val scrimAlphaTarget = if (visible) 1f else 0f
    val scrimAlpha by animateFloatAsState(
        targetValue = scrimAlphaTarget,
        animationSpec = tween(durationMillis = animationDurationMillis),
        label = "scrimAlpha"
    )

    // 当外部 visible 变化时，更新内容动画的目标状态
    LaunchedEffect(visible) {
        contentTransitionState.targetState = visible
    }

    // Popup 存在条件：外部要求可见，或遮罩/内容还在渐隐过程中
    val shouldPopupBeComposed = visible || scrimAlpha > 0.01f || contentTransitionState.currentState

    if (shouldPopupBeComposed) {
        Popup(
            alignment = contentAlignment, // 使用参数指定的对齐方式
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = true,
                dismissOnClickOutside = true,
                dismissOnBackPress = true
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(), // Popup 自身填满以便遮罩层生效
                contentAlignment = contentAlignment // 内容对齐也使用参数
            ) {
                // 1. 遮罩层 (始终在最底层，只做 alpha 动画)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(scrimAlpha)
                        .background(scrimColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = visible, // 只有当弹窗逻辑上可见时才允许通过遮罩关闭
                            onClick = {
                                // PopupProperties 的 dismissOnClickOutside 会处理
                                // onDismissRequest()
                            }
                        )
                )

                // 2. 内容层 (在遮罩层之上，做淡入淡出动画)
                AnimatedVisibility(
                    visibleState = contentTransitionState,
                    enter = fadeIn(animationSpec = tween(animationDurationMillis)) +
                            scaleIn(
                                initialScale = 0.9f, // 从 чуть меньше 开始，产生轻微的弹出感
                                animationSpec = tween(animationDurationMillis),
                                transformOrigin = TransformOrigin.Center
                            ),
                    exit = fadeOut(animationSpec = tween(animationDurationMillis)) +
                            scaleOut(
                                targetScale = 0.9f,
                                animationSpec = tween(animationDurationMillis),
                                transformOrigin = TransformOrigin.Center
                            ),
                    modifier = Modifier.wrapContentSize() // 自适应内容大小
                ) {
                    Column( // 使用 Column 方便内容垂直排列
                        modifier = modifier // 应用外部传入的 modifier (通常用于设置大小限制、padding 等)
                            .wrapContentSize() // 确保 Column 也自适应内容
                            .clip(RoundedCornerShape(cornerRadius))
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
