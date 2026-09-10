package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.IOSChevron
import com.today.habit.ui.component.IOSDivider
import com.today.habit.ui.component.IOSGroup
import com.today.habit.ui.component.IOSLargeTitle
import com.today.habit.ui.component.IOSNavBar
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.iosPressable
import com.today.habit.ui.component.rememberIOSCollapsed
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsScreen(navController: NavController, viewModel: HabitViewModel) {
    val habits by viewModel.allHabits.observeAsState(emptyList())
    val listState = rememberLazyListState()
    val collapsed = rememberIOSCollapsed(listState)

    Scaffold(
        topBar = {
            IOSNavBar(
                title = "管理习惯",
                onBack = { navController.popBackStack() },
                showTitle = collapsed,
                elevated = collapsed
            )
        },
        containerColor = IOSColors.background
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                IOSLargeTitle(title = "管理习惯", subtitle = "${habits.size} 个习惯")
            }
            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无习惯，去新建一个吧", style = IOSType.body, color = IOSColors.secondaryLabel)
                    }
                }
            } else {
                item {
                    IOSGroup {
                        habits.forEachIndexed { index, habit ->
                            HabitManageRow(
                                habit = habit,
                                onEdit = { navController.navigate("habit_edit/${habit.id}") },
                                onDelete = { navController.navigate("habit_delete/${habit.id}") }
                            )
                            if (index < habits.lastIndex) IOSDivider()
                        }
                    }
                }
            }
        }
    }
}

/** iOS 管理行：点整行编辑，尾部红色删除键 */
@Composable
private fun HabitManageRow(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(IOSColors.card)
            .iosPressable(onClick = onEdit)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(IOSColors.green.copy(alpha = 0.14f))
        ) {
            Icon(
                painter = painterResource(HabitIcons.getRes(habit.icon)),
                contentDescription = null,
                tint = IOSColors.green,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(habit.name, style = IOSType.body, color = IOSColors.label, maxLines = 1)
            Text(
                "目标 ${habit.targetCount} 次 / ${frequencyLabel(habit.frequency)}",
                style = IOSType.footnote,
                color = IOSColors.secondaryLabel
            )
        }
        Icon(
            painter = painterResource(SFIcons.res("trash")),
            contentDescription = "删除",
            tint = IOSColors.red,
            modifier = Modifier
                .size(36.dp)
                .iosPressable(pressedScale = 1f, pressedAlpha = 0.4f, onClick = onDelete)
                .padding(8.dp)
        )
        IOSChevron()
    }
}

fun frequencyLabel(frequency: String): String = when (frequency) {
    "DAILY" -> "每天"
    "WEEKDAYS" -> "工作日"
    "WEEKLY" -> "每周"
    "MONTHLY" -> "每月"
    else -> "每天"
}
