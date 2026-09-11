package com.today.habit.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 壁纸系统：APP 签名底衬 + 用户可换壁纸。
 * 液态玻璃卡片需要有色彩的底衬才成立——底衬是全局的，一处绘制、处处透出。
 */
data class WallpaperDef(
    val id: String,
    val name: String,
    /** 浅色主题三团雾（右上 / 左中 / 底部） */
    val washLight: List<Color>,
    /** 深色主题三团雾 */
    val washDark: List<Color>
)

val Wallpapers = listOf(
    WallpaperDef(
        id = "aurora", name = "默认极光",
        washLight = listOf(
            IOSGreenLight.copy(alpha = 0.20f),
            IOSTealLight.copy(alpha = 0.15f),
            IOSBlueLight.copy(alpha = 0.13f)
        ),
        washDark = listOf(
            IOSGreenDark.copy(alpha = 0.22f),
            IOSTealDark.copy(alpha = 0.17f),
            IOSBlueDark.copy(alpha = 0.15f)
        )
    ),
    WallpaperDef(
        id = "ocean", name = "深海蓝",
        washLight = listOf(
            IOSBlueLight.copy(alpha = 0.22f),
            IOSTealLight.copy(alpha = 0.15f),
            IOSGreenLight.copy(alpha = 0.10f)
        ),
        washDark = listOf(
            IOSBlueDark.copy(alpha = 0.25f),
            IOSTealDark.copy(alpha = 0.17f),
            IOSGreenDark.copy(alpha = 0.10f)
        )
    ),
    WallpaperDef(
        id = "sunset", name = "落日橙",
        washLight = listOf(
            IOSOrangeLight.copy(alpha = 0.22f),
            IOSRedLight.copy(alpha = 0.13f),
            IOSPurpleLight.copy(alpha = 0.11f)
        ),
        washDark = listOf(
            IOSOrangeDark.copy(alpha = 0.24f),
            IOSRedDark.copy(alpha = 0.15f),
            IOSPurpleDark.copy(alpha = 0.13f)
        )
    ),
    WallpaperDef(
        id = "neon", name = "霓虹紫",
        washLight = listOf(
            IOSPurpleLight.copy(alpha = 0.20f),
            IOSBlueLight.copy(alpha = 0.15f),
            IOSTealLight.copy(alpha = 0.11f)
        ),
        washDark = listOf(
            IOSPurpleDark.copy(alpha = 0.26f),
            IOSBlueDark.copy(alpha = 0.19f),
            IOSTealDark.copy(alpha = 0.13f)
        )
    ),
    // 纯净：无雾，老 UI 味道的保底选项
    WallpaperDef(id = "pure", name = "纯净", washLight = emptyList(), washDark = emptyList())
)

@Composable
@ReadOnlyComposable
fun wallpaperDef(id: String): WallpaperDef =
    Wallpapers.find { it.id == id } ?: Wallpapers[0]

/** 全局底衬：基底色 + 三团极光雾（静态绘制一次，滚列表不跟着动） */
@Composable
fun WallpaperBackground(id: String, modifier: Modifier = Modifier) {
    val light = isIOSLightTheme()
    val def = wallpaperDef(id)
    val washes = remember(def, light) {
        if (light) def.washLight else def.washDark
    }
    Box(modifier = modifier.background(IOSColors.background)) {
        if (washes.size >= 3) {
            WashCircle(washes[0], 420.dp, Alignment.TopEnd, (-70).dp, (-130).dp)
            WashCircle(washes[1], 340.dp, Alignment.CenterStart, (-110).dp, (-40).dp)
            WashCircle(washes[2], 460.dp, Alignment.BottomCenter, 0.dp, 150.dp)
        }
    }
}

@Composable
private fun BoxScope.WashCircle(color: Color, size: Dp, align: Alignment, dx: Dp, dy: Dp) {
    Box(
        modifier = Modifier
            .align(align)
            .offset(dx, dy)
            .size(size)
            .background(
                Brush.radialGradient(0.0f to color, 1.0f to Color.Transparent)
            )
    )
}

/** 壁纸选择器预览小图：三色纵向渐变近似实际雾效 */
@Composable
fun WallpaperPreview(id: String, modifier: Modifier = Modifier) {
    val light = isIOSLightTheme()
    val def = wallpaperDef(id)
    val preview: Brush = remember(def, light) {
        val washes = if (light) def.washLight else def.washDark
        if (washes.size >= 3) Brush.verticalGradient(washes)
        else Brush.linearGradient(
            0.0f to if (light) IOSGroupedLight else IOSGroupedDark,
            1.0f to if (light) IOSGroupedLight else IOSGroupedDark
        )
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(preview)
    )
}
