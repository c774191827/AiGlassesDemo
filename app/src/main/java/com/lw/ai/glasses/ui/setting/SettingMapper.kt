package com.lw.ai.glasses.ui.setting

import com.fission.wear.glasses.sdk.constant.LyCmdConstant

object SettingMapper {

    fun toLedBrightnessOptions(): List<SelectOption<LyCmdConstant.LedBrightnessLevel>> {
        return listOf(
            SelectOption(LyCmdConstant.LedBrightnessLevel.LOW, "低"),
            SelectOption(LyCmdConstant.LedBrightnessLevel.MEDIUM, "中"),
            SelectOption(LyCmdConstant.LedBrightnessLevel.HIGH, "高")
        )
    }

    fun toScreenOrientationOptions(): List<SelectOption<LyCmdConstant.ScreenOrientation>> {
        return listOf(
            SelectOption(LyCmdConstant.ScreenOrientation.PORTRAIT, "竖屏"),
            SelectOption(LyCmdConstant.ScreenOrientation.LANDSCAPE, "横屏")
        )
    }

    fun toGestureTypeTitle(gestureType: LyCmdConstant.GestureType): String {
        return when (gestureType) {
            LyCmdConstant.GestureType.SLIDE_FORWARD -> "前滑手势"
            LyCmdConstant.GestureType.SLIDE_BACKWARD -> "后滑手势"
            LyCmdConstant.GestureType.SINGLE_TAP -> "单击手势"
            LyCmdConstant.GestureType.DOUBLE_TAP -> "双击手势"
            LyCmdConstant.GestureType.TRIPLE_TAP -> "三击手势"
        }
    }

    fun toWearDetectionOptions(): List<SelectOption<LyCmdConstant.WearDetectionState>> {
        return listOf(
            SelectOption(LyCmdConstant.WearDetectionState.ON, "开启"),
            SelectOption(LyCmdConstant.WearDetectionState.OFF, "关闭")
        )
    }

    fun toGestureActionOptions(): List<SelectOption<LyCmdConstant.GestureAction>> {
        return listOf(
            SelectOption(LyCmdConstant.GestureAction.VOLUME_DOWN, "音量减小"),
            SelectOption(LyCmdConstant.GestureAction.VOLUME_UP, "音量增大"),
            SelectOption(LyCmdConstant.GestureAction.PLAY_PAUSE, "播放/暂停"),
            SelectOption(LyCmdConstant.GestureAction.PREVIOUS, "上一曲"),
            SelectOption(LyCmdConstant.GestureAction.NEXT, "下一曲"),
            SelectOption(LyCmdConstant.GestureAction.HANG_UP, "挂断电话")
        )
    }


}