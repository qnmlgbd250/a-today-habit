package com.today.habit.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType

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
 * iOS 导航栏：居中小标题（17sp semibold）+ 左侧返回 + 右侧动作。
 * [showTitle] 用于大标题页面联动：内容未滚动时隐藏标题只留底色。
 * [elevated] 为 true 时显示卡片底 + hairline（内容已滚动）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOSNavBar(
    title: String,
    onBack: (() -> Unit)? = null,
    backLabel: String = "返回",
    showTitle: Boolean = true,
    elevated: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        TopAppBar(
            title = {
                if (showTitle) {
                    Text(
                        title,
                        style = IOSType.headline,
                        color = IOSColors.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .iosPressable(pressedScale = 1f, pressedAlpha = 0.4f, onClick = onBack)
                            .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
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
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (elevated) IOSColors.card else IOSColors.background,
                titleContentColor = IOSColors.label,
                actionIconContentColor = IOSColors.blue
            )
        )
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
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
    Column(modifier = modifier.padding(top = 4.dp, bottom = 10.dp)) {
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
                style = IOSType.footnote,
                color = IOSColors.secondaryLabel,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 6.dp)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(IOSColors.card),
            content = content
        )
        if (footer != null) {
            Text(
                footer,
                style = IOSType.footnote,
                color = IOSColors.secondaryLabel,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp)
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
            .background(IOSColors.card)
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

/** iOS 设置风彩色圆角图标（29pt） */
@Composable
fun IOSSettingsIcon(iconKey: String, background: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(29.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(background)
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
// UISwitch：51x31，绿开 / 灰关，白色阴影旋钮
// ============================================================

@Composable
fun IOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "iosSwitch"
    )
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .size(width = 51.dp, height = 31.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (checked) IOSColors.green else IOSColors.switchOff)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(27.dp)
                .shadow(2.dp, CircleShape, clip = false)
                .background(Color.White, CircleShape)
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
                onClick = onClick
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
            .background(IOSColors.card)
            .iosPressable(onClick = onClick)
            .padding(vertical = 13.dp)
            .defaultMinSize(minHeight = 22.dp)
    ) {
        Text(label, style = IOSType.body, color = color)
    }
}
