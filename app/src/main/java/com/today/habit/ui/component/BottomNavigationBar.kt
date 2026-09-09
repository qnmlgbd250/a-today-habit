package com.today.habit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.today.habit.ui.theme.ThemeGreen

/**
 * 悬浮液态玻璃底栏（高级版）：真 backdrop 折射 + 纯平中央键。
 * 选中态用品牌绿 tint，未选中次级灰；中央键纯平、无渐变。
 */
@Composable
fun GlassBottomNavigationBar(
    navController: NavController,
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val surfaceColor = MaterialTheme.colorScheme.surface
    val dark = isDark()

    fun go(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    Row(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .height(66.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(33.dp) },
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(20f.dp.toPx(), 40f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(surfaceColor.copy(alpha = if (dark) 0.62f else 0.66f))
                }
            )
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomTab(
            title = "今日",
            iconKey = "house",
            selected = currentRoute == "home",
            onClick = { go("home") },
            modifier = Modifier.weight(1f)
        )
        // 中央新建键：纯平品牌绿
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(50.dp)
                .shadow(8.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.25f))
                .clip(CircleShape)
                .background(ThemeGreen)
                .border(0.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .pressable(scaleTo = 0.9f) { navController.navigate("habit_edit/new") }
        ) {
            Icon(
                painter = painterResource(SFIcons.res("plus")),
                contentDescription = "新建习惯",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        BottomTab(
            title = "统计",
            iconKey = "chart.bar",
            selected = currentRoute == "stats",
            onClick = { go("stats") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BottomTab(
    title: String,
    iconKey: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .pressable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(SFIcons.res(iconKey)),
            contentDescription = title,
            tint = if (selected) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            title,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

data class NavigationItem(val route: String, val title: String, val iconKey: String)
