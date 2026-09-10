package com.today.habit.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.CheckInSoundPlayer
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.IOSProgressRing
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.iosElevatedCard
import com.today.habit.ui.component.iosEntrance
import com.today.habit.ui.component.iosPressable
import com.today.habit.ui.component.IOSTopScrim
import com.today.habit.ui.component.rememberIOSCollapsed
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.livedata.observeAsState

private val WeekdayNames = listOf("一", "二", "三", "四", "五", "六", "日")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HabitViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDateIfNecessary()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val allHabits by viewModel.allHabits.observeAsState(emptyList())
    val selectedDate by viewModel.selectedDate
    val checkIns by viewModel.getCheckInsByDate(selectedDate.toString()).observeAsState(emptyList())
    val filteredHabits = viewModel.getFilteredHabits(allHabits, selectedDate)

    val gridState = rememberLazyGridState()
    val collapsed = rememberIOSCollapsed(gridState)

    val today = LocalDate.now()
    val subtitle = if (selectedDate == today) {
        "${today.monthValue}月${today.dayOfMonth}日 星期${WeekdayNames[today.dayOfWeek.value - 1]}"
    } else {
        "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 星期${WeekdayNames[selectedDate.dayOfWeek.value - 1]}"
    }

    // 今日总览
    val recordByHabit = remember(checkIns) { checkIns.associateBy { it.habitId } }
    val doneCount = filteredHabits.count { (recordByHabit[it.id]?.count ?: 0) >= it.targetCount }
    val overallProgress = if (filteredHabits.isEmpty()) 0f
        else doneCount.toFloat() / filteredHabits.size.toFloat()

    Scaffold(
        containerColor = IOSColors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.iosEntrance("home:title", 0)) {
                        // 大标题行内嵌新建按钮：零导航栏铬，不占额外高度
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("今日习惯", style = IOSType.largeTitle, color = IOSColors.label)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(subtitle, style = IOSType.subhead, color = IOSColors.secondaryLabel)
                            }
                            Icon(
                                painter = painterResource(SFIcons.res("plus")),
                                contentDescription = "新建习惯",
                                tint = IOSColors.blue,
                                modifier = Modifier
                                    .iosPressable(pressedScale = 0.8f, pressedAlpha = 0.5f) {
                                        navController.navigate("habit_edit/new")
                                    }
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.iosEntrance("home:hero", 1)) {
                        TodayOverviewCard(
                            doneCount = doneCount,
                            totalCount = filteredHabits.size,
                            progress = overallProgress
                        )
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.iosEntrance("home:dates", 2)) {
                        DateStrip(selectedDate) { viewModel.setSelectedDate(it) }
                    }
                }
                if (filteredHabits.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Box(modifier = Modifier.iosEntrance("home:empty", 3)) {
                            HomeEmptyState { navController.navigate("habit_edit/new") }
                        }
                    }
                } else {
                    items(filteredHabits, key = { it.id }) { habit ->
                        Box(
                            modifier = Modifier.iosEntrance(
                                key = "home:${habit.id}",
                                index = 3 + filteredHabits.indexOf(habit).coerceAtMost(8)
                            )
                        ) {
                            val checkIn = recordByHabit[habit.id]
                            HabitGridItem(habit, checkIn) {
                                viewModel.toggleCheckIn(habit.id, selectedDate.toString(), habit.targetCount)
                            }
                        }
                    }
                }
            }
            IOSTopScrim(collapsed)
        }
    }
}

/** 今日进度总览 hero 卡 */
@Composable
private fun TodayOverviewCard(doneCount: Int, totalCount: Int, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "todayProgress"
    )
    val headline = when {
        totalCount == 0 -> "还没有习惯"
        doneCount == totalCount -> "全部达成"
        else -> "已完成 $doneCount / $totalCount"
    }
    val sub = when {
        totalCount == 0 -> "点标题旁的 + 创建第一个习惯"
        doneCount == totalCount -> "太棒了，今天的目标都完成了"
        doneCount == 0 -> "还没有打卡，从第一个开始吧"
        else -> "继续加油，还差 ${totalCount - doneCount} 个"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .iosElevatedCard(18.dp)
            .padding(18.dp)
    ) {
        IOSProgressRing(
            progress = animatedProgress,
            strokeWidth = 8.dp,
            modifier = Modifier.size(84.dp)
        ) {
            Text(
                "${(animatedProgress * 100).toInt()}%",
                style = IOSType.headline,
                color = IOSColors.label
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(headline, style = IOSType.title2, color = IOSColors.label)
            Text(sub, style = IOSType.subhead, color = IOSColors.secondaryLabel)
        }
    }
}

/** iOS 风格横向日期条：星期小字 + 日期数字，选中蓝色圆底 */
@Composable
private fun DateStrip(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val dates = remember(today) { (-30..30).map { today.plusDays(it.toLong()) } }
    val listState = rememberLazyListState()

    LaunchedEffect(selectedDate) {
        val index = dates.indexOf(selectedDate)
        if (index >= 0) listState.scrollToItem(maxOf(0, index - 3))
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(dates, key = { it.toEpochDay() }) { date ->
            val isSelected = date == selectedDate
            val isToday = date == today
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .width(44.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) IOSColors.blue else Color.Transparent)
                    .iosPressable(pressedScale = 0.92f) { onDateSelected(date) }
                    .padding(vertical = 6.dp)
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White else IOSColors.secondaryLabel
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        isSelected -> Color.White
                        isToday -> IOSColors.blue
                        else -> IOSColors.label
                    }
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isToday && !isSelected) IOSColors.blue else Color.Transparent
                        )
                )
            }
        }
    }
}

/** 空态：同心圆 + 新建隐喻 + 两行文案 */
@Composable
private fun HomeEmptyState(onCreate: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
            .iosPressable(onClick = onCreate)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(116.dp)) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(IOSColors.tertiaryCard)
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(IOSColors.card)
            ) {
                Icon(
                    painter = painterResource(SFIcons.res("plus")),
                    contentDescription = null,
                    tint = IOSColors.blue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Text("这一天没有习惯", style = IOSType.headline, color = IOSColors.secondaryLabel)
        Text("点按这里新建第一个习惯", style = IOSType.subhead, color = IOSColors.tertiaryLabel)
    }
}

@Composable
fun HabitGridItem(habit: Habit, checkInRecord: CheckInRecord?, onClick: () -> Unit) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val currentCount = checkInRecord?.count ?: 0
    val targetCount = habit.targetCount
    val isCompleted = currentCount >= targetCount

    val progress by animateFloatAsState(
        targetValue = currentCount.toFloat() / targetCount.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "habitProgress"
    )
    // 完成态整圆融为一体；未完成墨色字形 + 进度环
    val circleSize by animateDpAsState(
        targetValue = if (isCompleted) 68.dp else 62.dp,
        animationSpec = tween(durationMillis = 300),
        label = "habitCircleSize"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isCompleted) Color.White else IOSColors.secondaryLabel,
        animationSpec = tween(durationMillis = 300),
        label = "habitIconTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .iosPressable {
                haptics.performHapticFeedback(
                    if (!isCompleted) HapticFeedbackType.LongPress
                    else HapticFeedbackType.VirtualKey
                )
                CheckInSoundPlayer.playCompletionSound(context)
                onClick()
            }
            .padding(vertical = 4.dp)
    ) {
        if (isCompleted) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(IOSColors.green)
            ) {
                Icon(
                    painter = painterResource(HabitIcons.getRes(habit.icon)),
                    contentDescription = habit.name,
                    modifier = Modifier.size(circleSize * 0.47f),
                    tint = iconTint
                )
            }
        } else {
            IOSProgressRing(
                progress = progress,
                strokeWidth = 4.dp,
                modifier = Modifier.size(76.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(circleSize)
                ) {
                    Icon(
                        painter = painterResource(HabitIcons.getRes(habit.icon)),
                        contentDescription = habit.name,
                        modifier = Modifier.size(30.dp),
                        tint = iconTint
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = habit.name,
            style = IOSType.footnote,
            fontWeight = FontWeight.Medium,
            color = IOSColors.label,
            maxLines = 1
        )
        Text(
            text = "$currentCount/$targetCount",
            style = IOSType.caption,
            fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isCompleted) IOSColors.green else IOSColors.tertiaryLabel
        )
    }
}
