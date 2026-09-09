package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.AuroraBackground
import com.today.habit.ui.component.GlassCard
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.KpiCard
import com.today.habit.ui.component.LargeTitleHeader
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.SectionHeader
import com.today.habit.ui.component.accent
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.viewmodel.HabitViewModel
import java.time.LocalDate

/**
 * 统计页 2.0：KPI 总览 + 热力图 + 习惯榜单，全部液态玻璃卡片。
 */
@Composable
fun StatsScreen(navController: NavController, viewModel: HabitViewModel) {
    val habits by viewModel.allHabits.observeAsState(emptyList())
    val allCheckIns by viewModel.allCheckIns.observeAsState(emptyList())
    val isDark by viewModel.isDarkTheme

    val totalCheckIns = allCheckIns.size
    val activeDays = remember(allCheckIns) { allCheckIns.map { it.date }.toSet().size }
    val bestStreak = remember(habits, allCheckIns) {
        habits.maxOfOrNull { h ->
            val mine = allCheckIns.filter { it.habitId == h.id && it.count >= h.targetCount }
            calculateStreak(h, mine)
        } ?: 0
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuroraBackground(dark = isDark)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.statusBarsPadding().height(12.dp))
                LargeTitleHeader(title = "统计回顾", subtitle = "你的坚持，值得被看见")
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val fallback = com.today.habit.ui.component.HabitAccents
                    val h0 = habits.firstOrNull()
                    KpiCard("flame", "$totalCheckIns", "累计打卡", h0?.accent() ?: fallback[5], Modifier.weight(1f))
                    KpiCard("calendar", "$activeDays", "坚持天数", fallback[3], Modifier.weight(1f))
                    KpiCard("trophy", "$bestStreak", "最高连击", fallback[0], Modifier.weight(1f))
                }
            }
            item { CombinedHeatmapCard(allCheckIns) { date -> viewModel.setSelectedDate(date); navController.navigate("home") } }
            item { SectionHeader("习惯榜单") }
            if (habits.isEmpty()) {
                item {
                    GlassCard {
                        Text(
                            "还没有习惯数据，先去首页创建一个吧",
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                }
            } else {
                items(habits, key = { it.id }) { habit ->
                    val habitCheckIns by viewModel.getCheckInsByHabitId(habit.id).collectAsState(emptyList())
                    FlagshipStatRow(habit, habitCheckIns)
                }
            }
        }
    }
}

@Composable
fun CombinedHeatmapCard(allCheckIns: List<CheckInRecord>, onDateClick: (LocalDate) -> Unit) {
    val counts = remember(allCheckIns) { allCheckIns.groupBy { it.date }.mapValues { it.value.size } }
    GlassCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("打卡热力图", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("少", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                listOf(0, 1, 3, 5, 7).forEach { level ->
                    Box(
                        Modifier.size(10.dp).clip(RoundedCornerShape(3.dp))
                            .background(getHeatmapColor(level))
                    )
                    Spacer(Modifier.width(3.dp))
                }
                Text("多", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(14.dp))
        MultiLevelHeatmap(counts, onDateClick)
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun MultiLevelHeatmap(countsByDate: Map<String, Int>, onDateClick: (LocalDate) -> Unit) {
    val today = remember { LocalDate.now() }
    val daysToDisplay = 105
    val dates = remember(today) { (0 until daysToDisplay).map { today.minusDays(it.toLong()) }.reversed() }
    val rows = 7
    val cols = daysToDisplay / rows
    var hoveredDate by remember { mutableStateOf<LocalDate?>(null) }

    Column {
        if (hoveredDate != null) {
            Text(
                "${hoveredDate!!.monthValue}月${hoveredDate!!.dayOfMonth}日",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (c in 0 until cols) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (r in 0 until rows) {
                        val index = c * rows + r
                        if (index < dates.size) {
                            val date = dates[index]
                            val count = countsByDate[date.toString()] ?: 0
                            Box(
                                Modifier.size(13.dp).clip(RoundedCornerShape(4.dp))
                                    .background(getHeatmapColor(count))
                                    .combinedClickable(
                                        onClick = { hoveredDate = null; onDateClick(date) },
                                        onLongClick = { hoveredDate = date }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getHeatmapColor(count: Int): Color = when {
    count == 0 -> Color.LightGray.copy(alpha = 0.22f)
    count <= 2 -> Color(0xFFB9E7BC)
    count <= 4 -> Color(0xFF7ED484)
    count <= 6 -> Color(0xFF34C759)
    count <= 8 -> Color(0xFF248A3D)
    else -> Color(0xFF14532D)
}

@Composable
fun FlagshipStatRow(habit: Habit, checkIns: List<CheckInRecord>) {
    val accent = remember(habit.id, habit.color) { habit.accent() }
    val completed = remember(checkIns, habit.targetCount) { checkIns.filter { it.count >= habit.targetCount } }
    val total = completed.size
    val streak = remember(habit, completed) { calculateStreak(habit, completed) }
    val frac = (total.toFloat() / 21f).coerceIn(0f, 1f)

    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitTile(iconRes = HabitIcons.getRes(habit.icon), accent = accent, size = 48.dp, iconSize = 23.dp)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(
                    "累计 $total 次 · 连续 $streak 天",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(7.dp))
                Box(
                    Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(frac).height(6.dp).clip(RoundedCornerShape(3.dp))
                            .background(Brush.horizontalGradient(listOf(accent.main, accent.gradientEnd)))
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Icon(
                    painter = painterResource(SFIcons.res("flame")),
                    contentDescription = null, tint = if (streak > 0) Color(0xFFFF9F0A) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
                Text("$streak", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Text("连击", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
            if (!dates.contains(currentDate)) currentDate = currentDate.minusDays(1)
            while (dates.contains(currentDate)) {
                streak++
                currentDate = currentDate.minusDays(1)
            }
        }
    }
    return streak
}
