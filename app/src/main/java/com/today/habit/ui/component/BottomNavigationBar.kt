package com.today.habit.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Composable
fun GlassBottomNavigationBar(
    navController: NavController,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem("home", "今日"),
        NavigationItem("stats", "统计")
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val surfaceColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .height(68.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(50) },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(surfaceColor.copy(alpha = 0.5f))
                }
            )
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val tint = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
            ) {
                CustomBottomIcon(name = item.route, isSelected = isSelected, color = tint)
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = tint,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CustomBottomIcon(name: String, isSelected: Boolean, color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val sizeVal = size.minDimension
        val strokeWidth = 1.8.dp.toPx()

        when (name) {
            "home" -> {
                // 手绘风格的日历/今日图标
                val path = Path().apply {
                    // 外框
                    moveTo(sizeVal * 0.2f, sizeVal * 0.3f)
                    lineTo(sizeVal * 0.8f, sizeVal * 0.3f)
                    lineTo(sizeVal * 0.8f, sizeVal * 0.9f)
                    lineTo(sizeVal * 0.2f, sizeVal * 0.9f)
                    close()
                    // 顶部两个小挂钩
                    moveTo(sizeVal * 0.35f, sizeVal * 0.15f)
                    lineTo(sizeVal * 0.35f, sizeVal * 0.35f)
                    moveTo(sizeVal * 0.65f, sizeVal * 0.15f)
                    lineTo(sizeVal * 0.65f, sizeVal * 0.35f)
                }
                drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                if (isSelected) {
                    // 选中状态画一个小点表示“今日”
                    drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(sizeVal * 0.5f, sizeVal * 0.65f))
                }
            }
            "stats" -> {
                // 手绘风格的折线/统计图标
                val path = Path().apply {
                    moveTo(sizeVal * 0.15f, sizeVal * 0.85f)
                    lineTo(sizeVal * 0.85f, sizeVal * 0.85f) // 底轴

                    moveTo(sizeVal * 0.2f, sizeVal * 0.7f)
                    lineTo(sizeVal * 0.45f, sizeVal * 0.35f)
                    lineTo(sizeVal * 0.65f, sizeVal * 0.55f)
                    lineTo(sizeVal * 0.85f, sizeVal * 0.25f)
                }
                drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                if (isSelected) {
                    // 选中状态在转折点画小圆点
                    drawCircle(color = color, radius = 1.5.dp.toPx(), center = Offset(sizeVal * 0.45f, sizeVal * 0.35f))
                    drawCircle(color = color, radius = 1.5.dp.toPx(), center = Offset(sizeVal * 0.85f, sizeVal * 0.25f))
                }
            }
        }
    }
}

data class NavigationItem(val route: String, val title: String)
