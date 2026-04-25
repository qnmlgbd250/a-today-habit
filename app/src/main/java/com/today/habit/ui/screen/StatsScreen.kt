package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.today.habit.data.AppDatabase
import com.today.habit.data.HabitRepository
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.HandDrawnSun
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.viewmodel.HabitViewModel
import com.today.habit.ui.viewmodel.HabitViewModelFactory
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen() {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = HabitRepository(database.habitDao())
    val viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory(repository))

    val habits by viewModel.allHabits.observeAsState(emptyList())
    val allCheckIns by viewModel.allCheckIns.observeAsState(emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("统计回顾", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 上区：合并热力图
            item {
                CombinedHeatmapCard(allCheckIns)
            }

            // 下区：习惯列表 (移除了“习惯项统计”标题)
            items(habits, key = { it.id }) { habit ->
                val habitCheckIns by viewModel.getCheckInsByHabitId(habit.id).collectAsState(emptyList())
                HabitStatsItem(habit, habitCheckIns)
            }
        }
    }
}

@Composable
fun CombinedHeatmapCard(allCheckIns: List<CheckInRecord>) {
    val checkInCountsByDate = remember(allCheckIns) {
        allCheckIns.groupBy { it.date }.mapValues { it.value.size }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 标题与图例并排显示在顶部
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "打卡热力图", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                
                // 图例：移动到右上角
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("少", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    listOf(0, 1, 3, 5, 7).forEach { level ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(getHeatmapColor(level))
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("多", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MultiLevelHeatmap(checkInCountsByDate)
        }
    }
}

@Composable
fun MultiLevelHeatmap(countsByDate: Map<String, Int>) {
    val today = remember { LocalDate.now() }
    val daysToDisplay = 105 
    val dates = remember(today) {
        (0 until daysToDisplay).map { today.minusDays(it.toLong()) }.reversed()
    }

    val rows = 7 
    val cols = daysToDisplay / rows

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (c in 0 until cols) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until rows) {
                    val index = c * rows + r
                    if (index < dates.size) {
                        val date = dates[index]
                        val count = countsByDate[date.toString()] ?: 0
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(RoundedCornerShape(3.dp)) // 恢复为带微圆角的方形
                                .background(getHeatmapColor(count))
                        )
                    }
                }
            }
        }
    }
}

fun getHeatmapColor(count: Int): Color {
    return when {
        count == 0 -> Color.LightGray.copy(alpha = 0.2f)
        count <= 2 -> Color(0xFFC8E6C9) 
        count <= 4 -> Color(0xFF81C784) 
        count <= 6 -> Color(0xFF4CAF50) 
        count <= 8 -> Color(0xFF2E7D32) 
        else -> Color(0xFF1B5E20)       
    }
}

@Composable
fun HabitStatsItem(habit: Habit, checkIns: List<CheckInRecord>) {
    // 只有打卡次数 >= 目标次数的记录才被视为“已完成”
    val completedCheckIns = remember(checkIns, habit.targetCount) {
        checkIns.filter { it.count >= habit.targetCount }
    }
    
    val totalCount = completedCheckIns.size
    val streak = calculateStreak(completedCheckIns)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HabitIcons.getIcon(habit.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "累计打卡 $totalCount 天",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = streak.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "连续打卡",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun calculateStreak(checkIns: List<CheckInRecord>): Int {
    if (checkIns.isEmpty()) return 0
    val dates = checkIns.map { it.date }.toSet()
    var streak = 0
    var currentDate = LocalDate.now()
    
    if (!dates.contains(currentDate.toString())) {
        currentDate = currentDate.minusDays(1)
    }

    while (dates.contains(currentDate.toString())) {
        streak++
        currentDate = currentDate.minusDays(1)
    }
    return streak
}
