package com.today.habit.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.theme.isIOSLightTheme

/** iOS 风格填充式输入框：无描边、圆角灰底 */
@Composable
fun IOSFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val container = IOSColors.tertiaryCard
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = IOSColors.tertiaryLabel) },
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(10.dp),
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            disabledContainerColor = container,
            cursorColor = IOSColors.blue,
            focusedTextColor = IOSColors.label,
            unfocusedTextColor = IOSColors.label
        )
    )
}

/** iOS 26 液态玻璃分段控制器：磨砂轨道 + 滑动玻璃滑块 + 镜面描边 */
@Composable
fun IOSSegmentedControl(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val light = isIOSLightTheme()
    val index = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
    val track = IOSColors.tertiaryCard
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    0.0f to lerp(track, Color.White, if (light) 0.35f else 0.08f),
                    1.0f to track
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = if (light) 0.5f else 0.18f),
                    0.5f to Color.Transparent
                ),
                RoundedCornerShape(12.dp)
            )
            .padding(2.dp)
    ) {
        val segW = maxWidth / options.size
        // 选中滑块：弹性滑动（iOS 26 同款跟手感）
        val thumbX by animateDpAsState(
            targetValue = segW * index,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = 0.82f
            ),
            label = "segThumb"
        )
        // 玻璃滑块（底层）
        Box(
            modifier = Modifier
                .offset(x = thumbX)
                .width(segW)
                .fillMaxHeight()
                .shadow(3.dp, RoundedCornerShape(10.dp), clip = false)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        0.0f to lerp(IOSColors.card, Color.White, if (light) 0.35f else 0.14f),
                        1.0f to IOSColors.card
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = if (light) 0.7f else 0.3f),
                        0.6f to Color.Transparent
                    ),
                    RoundedCornerShape(10.dp)
                )
        )
        // 文字层（顶层，只负责点击）
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEach { (id, label) ->
                val isSel = selected == id
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelect(id) }
                        .padding(vertical = 7.dp)
                ) {
                    Text(
                        label,
                        fontSize = 13.sp,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSel) IOSColors.label else IOSColors.secondaryLabel,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** iOS 风格滑块：细轨道 + 白色大圆钮 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = IOSColors.green,
            inactiveTrackColor = IOSColors.track
        ),
        thumb = {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .shadow(2.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .border(0.5.dp, Color.Black.copy(alpha = 0.08f), CircleShape)
            )
        },
        track = { state ->
            val fraction = if (valueRange.endInclusive > valueRange.start) {
                ((state.value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
            } else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(IOSColors.track)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(IOSColors.green)
                )
            }
        }
    )
}

/** iOS 风格筛选胶囊：选中蓝底白字 */
@Composable
fun IOSPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) IOSColors.blue else IOSColors.tertiaryCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else IOSColors.label,
            maxLines = 1
        )
    }
}

/** iOS 风格 HUD 提示（居中深色圆角胶囊） */
@Composable
fun IOSToast(message: String?, modifier: Modifier = Modifier) {
    if (message != null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .background(Color(0xCC1C1C1E), RoundedCornerShape(20.dp))
                    .padding(horizontal = 22.dp, vertical = 13.dp)
            )
        }
    }
}

/**
 * iOS 下拉菜单卡片（圆角 12 + 阴影 + hairline 边框）。
 * 注意：不能用 drawBackdrop 取样 NavHost 背景——菜单位于被录制图层内部会形成自引用循环，
 * 触发 RenderThread SIGSEGV 闪退（backdrop 库已知问题）。
 */
@Composable
fun IOSMenuCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(12.dp), spotColor = Color.Black.copy(alpha = 0.25f))
            .background(IOSColors.card.copy(alpha = 0.97f), RoundedCornerShape(12.dp))
            .border(0.5.dp, IOSColors.separator, RoundedCornerShape(12.dp))
            .width(200.dp)
            .padding(vertical = 5.dp),
        content = content
    )
}

/** iOS 菜单项：SF 图标 + 文字 */
@Composable
fun IOSMenuItem(
    iconKey: String,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Icon(
            painter = painterResource(SFIcons.res(iconKey)),
            contentDescription = null,
            tint = IOSColors.blue,
            modifier = Modifier.size(18.dp)
        )
        Text(
            label,
            style = IOSType.body,
            color = IOSColors.label
        )
    }
}

/** iOS 菜单分隔线（左对齐文字缩进） */
@Composable
fun IOSMenuDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 42.dp),
        thickness = 0.5.dp,
        color = IOSColors.separator
    )
}
