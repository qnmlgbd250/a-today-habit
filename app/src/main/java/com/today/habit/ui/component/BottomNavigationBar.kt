package com.today.habit.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.today.habit.ui.theme.IOSColors

private val GlassShape = RoundedCornerShape(30.dp)
private val IndicatorShape = RoundedCornerShape(19.dp)

/**
 * iOS 26 Liquid Glass 悬浮底栏（纯图标）：
 * 液态玻璃（折射 + vibrancy + 模糊）+ 镜面高光 +
 * 单块选中指示做弹簧滑动（低刚度 + 过冲 = 粘滞果冻感）。
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
    // 子页面（非 Tab）时指示器停在上一个 Tab，不乱跳
    var lastTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val activeIndex = items.indexOfFirst { it.route == currentRoute }
    if (activeIndex >= 0) lastTabIndex = activeIndex

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 10.dp)
        ) {
            // 粘滞滑块：整块滑动 + 弹簧过冲
            val tabWidth: Dp = maxWidth / 3
            val slideX by animateDpAsState(
                targetValue = tabWidth * lastTabIndex,
                animationSpec = spring(
                    stiffness = 170f,
                    dampingRatio = 0.62f
                ),
                label = "tabSlide"
            )
            Box(
                modifier = Modifier
                    .offset(x = slideX)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .clip(IndicatorShape)
                    .background(IOSColors.tabSelect)
            )
            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == lastTabIndex && activeIndex >= 0
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1f,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMedium,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        label = "tabIconPop"
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .iosPressable(pressedScale = 0.85f) {
                                if (index != lastTabIndex || activeIndex < 0) {
                                    haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            }
                    ) {
                        Icon(
                            painter = painterResource(SFIcons.res(item.iconKey)),
                            contentDescription = item.title,
                            tint = if (isSelected) IOSColors.label else IOSColors.gray,
                            modifier = Modifier
                                .size(25.dp)
                                .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                        )
                    }
                }
            }
        }
    }
}

data class NavigationItem(val route: String, val title: String, val iconKey: String)
