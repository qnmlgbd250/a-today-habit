package com.today.habit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

// ---------- iOS 式排印体系：紧凑字距 + 明确层级，数字用等宽 ----------
// 大标题：34pt ExtraBold，字距 -0.5（iOS Large Title 质感）
val DisplayTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 34.sp,
    letterSpacing = (-0.5).sp
)
// 分组内标题：17pt Semibold
val Headline17 = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    letterSpacing = (-0.2).sp
)
// 正文：17pt Regular
val Body17 = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp
)
// 次级：15pt
val Subhead15 = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp
)
// 脚注：13pt（分组说明、次要信息，配 secondary 颜色）
val Footnote13 = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp
)
// 等宽数字：统计、百分比、连击（tnum 防止数字跳动）
val TabularNum = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 22.sp,
    fontFeatureSettings = "tnum"
)
