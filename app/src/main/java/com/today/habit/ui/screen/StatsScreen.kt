package com.today.habit.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
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
import java.time.temporal.ChronoUnit
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
        containerColor = Color.Transparent
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
            .iosElevatedCard(22.dp)
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
            "今年 · 累计打卡 $totalCount 次 · 左右滑动查看全年",
            style = IOSType.footnote,
            color = IOSColors.tertiaryLabel
        )
    }
}

/**
 * GitHub 风格热力图：列=周（周一起），展示当前自然年 1 月~12 月，可横滑查看。
 * 顶部为月份标签，左侧标注一/三/五。初次进入自动定位到今天所在的列。
 * 长按格子显示该日数据，点击跳转到首页对应日期。
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HeatmapGrid(countsByDate: Map<String, Int>, onDateClick: (LocalDate) -> Unit) {
    val today = remember { LocalDate.now() }
    val year = today.year
    val rows = 7
    val cell = 13.dp
    val gap = 4.dp
    val monthH = 16.dp
    val colW = cell + gap
    // 自然年：从 1 月 1 日所在周的周一起，到 12 月 31 日所在周的周日止（52~53 列）
    // 年外的首尾几天留空（GitHub 同款）
    val startMonday = remember(year) {
        val jan1 = LocalDate.of(year, 1, 1)
        jan1.minusDays((jan1.dayOfWeek.value - 1).toLong())
    }
    val weekCount = remember(year, startMonday) {
        val dec31 = LocalDate.of(year, 12, 31)
        val endSunday = dec31.plusDays((7 - dec31.dayOfWeek.value).toLong())
        ((ChronoUnit.DAYS.between(startMonday, endSunday).toInt() + 1) / 7)
    }
    // 今天所在的列（用于初始定位）
    val todayCol = remember(today, startMonday) {
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        ChronoUnit.WEEKS.between(startMonday, monday).toInt()
    }
    var hintDate by remember { mutableStateOf<LocalDate?>(null) }
    val scrollState = rememberScrollState()

    val weekdayLabels = mapOf(0 to "一", 2 to "三", 4 to "五")
    // 月份标签：只在“本年 1 号”所在的周列上标注，且标签之间至少间隔 3 列
    // （否则 "9月" 紧贴 "10月" 会挤在一起；GitHub 同款做法）
    val monthLabels = remember(year, startMonday, weekCount) {
        val candidate = mutableMapOf<Int, String>()
        for (c in 0 until weekCount) {
            val colFirst = startMonday.plusDays((c * 7).toLong())
            for (r in 0 until rows) {
                val d = colFirst.plusDays(r.toLong())
                if (d.year == year && d.dayOfMonth == 1) {
                    candidate[c] = "${d.monthValue}月"
                    break
                }
            }
        }
        val labels = MutableList(weekCount) { "" }
        var lastKept = -10
        for (c in 0 until weekCount) {
            val s = candidate[c] ?: continue
            if (c - lastKept >= 3) {
                labels[c] = s
                lastKept = c
            }
        }
        labels
    }

    Column {
        if (hintDate != null) {
            Text(
                "${hintDate!!.monthValue}月${hintDate!!.dayOfMonth}日 · ${countsByDate[hintDate.toString()] ?: 0} 次打卡",
                style = IOSType.footnote,
                color = IOSColors.secondaryLabel,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        // 可视宽度反推格子大小：恰好容下整数列，默认位置左右两边都是完整列、不被切半
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // 左侧星期标签占掉 14.dp + 6.dp，真正可视的是剩下的宽度（上版按整行算，定位偏了约一列）
            val vw = maxWidth - 14.dp - 6.dp
            val fitCell = remember(vw) {
                if (vw <= 0.dp) cell
                else {
                    val n = (vw / colW).toInt().coerceAtLeast(1)
                    (vw - gap * (n - 1)) / n
                }
            }
            val fitColW = fitCell + gap
            // 实测可视宽度 + 今天列的真实右边缘（布局实测值，不靠公式估，杜绝半格）
            var viewportPx by remember { mutableIntStateOf(0) }
            var rowRootX by remember { mutableFloatStateOf(0f) }
            var todayRight by remember { mutableFloatStateOf(-1f) }
            // 只自动定位一次：滑动时今天列的全局坐标会变，若每次都跟就会跟手指打架（屏闪）
            var autoScrolled by remember { mutableStateOf(false) }
            // 内容量出后定位到今天所在的列（右对齐）；列宽已凑整，左右都是完整列
            LaunchedEffect(scrollState.maxValue, viewportPx, todayRight, rowRootX) {
                if (!autoScrolled && scrollState.maxValue > 0 && viewportPx > 0 && todayRight > 0) {
                    autoScrolled = true
                    val target = (todayRight - rowRootX - viewportPx).toInt()
                        .coerceIn(0, scrollState.maxValue)
                    scrollState.scrollTo(target)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                // 左侧星期标签（顶部留出月份栏高度以对齐）
                Column {
                    Spacer(modifier = Modifier.height(monthH + gap))
                    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                        for (r in 0 until rows) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(width = 14.dp, height = fitCell)
                            ) {
                                weekdayLabels[r]?.let {
                                    Text(it, style = IOSType.caption, color = IOSColors.tertiaryLabel)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                // 右侧：月份 + 自然年周网格，横滑查看全年
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .onSizeChanged { viewportPx = it.width }
                        .onGloballyPositioned { rowRootX = it.positionInRoot().x }
                        .horizontalScroll(scrollState)
                ) {
                    // 月份标签按列绝对定位画在网格上方，不再塞进格子里——
                    // 格宽约束会把“X月”压扁（之前“1月”只剩一条细线），这是根治
                    Box {
                        Column {
                            Spacer(modifier = Modifier.height(monthH + gap))
                            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                                for (c in 0 until weekCount) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(gap),
                                        modifier = Modifier.then(
                                            if (c == todayCol) Modifier.onGloballyPositioned {
                                                // 定位完成后不再更新，否则滑动时持续重组+抢滚动=屏闪
                                                if (!autoScrolled) {
                                                    todayRight = it.positionInRoot().x + it.size.width.toFloat()
                                                }
                                            } else Modifier
                                        )
                                    ) {
                                        for (r in 0 until rows) {
                                            val date = startMonday.plusDays((c * 7 + r).toLong())
                                            if (date.year == year && !date.isAfter(today)) {
                                                val count = countsByDate[date.toString()] ?: 0
                                                val selected = date == hintDate
                                                Box(
                                                    modifier = Modifier
                                                        .size(fitCell)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(heatmapColor(count))
                                                        .then(
                                                            if (selected) Modifier.border(
                                                                1.dp,
                                                                IOSColors.label,
                                                                RoundedCornerShape(4.dp)
                                                            ) else Modifier
                                                        )
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
                                            } else if (date.year == year) {
                                                // 今年的未来日期：浅色占位格（不可点），网格不塌、后面月份不空荡
                                                Box(
                                                    modifier = Modifier
                                                        .size(fitCell)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(heatmapColor(0))
                                                )
                                            } else {
                                                Spacer(modifier = Modifier.size(fitCell))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        monthLabels.forEachIndexed { c, s ->
                            if (s.isNotEmpty()) {
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier = Modifier
                                        .offset(x = fitColW * c)
                                        .height(monthH)
                                ) {
                                    Text(
                                        s,
                                        style = IOSType.caption,
                                        color = IOSColors.tertiaryLabel,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
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
            .iosElevatedCard(22.dp)
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
