package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.GlassCard
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.accent
import com.today.habit.ui.component.frequencyLabel
import com.today.habit.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsScreen(navController: NavController, viewModel: HabitViewModel) {
    val habits by viewModel.allHabits.observeAsState(emptyList())

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("管理习惯", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(SFIcons.res("chevron.left")), contentDescription = "返回", modifier = Modifier.size(22.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("habit_edit/new") }) {
                        Icon(painter = painterResource(SFIcons.res("plus")), contentDescription = "新建", modifier = Modifier.size(22.dp))
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
        if (habits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无习惯，点击右上角新建", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    HabitManageItem(
                        habit = habit,
                        onEdit = { navController.navigate("habit_edit/${habit.id}") },
                        onDelete = { navController.navigate("habit_delete/${habit.id}") }
                    )
                }
            }
        }
    }
}

@Composable
fun HabitManageItem(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = remember(habit.id, habit.color) { habit.accent() }
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HabitTile(iconRes = HabitIcons.getRes(habit.icon), accent = accent, size = 48.dp, iconSize = 23.dp)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(3.dp))
                Text(
                    "${habit.frequencyLabel()} · 目标 ${habit.targetCount} 次",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    painter = painterResource(SFIcons.res("pencil")), contentDescription = "编辑",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    painter = painterResource(SFIcons.res("trash")), contentDescription = "删除",
                    modifier = Modifier.size(18.dp), tint = Color(0xFFFF3B30)
                )
            }
        }
    }
}
