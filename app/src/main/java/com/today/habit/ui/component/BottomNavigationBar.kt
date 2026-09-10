package com.today.habit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.today.habit.ui.theme.IOSColors

private val GlassShape = RoundedCornerShape(30.dp)

/**
 * iOS 26 Liquid Glass 悬浮底栏：液态玻璃（折射 +  vibrancy + 模糊）+
 * 顶部镜面高光 + 选中项柔光底。
 * 注意：必须位于 NavHost 录制图层之外（MainApp 的 Box 上层），否则自引用闪退。
 */
@Composable
fun GlassBottomNavigationBar(
    navController: NavController,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavigationItem("home", "今日", "house"),
        NavigationItem("stats", "统计", "chart.bar"),
        NavigationItem("settings", "设置", "gearshape")
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val surfaceColor = IOSColors.card
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .padding(horizontal = 20.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { GlassShape },
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(20f.dp.toPx(), 40f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(surfaceColor.copy(alpha = 0.55f))
                }
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.28f), GlassShape)
            .fillMaxWidth()
    ) {
        // 顶部镜面高光：玻璃质感的关键
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(GlassShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.32f),
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        endY = 120f
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) IOSColors.blue.copy(alpha = 0.16f)
                            else Color.Transparent
                        )
                        .iosPressable(pressedScale = 0.9f) {
                            if (!isSelected) {
                                haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            }
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(SFIcons.res(item.iconKey)),
                        contentDescription = item.title,
                        tint = if (isSelected) IOSColors.blue else IOSColors.gray,
                        modifier = Modifier.size(23.dp)
                    )
                    Text(
                        item.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) IOSColors.blue else IOSColors.gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class NavigationItem(val route: String, val title: String, val iconKey: String)
