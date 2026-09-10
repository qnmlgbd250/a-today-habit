package com.today.habit.ui.theme

import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = IOSBlueDark,
    onPrimary = Color.White,
    secondary = IOSGreenDark,
    background = IOSGroupedDark,
    surface = IOSCardDark,
    surfaceVariant = IOSTertiaryCardDark,
    onBackground = IOSLabelDark,
    onSurface = IOSLabelDark,
    onSurfaceVariant = IOSSecondaryLabelDark,
    outline = IOSSeparatorDark,
    error = IOSRedDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = IOSBlueLight,
    onPrimary = Color.White,
    secondary = IOSGreenLight,
    background = IOSGroupedLight,
    surface = IOSCardLight,
    surfaceVariant = IOSGray6,
    onBackground = IOSLabelLight,
    onSurface = IOSLabelLight,
    onSurfaceVariant = IOSSecondaryLabelLight,
    outline = IOSSeparatorLight,
    error = IOSRedLight,
    onError = Color.White
)

@Composable
fun ConstantTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // iOS 式全屏沉浸：状态栏 / 手势条图标随主题反色
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        // 去 Android 化：全局关闭 Material 水波纹（iOS 用按压缩放 / 灰底高亮反馈）
        CompositionLocalProvider(LocalIndication provides NoIndication) {
            content()
        }
    }
}

/** 空交互反馈实现（无任何视觉回显），替代 Material 水波纹 */
private object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): Modifier.Node = EmptyNode()
    override fun equals(other: Any?): Boolean = other === this
    override fun hashCode(): Int = -1
}

private class EmptyNode : Modifier.Node()
