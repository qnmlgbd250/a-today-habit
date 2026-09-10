package com.today.habit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType

/**
 * iOS UITabBar：全宽底部毛玻璃 + 顶部分隔线，图标 + 小字标签。
 * 选中蓝色、未选中灰色（iOS 标准配色，无选中底）。
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RectangleShape },
                effects = {
                    vibrancy()
                    blur(8f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(surfaceColor.copy(alpha = 0.82f))
                }
            )
    ) {
        // TabBar 顶部分隔线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(IOSColors.separator)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .weight(1f)
                        .iosPressable(pressedScale = 0.92f) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                        .padding(vertical = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(SFIcons.res(item.iconKey)),
                        contentDescription = item.title,
                        tint = if (isSelected) IOSColors.blue else IOSColors.gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        item.title,
                        style = IOSType.tabLabel,
                        color = if (isSelected) IOSColors.blue else IOSColors.gray,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

data class NavigationItem(val route: String, val title: String, val iconKey: String)
