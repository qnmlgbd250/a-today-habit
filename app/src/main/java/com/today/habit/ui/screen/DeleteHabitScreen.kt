package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.viewmodel.HabitViewModel

/**
 * 删除习惯确认页面（替代原确认弹窗，iOS 两步确认交互）。
 * 第一次点击按钮进入"确认删除"状态，再次点击才真正删除。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteHabitScreen(navController: NavController, viewModel: HabitViewModel, habitId: String) {
    val id = habitId.toLongOrNull()
    val habits by viewModel.allHabits.observeAsState(emptyList())
    val habit = habits.find { it.id == id }

    // 第二次点击才执行删除
    var armed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(habit?.id) { if (habit == null) armed = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("删除习惯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(SFIcons.res("chevron.left")),
                            contentDescription = "返回",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (habit != null) {
                // 习惯概览卡片
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            painter = painterResource(HabitIcons.getRes(habit.icon)),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(habit.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val frequencyLabel = when (habit.frequency) {
                            "DAILY" -> "日"
                            "WEEKDAYS" -> "工作日"
                            "WEEKLY" -> "周"
                            "MONTHLY" -> "月"
                            else -> "日"
                        }
                        Text(
                            "目标: ${habit.targetCount} 次/$frequencyLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    "删除后，该习惯的全部打卡记录将一并删除，且无法恢复。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = {
                        if (armed) {
                            viewModel.deleteHabit(habit)
                            navController.popBackStack()
                        } else {
                            armed = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        if (armed) "确认删除" else "删除习惯",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (armed) {
                    OutlinedButton(
                        onClick = { armed = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Text("习惯不存在或已被删除", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
