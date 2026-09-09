package com.today.habit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.HairlineDivider
import com.today.habit.ui.component.InsetGroup
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.accent
import com.today.habit.ui.component.frequencyLabel
import com.today.habit.ui.component.pressable
import com.today.habit.ui.theme.Body17
import com.today.habit.ui.theme.Footnote13
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
                        Icon(painter = painterResource(SFIcons.res("chevron.left")), contentDescription = "返回", modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("habit_edit/new") }) {
                        Icon(painter = painterResource(SFIcons.res("plus")), contentDescription = "新建", modifier = Modifier.size(20.dp))
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
                Text("暂无习惯，点击右上角新建", style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    InsetGroup {
                        habits.forEachIndexed { index, habit ->
                            HabitManageRow(
                                habit = habit,
                                onEdit = { navController.navigate("habit_edit/${habit.id}") },
                                onDelete = { navController.navigate("habit_delete/${habit.id}") }
                            )
                            if (index < habits.size - 1) HairlineDivider(startIndent = 68.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HabitManageRow(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val accent = remember(habit.id, habit.color) { habit.accent() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .pressable(onClick = onEdit)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        HabitTile(iconRes = HabitIcons.getRes(habit.icon), accent = accent, size = 42.dp, iconSize = 20.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(habit.name, style = Body17, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
            Text(
                "${habit.frequencyLabel()} · 目标 ${habit.targetCount} 次",
                style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
            Icon(
                painter = painterResource(SFIcons.res("pencil")), contentDescription = "编辑",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
            Icon(
                painter = painterResource(SFIcons.res("trash")), contentDescription = "删除",
                modifier = Modifier.size(16.dp), tint = Color(0xFFFF3B30)
            )
        }
    }
}
