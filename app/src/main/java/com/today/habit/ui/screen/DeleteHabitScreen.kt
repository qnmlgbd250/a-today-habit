package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.IOSActionRow
import com.today.habit.ui.component.IOSGroup
import com.today.habit.ui.component.IOSGlyphTile
import com.today.habit.ui.component.IOSNavBar
import com.today.habit.ui.component.iosPressable
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.viewmodel.HabitViewModel

/**
 * 删除习惯确认页面（iOS 红色操作组，两步确认）。
 * 第一次点击删除行进入"确认删除"状态（按钮变红），再次点击才真正删除。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteHabitScreen(navController: NavController, viewModel: HabitViewModel, habitId: String) {
    val id = habitId.toLongOrNull()
    val habits by viewModel.allHabits.observeAsState(emptyList())
    val habit = habits.find { it.id == id }

    var armed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(habit?.id) { if (habit == null) armed = false }

    Scaffold(
        topBar = {
            IOSNavBar(
                title = "删除习惯",
                onBack = { navController.popBackStack() },
                elevated = true
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            if (habit != null) {
                // 习惯概览
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IOSGlyphTile(
                        iconKey = HabitIcons.resKey(habit.icon),
                        tint = IOSColors.red,
                        size = 72.dp,
                        radius = 18.dp,
                        glyphSize = 36.dp
                    )
                    Text(
                        "删除「${habit.name}」？",
                        style = IOSType.title2,
                        color = IOSColors.label,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "该习惯的全部打卡记录将一并删除，且无法恢复。",
                        style = IOSType.subhead,
                        color = IOSColors.secondaryLabel,
                        textAlign = TextAlign.Center
                    )
                }

                // iOS 红色操作组
                IOSGroup {
                    if (armed) {
                        // 确认态：整行变红
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(IOSColors.red)
                                .iosPressable {
                                    viewModel.deleteHabit(habit)
                                    navController.popBackStack()
                                }
                                .padding(vertical = 13.dp)
                        ) {
                            Text("确认删除", style = IOSType.body, color = Color.White)
                        }
                    } else {
                        IOSActionRow(
                            label = "删除习惯",
                            color = IOSColors.red,
                            onClick = { armed = true }
                        )
                    }
                }
                if (armed) {
                    IOSGroup {
                        IOSActionRow(
                            label = "取消",
                            color = IOSColors.blue,
                            onClick = { armed = false }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("习惯不存在或已被删除", style = IOSType.body, color = IOSColors.secondaryLabel)
                }
            }
        }
    }
}
