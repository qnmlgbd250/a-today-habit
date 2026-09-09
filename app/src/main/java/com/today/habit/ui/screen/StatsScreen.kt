package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.AuroraBackground
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.InsetGroup
import com.today.habit.ui.component.LargeTitleHeader
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.SectionLabel
import com.today.habit.ui.component.StatBlock
import com.today.habit.ui.component.accent
import com.today.habit.ui.component.isDark
import com.today.habit.ui.component.staggerIn
import com.today.habit.ui.theme.Body17
import com.today.habit.ui.theme.Footnote13
import com.today.habit.ui.theme.Headline17
import com.today.habit.ui.theme.TabularNum
import com.today.habit.ui.viewmodel.HabitViewModel
import java.time.LocalDate

/**
 * 统计页 3.0：Screen Time 式数字组 + 月份热力图 + 分组榜单。
 * 高级感：等宽数字、发丝分隔、纯平热力、克制的火焰点缀。
 */
@Composable
fun StatsScreen(navController: NavController, viewModel: HabitViewModel) {
    val habits by viewModel.allHabits.observeAsState(emptyList())
    val allCheckIns by viewModel.allCheckIns.observeAsState(emptyList())
    val dark = isDark()

    val totalCheckIns = allCheckIns.size
    val activeDays = remember(allCheckIns) { allCheckIns.map { it.date }.toSet().size }
    val bestStreak = remember(habits, allCheckIns) {
        habits.maxOfOrNull { h ->
            val mine = allCheckIns.filter { it.habitId == h.id && it.count >= h.targetCount }
            calculateStreak(h, mine)
        } ?: 0
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuroraBackground(dark = dark)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                Spacer(Modifier.statusBarsPadding().height(10.dp))
                LargeTitleHeader(title = "统计回顾", subtitle = "每一次坚持都有痕迹")
                Spacer(Modifier.height(14.dp))
            }
            item {
                InsetGroup {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        StatBlock("$totalCheckIns", "累计打卡", Modifier.weight(1f))
                        Box(Modifier.width(0.5.dp).height(44.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                        StatBlock("$activeDays", "坚持天数", Modifier.weight(1f))
                        Box(Modifier.width(0.5.dp).height(44.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                        StatBlock("$bestStreak", "最高连击", Modifier.weight(1f))
                    }
                }
            }
            item { SectionLabel("打卡热力图") }
            item {
                InsetGroup {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        HeatLegend()
                        Spacer(Modifier.height(10.dp))
                        RefinedHeatmap(
                            countsByDate = remember(allCheckIns) {
                                allCheckIns.groupBy { it.date }.mapValues { it.value.size }
                            },
                            onDateClick = { date -> viewModel.setSelectedDate(date); navController.navigate("home") }
                        )
                    }
                }
            }
            item { SectionLabel("习惯") }
            if (habits.isEmpty()) {
                item {
                    InsetGroup {
                        Text(
                            "还没有习惯数据",
                            style = Footnote13,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(habits, key = { _, h -> h.id }) { index, habit ->
                    val habitCheckIns by viewModel.getCheckInsByHabitId(habit.id).collectAsState(emptyList())
                    InsetGroup(Modifier.staggerIn(index)) {
                        StatRow(habit, habitCheckIns)
                    }
                    if (index < habits.size - 1) Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun HeatLegend() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("近 15 周", style = Headline17, color = MaterialTheme.colorScheme.onBackground)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("少", style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(4.dp))
            listOf(0, 1, 3, 5, 7).forEach { level ->
                Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(getHeatmapColor(level)))
                Spacer(Modifier.width(3.dp))
            }
            Text("多", style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun RefinedHeatmap(countsByDate: Map<String, Int>, onDateClick: (LocalDate) -> Unit) {
    val today = remember { LocalDate.now() }
    val daysToDisplay = 105
    val dates = remember(today) { (0 until daysToDisplay).map { today.minusDays(it.toLong()) }.reversed() }
    val rows = 7
    val cols = daysToDisplay / rows
    var hoveredDate by remember { mutableStateOf<LocalDate?>(null) }

    Column {
        // 月份标签（与下方列严格对齐）
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (c in 0 until cols) {
                val first = dates[c * rows]
                val show = c == 0 || first.monthValue != dates[(c - 1) * rows].monthValue
                Box(Modifier.width(13.dp), contentAlignment = Alignment.Center) {
                    if (show) {
                        Text(
                            "${first.monthValue}月", fontSize = 9.sp, softWrap = false,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        if (hoveredDate != null) {
            Text(
                "${hoveredDate!!.monthValue}月${hoveredDate!!.dayOfMonth}日",
                style = Footnote13,
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

@Composable
fun getHeatmapColor(count: Int): Color {
    val dark = isDark()
    if (count == 0) return MaterialTheme.colorScheme.onSurface.copy(alpha = if (dark) 0.14f else 0.08f)
    return when {
        count <= 2 -> Color(0xFFA8E0AC)
        count <= 4 -> Color(0xFF6FCE77)
        count <= 6 -> Color(0xFF34C759)
        count <= 8 -> Color(0xFF248A3D)
        else -> Color(0xFF14532D)
    }
}

@Composable
fun StatRow(habit: Habit, checkIns: List<CheckInRecord>) {
    val accent = remember(habit.id, habit.color) { habit.accent() }
    val completed = remember(checkIns, habit.targetCount) { checkIns.filter { it.count >= habit.targetCount } }
    val total = completed.size
    val streak = remember(habit, completed) { calculateStreak(habit, completed) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        HabitTile(iconRes = HabitIcons.getRes(habit.icon), accent = accent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(habit.name, style = Body17, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
            Spacer(Modifier.height(1.dp))
            Text(
                "累计 $total 次" + if (streak > 0) " · 连续 $streak 天" else "",
                style = Footnote13,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (streak > 0) {
            Icon(
                painter = painterResource(SFIcons.res("flame")),
                contentDescription = null,
                tint = Color(0xFFFF9500),
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text("$streak", style = TabularNum, fontSize = 19.sp, color = MaterialTheme.colorScheme.onBackground)
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
