package com.today.habit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.today.habit.ui.theme.ThemeGreen

/** iOS 风格填充式输入框：无描边、圆角灰底，聚焦光标为主题色 */
@Composable
fun IOSFormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val container = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
        leadingIcon = leadingIcon,
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedContainerColor = container,
            unfocusedContainerColor = container,
            disabledContainerColor = container,
            cursorColor = ThemeGreen,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/** iOS 分段控制器（UISegmentedControl）：灰色轨道 + 白色选中段 */
@Composable
fun IOSSegmentedControl(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(track)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        options.forEach { (id, label) ->
            val isSel = selected == id
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .border(
                        0.5.dp,
                        if (isSel) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(id) }
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isSel) FontWeight.Medium else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
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
            activeTrackColor = ThemeGreen,
            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
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
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .background(ThemeGreen)
                )
            }
        }
    )
}

/** iOS 风格筛选胶囊（分类标签） */
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
            .background(
                if (selected) ThemeGreen
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

/** iOS 风格 HUD 提示（居中深色圆角胶囊，替代系统 Toast） */
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
 * iOS 拉下式菜单卡片，替代 Material DropdownMenu。
 * 注意：不能用 drawBackdrop 取样 NavHost 背景——菜单位于被录制图层内部会形成自引用循环，
 * 触发 RenderThread SIGSEGV 闪退（backdrop 库已知问题），故用带阴影的半透明表面实现。
 */
@Composable
fun IOSMenuCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(14.dp), spotColor = Color.Black.copy(alpha = 0.25f))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f), RoundedCornerShape(14.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .width(224.dp)
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
            tint = ThemeGreen,
            modifier = Modifier.size(18.dp)
        )
        Text(
            label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** iOS 菜单分隔线（左对齐文字缩进） */
@Composable
fun IOSMenuDivider() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(start = 42.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
}
