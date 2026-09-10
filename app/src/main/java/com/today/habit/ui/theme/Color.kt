package com.today.habit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** 当前是否为浅色主题（用背景亮度判定，跟随 App 主题切换） */
@Composable
@ReadOnlyComposable
private fun isIOSLightTheme(): Boolean =
    MaterialTheme.colorScheme.background.luminance() > 0.5f

// ============================================================
// iOS 系统色板（精确取自 iOS Human Interface Guidelines）
// ============================================================

// —— 品牌 / 语义色（light / dark）——
val IOSBlueLight = Color(0xFF007AFF)
val IOSBlueDark = Color(0xFF0A84FF)
val IOSGreenLight = Color(0xFF34C759)
val IOSGreenDark = Color(0xFF30D158)
val IOSRedLight = Color(0xFFFF3B30)
val IOSRedDark = Color(0xFFFF453A)
val IOSOrangeLight = Color(0xFFFF9500)
val IOSOrangeDark = Color(0xFFFF9F0A)
val IOSPurpleLight = Color(0xFFAF52DE)
val IOSPurpleDark = Color(0xFFBF5AF2)
val IOSTealLight = Color(0xFF5AC8FA)
val IOSTealDark = Color(0xFF64D2FF)

// —— 系统灰阶 ——
val IOSGrayLight = Color(0xFF8E8E93)
val IOSGrayDark = Color(0xFF98989D)
val IOSGray2 = Color(0xFFAEAEB2)
val IOSGray3 = Color(0xFFC7C7CC)
val IOSGray4 = Color(0xFFD1D1D6)
val IOSGray5 = Color(0xFFE5E5EA)
val IOSGray6 = Color(0xFFF2F2F7)

// —— 分组背景 ——
val IOSGroupedLight = Color(0xFFF2F2F7)
val IOSGroupedDark = Color(0xFF000000)
val IOSCardLight = Color(0xFFFFFFFF)
val IOSCardDark = Color(0xFF1C1C1E)
val IOSTertiaryCardLight = Color(0xFFF2F2F7)
val IOSTertiaryCardDark = Color(0xFF2C2C2E)

// —— 文字 ——
val IOSLabelLight = Color(0xFF000000)
val IOSLabelDark = Color(0xFFFFFFFF)
val IOSSecondaryLabelLight = Color(0x993C3C43) // 3C3C43 @60%
val IOSSecondaryLabelDark = Color(0x99EBEBF5) // EBEBF5 @60%
val IOSTertiaryLabelLight = Color(0x4D3C3C43) // 3C3C43 @30%
val IOSTertiaryLabelDark = Color(0x4DEBEBF5)

// —— 分隔线 ——
val IOSSeparatorLight = Color(0x4A3C3C43) // 3C3C43 @29%
val IOSSeparatorDark = Color(0x99545458) // 545458 @60%

// —— Switch 关闭态轨道 ——
val IOSSwitchOffLight = Color(0xFFE9E9EA)
val IOSSwitchOffDark = Color(0xFF39393D)

// —— 热力图绿色梯度（配合 systemGreen）——
val IOSHeat0Light = Color(0xFFE5E5EA)
val IOSHeat0Dark = Color(0xFF2C2C2E)
val IOSHeat1 = Color(0xFFA8E5B0)
val IOSHeat2 = Color(0xFF7BD88A)
val IOSHeat3 = Color(0xFF34C759)
val IOSHeat4 = Color(0xFF248A3D)
val IOSHeat5 = Color(0xFF145A23)

/**
 * iOS 语义色板：随当前主题（浅 / 深）自动切换。
 * 页面中优先使用这里的颜色，不要再硬编码 Material 绿或灰。
 */
object IOSColors {
    val background: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSGroupedLight else IOSGroupedDark

    val card: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSCardLight else IOSCardDark

    val tertiaryCard: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSTertiaryCardLight else IOSTertiaryCardDark

    val label: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSLabelLight else IOSLabelDark

    val secondaryLabel: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSSecondaryLabelLight else IOSSecondaryLabelDark

    val tertiaryLabel: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSTertiaryLabelLight else IOSTertiaryLabelDark

    val separator: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSSeparatorLight else IOSSeparatorDark

    val blue: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSBlueLight else IOSBlueDark

    /** 主题绿：打卡完成、进度、开关等品牌表达 */
    val green: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSGreenLight else IOSGreenDark

    val red: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSRedLight else IOSRedDark

    val orange: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSOrangeLight else IOSOrangeDark

    val purple: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSPurpleLight else IOSPurpleDark

    val teal: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSTealLight else IOSTealDark

    val gray: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSGrayLight else IOSGrayDark

    val switchOff: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSSwitchOffLight else IOSSwitchOffDark

    /** 进度环 / 占位轨道 */
    val track: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSGray5 else IOSTertiaryCardDark

    /** 热力图空态格 */
    val heatEmpty: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) IOSHeat0Light else IOSHeat0Dark

    /** 底栏选中指示：浅灰（去廉价感），不跟品牌蓝 */
    val tabSelect: Color
        @Composable @ReadOnlyComposable get() =
            if (isIOSLightTheme()) Color(0xFFE3E3E8) else Color(0xFF3A3A3C)
}
