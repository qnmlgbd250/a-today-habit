package com.today.habit.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.today.habit.ui.theme.DisplayTitle
import com.today.habit.ui.theme.Footnote13
import com.today.habit.ui.theme.TabularNum
import com.today.habit.ui.theme.ThemeGreen
import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// 高级感设计系统：克制、纯平、发丝线。
// 铁律：
// 1. 禁止渐变填充（品牌绿点缀除外的小面积 tint 也不用渐变）；
// 2. 禁止彩色阴影与重阴影，深色下零阴影；
// 3. 分层靠 0.5dp 发丝线 + 通透度，不靠描边粗线；
// 4. 选中态优先黑白，彩色只表示进度/状态。
// （页面内容被录制为液态玻璃底栏取样源，卡片仍用仿玻璃实现，避免自引用闪退。）
// ---------------------------------------------------------------------------

@Composable
fun isDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** 页面顶部氛围光：极淡，为液态玻璃底栏提供折射素材 */
@Composable
fun AuroraBackground(modifier: Modifier = Modifier, dark: Boolean = false) {
    Box(modifier = modifier.fillMaxWidth().height(300.dp)) {
        val a1 = if (dark) Color(0xFF30D158).copy(alpha = 0.10f) else Color(0xFF7DFFA8).copy(alpha = 0.28f)
        val a2 = if (dark) Color(0xFF0A84FF).copy(alpha = 0.08f) else Color(0xFFA8DCFF).copy(alpha = 0.30f)
        val a3 = if (dark) Color(0xFFFF9F0A).copy(alpha = 0.06f) else Color(0xFFFFE3A8).copy(alpha = 0.32f)
        Box(Modifier.size(240.dp).align(Alignment.TopStart).padding(start = 8.dp).blur(80.dp).background(a1, CircleShape))
        Box(Modifier.size(280.dp).align(Alignment.TopEnd).padding(top = 20.dp).blur(90.dp).background(a2, CircleShape))
        Box(Modifier.size(160.dp).align(Alignment.TopCenter).padding(top = 90.dp).blur(70.dp).background(a3, CircleShape))
    }
}

/** 分组容器铬：实色 + 发丝线 + 极淡阴影（深色零阴影） */
private fun Modifier.groupChrome(corner: Dp, dark: Boolean, surface: Color): Modifier {
    val base = this
        .clip(RoundedCornerShape(corner))
        .background(surface)
        .border(
            0.5.dp,
            if (dark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f),
            RoundedCornerShape(corner)
        )
    return if (dark) base else base.shadow(8.dp, RoundedCornerShape(corner), spotColor = Color.Black.copy(alpha = 0.06f))
}

/** 独立玻璃卡片（带内边距） */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isDark()
    Column(
        modifier = modifier
            .groupChrome(corner, dark, MaterialTheme.colorScheme.surface)
            .padding(18.dp),
        content = content
    )
}

/** 内嵌分组（行自己管内边距，用于 iOS 设置式列表） */
@Composable
fun InsetGroup(
    modifier: Modifier = Modifier,
    corner: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val dark = isDark()
    Column(
        modifier = modifier.groupChrome(corner, dark, MaterialTheme.colorScheme.surface),
        content = content
    )
}

/** 发丝分隔线（iOS separator，带左缩进） */
@Composable
fun HairlineDivider(startIndent: Dp = 0.dp, endIndent: Dp = 0.dp, modifier: Modifier = Modifier) {
    val dark = isDark()
    Box(
        modifier
            .fillMaxWidth()
            .padding(start = startIndent, end = endIndent)
            .height(0.5.dp)
            .background((if (dark) Color.White else Color.Black).copy(alpha = if (dark) 0.14f else 0.08f))
    )
}

/** 分组小标题：13pt 次级色（iOS 分组表头语气） */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = Footnote13,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 6.dp)
    )
}

/** 分组脚注：13pt 次级色说明文字 */
@Composable
fun SectionFooter(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = Footnote13,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 6.dp)
    )
}

/** 分组标题行（大分组用） */
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

/** 大标题页眉：iOS Large Title */
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
            Text(subtitle, style = Footnote13, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(title, style = DisplayTitle, color = MaterialTheme.colorScheme.onBackground)
        }
        if (trailing != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                trailing()
            }
        }
    }
}

/** 按压缩放点击：iOS 式触感反馈（替代水波纹） */
fun Modifier.pressable(
    scaleTo: Float = 0.97f,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) scaleTo else 1f, tween(140), label = "press")
    this
        .graphicsLayer(scaleX = scale, scaleY = scale)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/** 列表 stagger 入场：淡入 + 上浮，首次出现依次延迟 */
fun Modifier.staggerIn(index: Int, stepMs: Int = 45): Modifier = composed {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((60 + index * stepMs).toLong())
        shown = true
    }
    val alpha by animateFloatAsState(if (shown) 1f else 0f, tween(380), label = "staggerA")
    val dy by animateFloatAsState(if (shown) 0f else 16f, tween(380, easing = FastOutSlowInEasing), label = "staggerY")
    this.graphicsLayer(alpha = alpha, translationY = dy)
}

/** 圆形工具按钮：半透明 + 发丝线 */
@Composable
fun GlassIconButton(iconKey: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val dark = isDark()
    val surface = MaterialTheme.colorScheme.surface
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(surface.copy(alpha = if (dark) 0.85f else 0.9f))
            .border(
                0.5.dp,
                if (dark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f),
                CircleShape
            )
            .pressable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(SFIcons.res(iconKey)),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(18.dp)
        )
    }
}

/** 纯平图标瓷砖：iOS 设置式，纯色 + 白字形，零渐变 */
@Composable
fun HabitTile(iconRes: Int, accent: HabitAccent, modifier: Modifier = Modifier, size: Dp = 46.dp, iconSize: Dp = 22.dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.main)
    ) {
        Icon(painter = painterResource(iconRes), contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

/** 细圆环（纯色）：只画弧，内容由调用方叠加 */
@Composable
fun HeroRing(progress: Float, modifier: Modifier = Modifier, size: Dp = 64.dp, stroke: Dp = 6.dp, color: Color = ThemeGreen) {
    val anim = animateFloatAsState(progress.coerceIn(0f, 1f), tween(700), label = "hero")
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    Canvas(modifier.size(size)) {
        drawArc(track, 0f, 360f, false, style = Stroke(stroke.toPx(), cap = StrokeCap.Round))
        if (anim.value > 0.001f) {
            drawArc(color, -90f, 360f * anim.value, false, style = Stroke(stroke.toPx(), cap = StrokeCap.Round))
        }
    }
}

/** 打卡圆环键：完成 = 纯色填充 + 白勾；未完成 = 发丝圆环 + 计数 */
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
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (done) accent.main else Color.Transparent)
            .border(
                1.5.dp,
                if (done) accent.main else accent.main.copy(alpha = 0.45f),
                CircleShape
            )
            .pressable(onClick = onClick, scaleTo = 0.9f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(38.dp)) {
            if (!done && anim.value > 0.001f && anim.value < 0.999f) {
                drawArc(
                    accent.main, -90f, 360f * anim.value, false,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        if (done) {
            Icon(
                painter = painterResource(SFIcons.res("checkmark")),
                contentDescription = "已完成",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else if (target > 1) {
            Text("${count}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent.main)
        } else {
            Box(Modifier.size(7.dp).clip(CircleShape).background(accent.main.copy(alpha = 0.6f)))
        }
    }
}

/** 统计数字块：等宽大数字 + 脚注（Screen Time 式） */
@Composable
fun StatBlock(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = TabularNum, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(2.dp))
        Text(label, style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
