package com.today.habit.ui.component

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.theme.isIOSLightTheme

// ============================================================
// 基础交互：iOS 按压反馈（缩放 / 透明度，无水波纹）
// ============================================================

/**
 * iOS 式按压反馈：按下时缩小（或变透明），松开时弹性回位。
 * 卡片、图标按钮用缩放；导航栏文字按钮用透明度。
 */
fun Modifier.iosPressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f,
    pressedAlpha: Float = 1f,
    onClick: () -> Unit
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "iosPressScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) pressedAlpha else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "iosPressAlpha"
    )
    this
        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

// ============================================================
// 分隔线：iOS hairline（0.5dp）
// ============================================================

/** iOS 分隔线，默认左缩进 16dp（与行文字对齐） */
@Composable
fun IOSDivider(indent: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent)
            .height(0.5.dp)
            .background(IOSColors.separator)
    )
}

// ============================================================
// 导航栏：iOS 小标题栏（返回 chevron + 蓝色文字按钮）
// ============================================================

/**
 * iOS 窄导航栏（48pt 内容 + 状态栏）：居中小标题 + 左侧返回 + 右侧动作。
 * 底色永远跟页面一致（elevated 时只出一条 hairline，不出现切割色块）。
 * [showTitle] 用于大标题页面联动：标题淡入淡出。
 */
@Composable
fun IOSNavBar(
    title: String,
    onBack: (() -> Unit)? = null,
    backLabel: String = "返回",
    showTitle: Boolean = true,
    elevated: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val titleAlpha by animateFloatAsState(
        targetValue = if (showTitle) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "navTitle"
    )
    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .statusBarsPadding()
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
            if (onBack != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .iosPressable(pressedScale = 1f, pressedAlpha = 0.4f, onClick = onBack)
                        .padding(start = 4.dp, end = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(SFIcons.res("chevron.left")),
                        contentDescription = backLabel,
                        tint = IOSColors.blue,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        backLabel,
                        style = IOSType.body,
                        color = IOSColors.blue,
                        maxLines = 1
                    )
                }
            }
            Text(
                title,
                style = IOSType.headline,
                color = IOSColors.label.copy(alpha = titleAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.align(Alignment.CenterEnd),
                content = actions
            )
        }
        if (elevated) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(IOSColors.separator)
            )
        }
    }
}

/** 导航栏右侧蓝色文字按钮（保存 / 添加 / 完成） */
@Composable
fun RowScope.IOSNavAction(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Text(
        label,
        style = IOSType.body,
        fontWeight = FontWeight.Normal,
        color = if (enabled) IOSColors.blue else IOSColors.tertiaryLabel,
        modifier = Modifier
            .iosPressable(enabled = enabled, pressedScale = 1f, pressedAlpha = 0.4f, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

/**
 * 列表顶部淡出：内容自身在顶部渐隐（DstIn）。
 * 滚动时由调用方把 [fade] 从 0 渐变为 88.dp；为 0 时跳过绘制，静止零开销。
 *（常驻雾罩已删除：它的底边自己就是一条线。淡出经放大验证无分界。）
 */
fun Modifier.iosTopFade(fade: Dp): Modifier = composed {
    val density = LocalDensity.current
    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()
            val fadePx = with(density) { fade.toPx() }
            if (fadePx > 1f) {
                val f = (fadePx / size.height).coerceIn(0f, 1f)
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        f to Color.Black,
                        1.0f to Color.Black
                    ),
                    blendMode = BlendMode.DstIn
                )
            }
        }
}

// ============================================================
// 大标题：随内容滚动，滚出后由导航栏小标题接管（iOS 原生行为）
// ============================================================

@Composable
fun IOSLargeTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 2.dp, bottom = 6.dp)) {
        Text(title, style = IOSType.largeTitle, color = IOSColors.label)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = IOSType.subhead, color = IOSColors.secondaryLabel)
        }
    }
}

/** 内容滚动超过阈值即认为大标题已滚出（导航栏接管小标题 + hairline） */
@Composable
fun rememberIOSCollapsed(listState: LazyListState, threshold: Dp = 48.dp): Boolean {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    return remember(listState) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > thresholdPx
        }
    }.value
}

@Composable
fun rememberIOSCollapsed(gridState: LazyGridState, threshold: Dp = 48.dp): Boolean {
    val thresholdPx = with(LocalDensity.current) { threshold.toPx() }
    return remember(gridState) {
        derivedStateOf {
            gridState.firstVisibleItemIndex > 0 ||
                gridState.firstVisibleItemScrollOffset > thresholdPx
        }
    }.value
}

// ============================================================
// 分组：iOS Grouped List（圆角卡片 + 上下说明文字）
// ============================================================

@Composable
fun IOSGroup(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        if (header != null) {
            Text(
                header,
                style = IOSType.footnote.copy(letterSpacing = 0.6.sp),
                color = IOSColors.secondaryLabel,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 6.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .iosElevatedCard(18.dp),
            content = content
        )
        if (footer != null) {
            Text(
                footer,
                style = IOSType.footnote,
                color = IOSColors.secondaryLabel,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp)
            )
        }
    }
}

/**
 * iOS 列表行：最小 44pt 高，按压灰底高亮。
 * [leading] 图标区、[trailing] 右侧（chevron / 数值 / 开关）。
 */
@Composable
fun IOSRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
    leading: (@Composable BoxScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .fillMaxWidth()
            // 行底透明，透出分组卡的渐变与高光
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(contentPadding)
        ) {
            if (leading != null) {
                Box(contentAlignment = Alignment.Center, content = leading)
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f), content = content)
            if (trailing != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, content = trailing)
            }
        }
        // iOS 行按压高亮
        if (pressed && onClick != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Gray.copy(alpha = 0.14f))
            )
        }
    }
}

/** iOS 右箭头（disclosure indicator） */
@Composable
fun IOSChevron() {
    Icon(
        painter = painterResource(SFIcons.res("chevron.right")),
        contentDescription = null,
        tint = IOSColors.tertiaryLabel,
        modifier = Modifier.size(13.dp)
    )
}

/** iOS 设置风珠宝渐变角标（29pt，同色系亮→ base →暗） */
@Composable
fun IOSSettingsIcon(iconKey: String, tint: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(29.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        lerp(tint, Color.White, 0.14f),
                        tint,
                        lerp(tint, Color.Black, 0.14f)
                    )
                )
            )
    ) {
        Icon(
            painter = painterResource(SFIcons.res(iconKey)),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(17.dp)
        )
    }
}

/** iOS 表单行内输入框（无边框，配 hairline 由调用方加分隔线） */
@Composable
fun IOSInlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    textStyle: TextStyle = IOSType.body
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = textStyle.copy(color = IOSColors.label),
        cursorBrush = SolidColor(IOSColors.blue),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(placeholder, style = textStyle, color = IOSColors.tertiaryLabel)
                }
                inner()
            }
        }
    )
}

// ============================================================
// 开关（Liquid Glass）：珠宝渐变轨道 + 镜面边 + 高光旋钮
// ============================================================

@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val light = isIOSLightTheme()
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "iosSwitch"
    )
    val base = if (checked) IOSColors.green else IOSColors.switchOff
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    0.0f to lerp(base, Color.White, if (light) 0.22f else 0.12f),
                    0.45f to base,
                    1.0f to lerp(base, Color.Black, 0.12f)
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = if (light) 0.5f else 0.25f),
                    0.55f to Color.Transparent
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onCheckedChange(!checked)
            }
            .padding(horizontal = 2.dp)
    ) {
        // 高光旋钮：纵向釉面渐变 + 柔阴影
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .shadow(3.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.White,
                        0.5f to Color.White,
                        1.0f to if (light) Color(0xFFE4E4E9) else Color(0xFFD1D1D6)
                    )
                )
        )
    }
}

// ============================================================
// UIStepper：(−|数值|+)，目标次数等小数字调节
// ============================================================

@Composable
fun IOSStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange = 1..10,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(IOSColors.tertiaryCard)
    ) {
        StepperButton("-", enabled = value > range.first) { onValueChange((value - 1).coerceIn(range)) }
        Text(
            "$value",
            style = IOSType.body,
            fontWeight = FontWeight.SemiBold,
            color = IOSColors.label,
            modifier = Modifier.width(44.dp),
            maxLines = 1,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        StepperButton("+", enabled = value < range.last) { onValueChange((value + 1).coerceIn(range)) }
    }
}

@Composable
private fun StepperButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 44.dp, height = 34.dp)
            .background(if (pressed && enabled) Color.Gray.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    onClick()
                }
            )
    ) {
        Text(
            label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) IOSColors.blue else IOSColors.tertiaryLabel
        )
    }
}

// ============================================================
// 周几选择器：iOS 日历重复规则式圆形多选
// ============================================================

/** value 用 LocalDate.dayOfWeek.value（周一=1 … 周日=7） */
@Composable
fun IOSWeekdayPicker(
    selected: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf(
        1 to "一", 2 to "二", 3 to "三", 4 to "四",
        5 to "五", 6 to "六", 7 to "日"
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (value, label) ->
            val isSel = selected.contains(value)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSel) IOSColors.blue else IOSColors.tertiaryCard)
                    .iosPressable(pressedScale = 0.9f) { onToggle(value) }
            ) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSel) Color.White else IOSColors.label
                )
            }
        }
    }
}

// ============================================================
// 进度圆环（通用）
// ============================================================

@Composable
fun IOSProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 5.dp,
    trackColor: Color = IOSColors.track,
    color: Color = IOSColors.green,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            if (progress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        content()
    }
}

// ============================================================
// UISearchBar：灰底圆角搜索框
// ============================================================

@Composable
fun IOSSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "搜索",
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(IOSColors.tertiaryCard)
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            painter = painterResource(SFIcons.res("magnifyingglass")),
            contentDescription = null,
            tint = IOSColors.gray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = IOSType.body.copy(color = IOSColors.label),
            cursorBrush = SolidColor(IOSColors.blue),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text("搜索", style = IOSType.body, color = IOSColors.secondaryLabel)
                    }
                    inner()
                }
            }
        )
        if (value.isNotEmpty()) {
            Icon(
                painter = painterResource(SFIcons.res("xmark")),
                contentDescription = "清除",
                tint = IOSColors.gray,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onValueChange("") }
                    .padding(4.dp)
            )
        }
    }
}

/** 居中操作行（iOS  destructive / action 按钮组用） */
@Composable
fun IOSActionRow(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .iosPressable(onClick = onClick)
            .padding(vertical = 13.dp)
            .defaultMinSize(minHeight = 22.dp)
    ) {
        Text(label, style = IOSType.body, color = color)
    }
}

// ============================================================
// 高级材质：柔阴影 + 顶部高光卡片
// ============================================================

/** Liquid Glass 卡片：半透明玻璃底（底衬雾色透上来）+ 顶部釉光 + 上亮下隐的玻璃边缘光 + 柔深阴影 */
@Composable
fun Modifier.iosElevatedCard(radius: Dp = 22.dp): Modifier {
    val light = isIOSLightTheme()
    // 玻璃基底：浅色白 0.60 / 深色白 0.08，雾色从后面透上来
    val top = if (light) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.15f)
    val base = if (light) Color.White.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.08f)
    // 边缘光：顶部亮、向下隐去（玻璃反光边）
    val edge = Brush.verticalGradient(
        0.0f to Color.White.copy(alpha = if (light) 0.55f else 0.22f),
        0.35f to Color.White.copy(alpha = 0.0f),
        1.0f to Color.Transparent
    )
    val shadowCol = if (light) Color(0x24000000) else Color(0x70000000)
    return this
        .shadow(24.dp, RoundedCornerShape(radius), spotColor = shadowCol, ambientColor = shadowCol)
        .clip(RoundedCornerShape(radius))
        .background(
            Brush.verticalGradient(0.0f to top, 0.35f to base, 1.0f to base)
        )
        .border(1.dp, edge, RoundedCornerShape(radius))
}

/** 通用字形角标：淡 tint 渐变底 + 镜面边 + 字形（习惯用墨色） */
@Composable
fun IOSGlyphTile(
    iconKey: String,
    tint: Color,
    size: Dp = 38.dp,
    radius: Dp = 12.dp,
    glyphSize: Dp = 21.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(radius))
            .background(
                Brush.verticalGradient(
                    listOf(tint.copy(alpha = 0.18f), tint.copy(alpha = 0.06f))
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = 0.35f),
                    0.5f to Color.Transparent
                ),
                RoundedCornerShape(radius)
            )
    ) {
        Icon(
            painter = painterResource(SFIcons.res(iconKey)),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(glyphSize)
        )
    }
}

/** 页面顶部环境微光——已删除：真机上会显形成顶部色块，造成割裂感。surfaces 保持中性，品牌色只出现在语义元素（进度/完成/选中）上。 */

// ============================================================
// 入场编排：首见淡入上浮（每 key 终身一次，切 Tab 不重播）
// ============================================================

private object EntranceGate {
    private const val MAX_KEYS = 500
    private val seen = LinkedHashSet<String>()
    /** 已见过返回 true；首见登记并返回 false */
    fun check(key: String): Boolean {
        if (seen.contains(key)) return true
        if (seen.size >= MAX_KEYS) seen.clear()
        seen.add(key)
        return false
    }
}

private val EnterEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** 列表入场：淡入 + 上浮，首见播放一次 */
fun Modifier.iosEntrance(key: String, index: Int, stepMs: Int = 45, baseMs: Int = 70): Modifier = composed {
    val seen = remember(key) { EntranceGate.check(key) }
    var started by remember(key) { mutableStateOf(seen) }
    LaunchedEffect(key) { if (!seen) started = true }
    val delay = if (seen) 0 else baseMs + index * stepMs
    val alpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 380, delayMillis = delay, easing = EnterEasing),
        label = "enterAlpha"
    )
    val rise by animateDpAsState(
        targetValue = if (started) 0.dp else 16.dp,
        animationSpec = tween(durationMillis = 430, delayMillis = delay, easing = EnterEasing),
        label = "enterRise"
    )
    this.graphicsLayer(
        alpha = alpha,
        translationY = with(LocalDensity.current) { rise.toPx() }
    )
}
