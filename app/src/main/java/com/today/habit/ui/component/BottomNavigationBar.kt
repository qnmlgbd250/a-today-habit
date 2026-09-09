package com.today.habit.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
 * iOS 27 悬浮液态玻璃底栏：真正的 backdrop 折射 + 中央新建按钮。
 * 布局：今日 | ＋ | 统计，中央按钮为品牌绿渐变，旗舰 App 标配。
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

    fun go(route: String) {
        if (currentRoute == route) return
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

    Row(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .height(72.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(36.dp) },
                effects = {
                    vibrancy()
                    blur(6f.dp.toPx())
                    lens(20f.dp.toPx(), 40f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(surfaceColor.copy(alpha = 0.55f))
                }
            )
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
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
        // 中央新建按钮
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp)
                .shadow(10.dp, CircleShape, spotColor = ThemeGreen.copy(alpha = 0.5f))
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF5BE584), ThemeGreen)))
                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { navController.navigate("habit_edit/new") }
        ) {
            Icon(
                painter = painterResource(SFIcons.res("plus")),
                contentDescription = "新建习惯",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
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
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(SFIcons.res(iconKey)),
            contentDescription = title,
            tint = if (selected) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            modifier = Modifier.size(23.dp)
        )
        Text(
            title,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}

data class NavigationItem(val route: String, val title: String, val iconKey: String)
