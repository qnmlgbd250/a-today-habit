package com.today.habit.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.today.habit.ui.theme.ThemeGreen

// ---------------------------------------------------------------------------
// 液态玻璃设计系统（iOS 27 风格）
// 注意：页面内容本身被录制为液态玻璃底栏的取样源，直接在卡片上使用
// drawBackdrop 会形成自引用导致闪退，因此卡片采用"仿玻璃"实现：
// 半透明表面 + 白色高光描边 + 顶部镜面高光 + 柔和阴影。
// 只有悬浮底栏使用真正的 backdrop 取样。
// ---------------------------------------------------------------------------

/** 页面顶部极光背景：给液态玻璃提供折射素材，同时营造旗舰质感 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier, dark: Boolean = false) {
    Box(modifier = modifier.fillMaxWidth().height(340.dp)) {
        val a1 = if (dark) Color(0xFF30D158).copy(alpha = 0.22f) else Color(0xFF7DFFA8).copy(alpha = 0.55f)
        val a2 = if (dark) Color(0xFF0A84FF).copy(alpha = 0.20f) else Color(0xFFA8DCFF).copy(alpha = 0.6f)
        val a3 = if (dark) Color(0xFFFF9F0A).copy(alpha = 0.14f) else Color(0xFFFFE3A8).copy(alpha = 0.7f)
        Box(
            modifier = Modifier.size(260.dp).align(Alignment.TopStart).padding(start = 8.dp)
                .blur(70.dp).background(a1, CircleShape)
        )
        Box(
            modifier = Modifier.size(300.dp).align(Alignment.TopEnd).padding(top = 20.dp)
                .blur(80.dp).background(a2, CircleShape)
        )
        Box(
            modifier = Modifier.size(180.dp).align(Alignment.TopCenter).padding(top = 90.dp)
                .blur(60.dp).background(a3, CircleShape)
        )
    }
}

/** 液态玻璃卡片：半透明 + 高光边框 + 顶部镜面反光 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 26.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val isDark = MaterialTheme.colorScheme.background.luminanceLow()
    Column(
        modifier = modifier
            .shadow(14.dp, RoundedCornerShape(corner), spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(RoundedCornerShape(corner))
            .background(surface.copy(alpha = if (isDark) 0.72f else 0.78f))
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.22f else 0.9f),
                        Color.White.copy(alpha = 0.06f)
                    )
                ),
                RoundedCornerShape(corner)
            )
            .padding(18.dp),
        content = content
    )
}

/** 分组标题行 */
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        action?.invoke()
    }
}

/** 大标题页眉：iOS Large Title 风格 */
@Composable
fun LargeTitleHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(2.dp))
            Text(
                title,
                fontSize = 33.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (trailing != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                trailing()
            }
        }
    }
}

/** 圆形玻璃图标按钮 */
@Composable
fun GlassIconButton(iconKey: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val surface = MaterialTheme.colorScheme.surface
    val isDark = MaterialTheme.colorScheme.background.luminanceLow()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.12f))
            .clip(CircleShape)
            .background(surface.copy(alpha = if (isDark) 0.7f else 0.8f))
            .border(0.75.dp, Color.White.copy(alpha = if (isDark) 0.2f else 0.8f), CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
    ) {
        Icon(
            painter = painterResource(SFIcons.res(iconKey)),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(19.dp)
        )
    }
}

/** 渐变图标瓷砖：每个习惯的视觉锚点 */
@Composable
fun HabitTile(iconRes: Int, accent: HabitAccent, modifier: Modifier = Modifier, size: Dp = 52.dp, iconSize: Dp = 25.dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(accent.main, accent.gradientEnd)))
            .border(0.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
    ) {
        // 镜面高光
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(size / 2)
                .background(
                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent)),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
        )
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

/** 今日进度大圆环 */
@Composable
fun HeroRing(progress: Float, modifier: Modifier = Modifier, size: Dp = 96.dp, stroke: Dp = 10.dp) {
    val anim = animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "hero")
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            drawArc(track, 0f, 360f, false, style = Stroke(stroke.toPx(), cap = StrokeCap.Round))
            if (anim.value > 0.001f) {
                drawArc(
                    Brush.sweepGradient(listOf(ThemeGreen, Color(0xFF7DFFA8), ThemeGreen)),
                    -90f, 360f * anim.value, false,
                    style = Stroke(stroke.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(anim.value * 100).toInt()}%",
                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

/** 打卡圆环按钮：点按完成一次 */
@Composable
fun CheckRingButton(
    count: Int,
    target: Int,
    accent: HabitAccent,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val done = count >= target
    val progress = if (target <= 0) 0f else (count.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val anim = animateFloatAsState(progress, tween(450), label = "check")
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(if (done) accent.main else accent.soft)
            .border(1.dp, if (done) Color.White.copy(alpha = 0.4f) else accent.main.copy(alpha = 0.35f), CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(46.dp)) {
            if (!done && anim.value > 0f) {
                drawArc(
                    accent.main, -90f, 360f * anim.value, false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        if (done) {
            Icon(
                painter = painterResource(SFIcons.res("checkmark")),
                contentDescription = "已完成",
                tint = Color.White,
                modifier = Modifier.size(19.dp)
            )
        } else {
            Text(
                if (target <= 1) "" else "$count/$target",
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accent.main
            )
            if (target <= 1) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(accent.main.copy(alpha = 0.55f)))
            }
        }
    }
}

/** KPI 小卡片 */
@Composable
fun KpiCard(iconKey: String, value: String, label: String, accent: HabitAccent, modifier: Modifier = Modifier) {
    val surface = MaterialTheme.colorScheme.surface
    val isDark = MaterialTheme.colorScheme.background.luminanceLow()
    Column(
        modifier = modifier
            .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(22.dp))
            .background(surface.copy(alpha = if (isDark) 0.72f else 0.8f))
            .border(1.dp, Color.White.copy(alpha = if (isDark) 0.16f else 0.7f), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(32.dp).clip(CircleShape).background(accent.soft)
        ) {
            Icon(
                painter = painterResource(SFIcons.res(iconKey)), contentDescription = null,
                tint = accent.main, modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Color.luminanceLow(): Boolean {
    val l = 0.2126f * red + 0.7152f * green + 0.0722f * blue
    return l < 0.5f
}
