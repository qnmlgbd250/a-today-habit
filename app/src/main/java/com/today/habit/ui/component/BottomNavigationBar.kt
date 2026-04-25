package com.today.habit.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItem("home", "今日"),
        NavigationItem("stats", "统计")
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { 
                    CustomBottomIcon(
                        name = item.route, 
                        isSelected = isSelected,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                label = { Text(item.title) },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent
                ),
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
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
