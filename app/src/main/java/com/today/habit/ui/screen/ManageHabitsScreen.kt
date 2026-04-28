package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.today.habit.data.AppDatabase
import com.today.habit.data.HabitRepository
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.theme.ThemeGreenDark
import com.today.habit.ui.viewmodel.HabitViewModel
import com.today.habit.ui.viewmodel.HabitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsScreen(navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = HabitRepository(database.habitDao())
    val viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory(repository))
    
    val habits by viewModel.allHabits.observeAsState(emptyList())
    
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = { Text("管理习惯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = { 
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回") 
                    } 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (habits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无习惯", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding), 
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 1.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits) { habit ->
                    HabitManageItem(
                        habit = habit, 
                        onEdit = { habitToEdit = habit }, 
                        onDelete = { habitToDelete = habit }
                    )
                }
            }
        }
    }

    if (habitToEdit != null) {
        EditHabitDialog(
            habit = habitToEdit!!, 
            onDismiss = { habitToEdit = null }, 
            onUpdate = { 
                viewModel.updateHabit(it)
                habitToEdit = null 
            }
        )
    }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("删除习惯") },
            text = { Text("确定要删除习惯“${habitToDelete!!.name}”吗？") },
            confirmButton = { 
                TextButton(
                    onClick = { 
                        viewModel.deleteHabit(habitToDelete!!)
                        habitToDelete = null 
                    }, 
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") } 
            },
            dismissButton = { 
                TextButton(onClick = { habitToDelete = null }) { Text("取消") } 
            }
        )
    }
}

@Composable
fun HabitManageItem(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 习惯图标
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HabitIcons.getIcon(habit.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val frequencyLabel = when (habit.frequency) {
                    "DAILY" -> "日"
                    "WEEKDAYS" -> "工作日"
                    "WEEKLY" -> "周"
                    "MONTHLY" -> "月"
                    else -> "日"
                }
                Text(
                    text = "目标: ${habit.targetCount} 次/$frequencyLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 操作按钮
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "编辑",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "删除",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditHabitDialog(habit: Habit, onDismiss: () -> Unit, onUpdate: (Habit) -> Unit) {
    var name by remember { mutableStateOf(habit.name) }
    var frequency by remember { mutableStateOf(habit.frequency) }
    var frequencyValue by remember { mutableStateOf(habit.frequencyValue) }
    var selectedIcon by remember { mutableStateOf(habit.icon) }
    var targetCount by remember { mutableStateOf(habit.targetCount) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "编辑习惯",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // 基础信息
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("习惯名称") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp), 
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                )

                // 图标选择
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "选择图标", 
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HabitIcons.IconsMap.forEach { (name, icon) ->
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedIcon == name) ThemeGreen.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(
                                            width = 1.dp,
                                            color = if (selectedIcon == name) ThemeGreen else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedIcon = name }
                                        .padding(8.dp), 
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon, 
                                        contentDescription = null, 
                                        tint = if (selectedIcon == name) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 目标设定
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val targetLabel = when (frequency) {
                            "DAILY" -> "目标次数 (每日)"
                            "WEEKDAYS" -> "目标次数 (工作日)"
                            "WEEKLY" -> "目标次数 (每周)"
                            "MONTHLY" -> "目标次数 (每月)"
                            else -> "目标次数 (每日)"
                        }
                        Text(
                            targetLabel, 
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$targetCount 次", 
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = ThemeGreen
                        )
                    }
                    Slider(
                        value = targetCount.toFloat(),
                        onValueChange = { targetCount = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.height(24.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = ThemeGreen,
                            activeTrackColor = ThemeGreen,
                            inactiveTrackColor = ThemeGreen.copy(alpha = 0.2f)
                        )
                    )
                }

                // 周期选择
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "重复周期", 
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("DAILY" to "每天", "WEEKDAYS" to "工作日", "WEEKLY" to "每周", "MONTHLY" to "每月").forEach { (id, label) ->
                            FilterChip(
                                selected = frequency == id, 
                                onClick = { frequency = id; if (id == "DAILY" || id == "WEEKDAYS") frequencyValue = "" }, 
                                label = { Text(label, fontSize = 13.sp) }, 
                                shape = RoundedCornerShape(12.dp), 
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ThemeGreen, 
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                ),
                                border = null
                            )
                        }
                    }
                    
                    if (frequency == "WEEKLY" || frequency == "MONTHLY") {
                        OutlinedTextField(
                            value = frequencyValue, 
                            onValueChange = { frequencyValue = it }, 
                            placeholder = { Text(if(frequency == "WEEKLY") "例如: 1,3,5 (周几)" else "例如: 1,15 (几号)") }, 
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp), 
                            shape = RoundedCornerShape(12.dp), 
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                        )
                    }
                }

                // 底部按钮
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp), 
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { onUpdate(habit.copy(name = name, frequency = frequency, frequencyValue = frequencyValue, icon = selectedIcon, targetCount = targetCount)) }, 
                        enabled = name.isNotBlank(), 
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp), 
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeGreen)
                    ) { 
                        Text("保存") 
                    }
                }
            }
        }
    }
}
