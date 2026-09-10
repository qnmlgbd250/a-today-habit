package com.today.habit.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.IOSProgressRing
import com.today.habit.ui.component.iosElevatedCard
import com.today.habit.ui.component.iosEntrance
import com.today.habit.ui.component.iosTopFade
import com.today.habit.ui.component.rememberIOSCollapsed
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSHeat1
import com.today.habit.ui.theme.IOSHeat2
import com.today.habit.ui.theme.IOSHeat3
import com.today.habit.ui.theme.IOSHeat4
import com.today.habit.ui.theme.IOSHeat5
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.viewmodel.HabitViewModel
import java.time.LocalDate
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavController, viewModel: HabitViewModel) {
    val habits by viewModel.allHabits.observeAsState(emptyList())
    val allCheckIns by viewModel.allCheckIns.observeAsState(emptyList())

    val listState = rememberLazyListState()
    val collapsed = rememberIOSCollapsed(listState)
    // 滚动时列表顶部渐隐（内容自身淡出；常驻雾罩已删：它的底边自己就是一条线）
    val topFade by animateDpAsState(
        targetValue = if (collapsed) 88.dp else 0.dp,
        animationSpec = tween(durationMillis = 250),
        label = "topFade"
    )

    Scaffold(
        containerColor = IOSColors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().iosTopFade(topFade),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(modifier = Modifier.iosEntrance("stats:heat", 0)) {
                        HeatmapCard(allCheckIns) { date ->
                            viewModel.setSelectedDate(date)
                            navController.navigate("home")
                        }
                    }
                }
                items(habits, key = { it.id }) { habit ->
                    Box(
                        modifier = Modifier.iosEntrance(
                            key = "stats:${habit.id}",
                            index = 1 + habits.indexOf(habit).coerceAtMost(8)
                        )
                    ) {
                        val habitCheckIns by viewModel.getCheckInsByHabitId(habit.id).collectAsState(emptyList())
                        HabitStatsCard(habit, habitCheckIns)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCard(allCheckIns: List<CheckInRecord>, onDateClick: (LocalDate) -> Unit) {
    val countsByDate = remember(allCheckIns) {
        allCheckIns.groupBy { it.date }.mapValues { it.value.size }
    }
    val totalCount = allCheckIns.size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .iosElevatedCard(16.dp)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("打卡热力图", style = IOSType.headline, color = IOSColors.label)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("少", style = IOSType.caption, color = IOSColors.secondaryLabel)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(0, 1, 3, 5, 7).forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(heatmapColor(level))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text("多", style = IOSType.caption, color = IOSColors.secondaryLabel)
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        HeatmapGrid(countsByDate, onDateClick)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "过去 15 周 · 累计打卡 $totalCount 次",
            style = IOSType.footnote,
            color = IOSColors.tertiaryLabel
        )
    }
}

/**
 * 星期对齐的热力图：列=周（周一起），左侧标注一/三/五。
 * 长按格子显示该日数据，点击跳转到首页对应日期。
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HeatmapGrid(countsByDate: Map<String, Int>, onDateClick: (LocalDate) -> Unit) {
    val today = remember { LocalDate.now() }
    val cols = 15
    val rows = 7
    // 尾端对齐今天：今天固定在最后一列与其星期对应的行，未来格留空
    val todaySlot = remember(today) { (cols - 1) * rows + (today.dayOfWeek.value - 1) }
    var hintDate by remember { mutableStateOf<LocalDate?>(null) }

    val weekdayLabels = mapOf(0 to "一", 2 to "三", 4 to "五")

    Column {
        if (hintDate != null) {
            Text(
                "${hintDate!!.monthValue}月${hintDate!!.dayOfMonth}日 · ${countsByDate[hintDate.toString()] ?: 0} 次打卡",
                style = IOSType.footnote,
                color = IOSColors.secondaryLabel,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            // 左侧星期标签
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until rows) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(width = 14.dp, height = 13.dp)
                    ) {
                        weekdayLabels[r]?.let {
                            Text(it, style = IOSType.caption, color = IOSColors.tertiaryLabel)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            // 15 周网格
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0 until cols) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (r in 0 until rows) {
                            val slot = c * rows + r
                            if (slot <= todaySlot) {
                                val date = today.minusDays((todaySlot - slot).toLong())
                                val count = countsByDate[date.toString()] ?: 0
                                Box(
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(heatmapColor(count))
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                hintDate = null
                                                onDateClick(date)
                                            },
                                            onLongClick = { hintDate = date }
                                        )
                                )
                            } else {
                                Spacer(modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun heatmapColor(count: Int): Color {
    val empty = IOSColors.heatEmpty
    return when {
        count == 0 -> empty
        count <= 2 -> IOSHeat1
        count <= 4 -> IOSHeat2
        count <= 6 -> IOSHeat3
        count <= 8 -> IOSHeat4
        else -> IOSHeat5
    }
}

@Composable
private fun HabitStatsCard(habit: Habit, checkIns: List<CheckInRecord>) {
    // 只有打卡次数 >= 目标次数的记录才视为"已完成"
    val completedCheckIns = remember(checkIns, habit.targetCount) {
        checkIns.filter { it.count >= habit.targetCount }
    }
    val totalCount = completedCheckIns.size
    val streak = calculateStreak(habit, completedCheckIns)

    // 以 21 天为一个阶段
    val progress by animateFloatAsState(
        targetValue = (totalCount.toFloat() / 21f).coerceAtMost(1f),
        animationSpec = tween(durationMillis = 800),
        label = "statsProgress"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .iosElevatedCard(16.dp)
            .padding(16.dp)
    ) {
        IOSProgressRing(
            progress = progress,
            strokeWidth = 4.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                painter = painterResource(HabitIcons.getRes(habit.icon)),
                contentDescription = null,
                modifier = Modifier.size(25.dp),
                tint = if (progress >= 1f) IOSColors.green else IOSColors.secondaryLabel
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(habit.name, style = IOSType.headline, color = IOSColors.label, maxLines = 1)
            Text(
                "累计打卡 $totalCount 次",
                style = IOSType.footnote,
                color = IOSColors.tertiaryLabel
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "$streak",
                style = IOSType.display,
                color = IOSColors.label
            )
            Text(
                "连续${when (habit.frequency) { "WEEKLY" -> "周"; "MONTHLY" -> "月"; else -> "天" }}",
                style = IOSType.caption,
                color = IOSColors.tertiaryLabel
            )
        }
    }
}

fun calculateStreak(habit: Habit, checkIns: List<CheckInRecord>): Int {
    if (checkIns.isEmpty()) return 0
    val dates = checkIns.map { LocalDate.parse(it.date) }.toSortedSet()
    var streak = 0

    when (habit.frequency) {
        "MONTHLY" -> {
            var year = LocalDate.now().year
            var month = LocalDate.now().monthValue
            if (dates.none { it.year == year && it.monthValue == month }) {
                month--
                if (month == 0) { month = 12; year-- }
            }
            while (dates.any { it.year == year && it.monthValue == month }) {
                streak++
                month--
                if (month == 0) { month = 12; year-- }
            }
        }
        "WEEKLY" -> {
            val today = LocalDate.now()
            var weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
            if (dates.none { !it.isBefore(weekStart) && !it.isAfter(weekStart.plusDays(6)) }) {
                weekStart = weekStart.minusWeeks(1)
            }
            while (dates.any { !it.isBefore(weekStart) && !it.isAfter(weekStart.plusDays(6)) }) {
                streak++
                weekStart = weekStart.minusWeeks(1)
            }
        }
        else -> {
            var currentDate = LocalDate.now()
            if (!dates.contains(currentDate)) {
                currentDate = currentDate.minusDays(1)
            }
            while (dates.contains(currentDate)) {
                streak++
                currentDate = currentDate.minusDays(1)
            }
        }
    }
    return streak
}
