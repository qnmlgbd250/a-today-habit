package com.today.habit.ui.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.roundToInt

private val GlassShape = RoundedCornerShape(30.dp)
private val BlobShape = RoundedCornerShape(19.dp)

/**
 * iOS 26 Liquid Glass 悬浮底栏（纯图标）：
 * - 整栏液态玻璃（折射 + vibrancy + 模糊 + 镜面高光）
 * - 选中态是一块可拖动的磨砂玻璃：跟手拖拽（跨界滴答触觉），
 *   松手弹簧吸附到最近 Tab；轻点同样弹簧滑过去
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
    // 子页面（非 Tab）时指示停在上一个 Tab，不乱跳
    var lastTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val activeIndex = items.indexOfFirst { it.route == currentRoute }
    if (activeIndex >= 0) lastTabIndex = activeIndex

    val surfaceColor = IOSColors.card
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f

    fun navigateTo(index: Int) {
        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
        navController.navigate(items[index].route) {
            popUpTo(navController.graph.startDestinationId)
            launchSingleTop = true
        }
    }

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
        // 整栏顶部镜面高光
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
            val tabWidth: Dp = maxWidth / 3

            // 弹簧基座（轻点切换时粘滞滑过去）
            val baseXState = animateDpAsState(
                targetValue = tabWidth * lastTabIndex,
                animationSpec = spring(stiffness = 170f, dampingRatio = 0.62f),
                label = "tabSlide"
            )
            val baseX by baseXState
            // 拖拽偏移（跟手，松手清零由弹簧接管）
            var dragOffset by remember { mutableStateOf(0.dp) }
            var hoverIndex by remember { mutableStateOf(lastTabIndex) }
            // 手势回调里读最新值，避免闭包过期
            val latestBaseX by rememberUpdatedState(baseX)
            val latestTab by rememberUpdatedState(lastTabIndex)

            val dragState = remember(tabWidth) {
                androidx.compose.foundation.gestures.DraggableState { deltaPx ->
                    val deltaDp = with(density) { deltaPx.toDp() }
                    val min = -(tabWidth * latestTab) - 20.dp
                    val max = tabWidth * (2 - latestTab) + 20.dp
                    dragOffset = (dragOffset + deltaDp).coerceIn(min, max)
                    val hover = ((latestBaseX + dragOffset) / tabWidth).roundToInt().coerceIn(0, 2)
                    if (hover != hoverIndex) {
                        hoverIndex = hover
                        haptics.performHapticFeedback(HapticFeedbackType.VirtualKey)
                    }
                }
            }

            // 可拖动的磨砂玻璃选中块
            Box(
                modifier = Modifier
                    .offset(x = baseX + dragOffset)
                    .width(tabWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { BlobShape },
                        effects = { blur(14f.dp.toPx()) },
                        onDrawSurface = {
                            drawRect(Color.White.copy(alpha = if (isLight) 0.22f else 0.30f))
                        }
                    )
                    .border(0.5.dp, Color.White.copy(alpha = 0.25f), BlobShape)
            ) {
                // 块内镜面高光
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(BlobShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                ),
                                endY = 70f
                            )
                        )
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .draggable(
                        state = dragState,
                        orientation = Orientation.Horizontal,
                        onDragStarted = { hoverIndex = lastTabIndex },
                        onDragStopped = { velocity ->
                            var target = ((latestBaseX + dragOffset) / tabWidth).roundToInt()
                            if (velocity > 600f) target += 1
                            else if (velocity < -600f) target -= 1
                            target = target.coerceIn(0, 2)
                            dragOffset = 0.dp
                            if (target != latestTab) navigateTo(target)
                        }
                    )
            ) {
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
                                    navigateTo(index)
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
