package com.today.habit.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.today.habit.data.AppDatabase
import com.today.habit.data.HabitRepository
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.HandDrawnSun
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.viewmodel.HabitViewModel
import com.today.habit.ui.viewmodel.HabitViewModelFactory
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

// 定义全局统一的主题绿
val ThemeGreen = Color(0xFF4CAF50)
val ThemeGreenDark = Color(0xFF2E7D32)
val ThemeGreenLight = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = HabitRepository(database.habitDao())
    val viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory(repository))

    val allHabits by viewModel.allHabits.observeAsState(emptyList())
    val selectedDate by viewModel.selectedDate
    val checkIns by viewModel.getCheckInsByDate(selectedDate.toString()).observeAsState(emptyList())
    
    val filteredHabits = viewModel.getFilteredHabits(allHabits, selectedDate)
    
    var showDateBar by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showManageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showDateBar = !showDateBar }
                        ) {
                            Text(
                                text = if (selectedDate == LocalDate.now()) "今日习惯" else selectedDate.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                if (showDateBar) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.Add, contentDescription = "操作")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                offset = androidx.compose.ui.unit.DpOffset(0.dp, 8.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("新建习惯", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.AddCircle, contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        showAddDialog = true
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                                DropdownMenuItem(
                                    text = { Text("管理习惯", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        showManageDialog = true
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                if (showDateBar) {
                    DateSelectionBar(selectedDate) { viewModel.setSelectedDate(it) }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (filteredHabits.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("该日期没有需要完成的习惯", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredHabits) { habit ->
                    val checkIn = checkIns.find { it.habitId == habit.id }
                    HabitGridItem(habit, checkIn) {
                        viewModel.toggleCheckIn(habit.id, selectedDate.toString(), habit.targetCount)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, freq, freqVal, icon, target ->
                viewModel.insertHabit(name, desc, freq, freqVal, icon, target)
                showAddDialog = false
            }
        )
    }

    if (showManageDialog) {
        ManageHabitsDialog(
            habits = allHabits,
            onDismiss = { showManageDialog = false },
            onUpdate = { viewModel.updateHabit(it) },
            onDelete = { viewModel.deleteHabit(it) }
        )
    }
}

@Composable
fun DateSelectionBar(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val dates = remember {
        (-30..30).map { LocalDate.now().plusDays(it.toLong()) }
    }
    val listState = rememberLazyListState()
    
    LaunchedEffect(selectedDate) {
        val index = dates.indexOf(selectedDate)
        if (index >= 0) {
            listState.scrollToItem(maxOf(0, index - 3))
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            Column(
                modifier = Modifier
                    .width(45.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) ThemeGreen else Color.Transparent)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
                    fontSize = 11.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isToday) (if (isSelected) Color.White else ThemeGreen) 
                            else Color.Transparent
                        )
                )
            }
        }
    }
}

@Composable
fun HabitGridItem(habit: Habit, checkInRecord: CheckInRecord?, onClick: () -> Unit) {
    val currentCount = checkInRecord?.count ?: 0
    val targetCount = habit.targetCount
    val isCompleted = currentCount >= targetCount
    
    val progress = animateFloatAsState(
        targetValue = currentCount.toFloat() / targetCount.toFloat(),
        animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing),
        label = "ProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(64.dp)) {
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                
                if (progress.value > 0f) {
                    drawArc(
                        color = ThemeGreen,
                        startAngle = -90f,
                        sweepAngle = 360f * progress.value,
                        useCenter = false,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            
            AnimatedContent(
                targetState = isCompleted,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                },
                label = "IconChange"
            ) { completed ->
                if (completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = ThemeGreenDark
                    )
                } else {
                    Icon(
                        imageVector = HabitIcons.getIcon(habit.icon),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = habit.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "$currentCount/$targetCount",
            style = MaterialTheme.typography.labelSmall,
            color = if (isCompleted) ThemeGreenDark else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("DAILY") }
    var frequencyValue by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("Sunny") }
    var targetCount by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("新建习惯", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("习惯名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                )
                
                Text("选择图标", style = MaterialTheme.typography.titleMedium)
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
                                    .background(if (selectedIcon == name) ThemeGreenLight else Color.Transparent)
                                    .clickable { selectedIcon = name }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, contentDescription = null, tint = if (selectedIcon == name) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Text("每日目标次数: $targetCount", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = targetCount.toFloat(),
                    onValueChange = { targetCount = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(thumbColor = ThemeGreen, activeTrackColor = ThemeGreen)
                )
                
                Text("选择周期", style = MaterialTheme.typography.titleMedium)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DAILY" to "每天", "WEEKDAYS" to "工作日", "WEEKLY" to "每周", "MONTHLY" to "每月").forEach { (id, label) ->
                        FilterChip(
                            selected = frequency == id,
                            onClick = { frequency = id; frequencyValue = "" },
                            label = { Text(label) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ThemeGreen, selectedLabelColor = Color.White)
                        )
                    }
                }
                
                if (frequency == "WEEKLY" || frequency == "MONTHLY") {
                    OutlinedTextField(
                        value = frequencyValue, 
                        onValueChange = { frequencyValue = it }, 
                        placeholder = { Text(if(frequency == "WEEKLY") "例如: 1,3,5 (周几)" else "例如: 1,15 (几号)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(name, "", frequency, frequencyValue, selectedIcon, targetCount) }, 
                        enabled = name.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ThemeGreen)
                    ) {
                        Text("创建")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsDialog(
    habits: List<Habit>,
    onDismiss: () -> Unit,
    onUpdate: (Habit) -> Unit,
    onDelete: (Habit) -> Unit
) {
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("管理习惯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "关闭") } },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                }
            ) { padding ->
                if (habits.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text("暂无习惯", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(habits) { habit ->
                            HabitManageItem(habit, onEdit = { habitToEdit = habit }, onDelete = { habitToDelete = habit })
                        }
                    }
                }
            }
        }
    }

    if (habitToEdit != null) {
        EditHabitDialog(habit = habitToEdit!!, onDismiss = { habitToEdit = null }, onUpdate = { onUpdate(it); habitToEdit = null })
    }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("删除习惯") },
            text = { Text("确定要删除习惯“${habitToDelete!!.name}”吗？") },
            confirmButton = { TextButton(onClick = { onDelete(habitToDelete!!); habitToDelete = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("删除") } },
            dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("取消") } }
        )
    }
}

@Composable
fun HabitManageItem(habit: Habit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 习惯图标：使用非常淡的灰色背景
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = HabitIcons.getIcon(habit.icon),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "目标: ${habit.targetCount} 次/日",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 操作按钮：统一使用中性色，避免红绿冲突
            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OutlinedFlag, // 改用更轻盈的图标
                        contentDescription = "编辑",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline, // 使用描边型图标更高级
                        contentDescription = "删除",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("编辑习惯", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("习惯名称") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen))
                Text("选择图标", style = MaterialTheme.typography.titleMedium)
                LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitIcons.IconsMap.forEach { (name, icon) ->
                        item {
                            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(if (selectedIcon == name) ThemeGreenLight else Color.Transparent).clickable { selectedIcon = name }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Icon(icon, contentDescription = null, tint = if (selectedIcon == name) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Text("每日目标次数: $targetCount", style = MaterialTheme.typography.titleMedium)
                Slider(value = targetCount.toFloat(), onValueChange = { targetCount = it.toInt() }, valueRange = 1f..10f, steps = 8, colors = SliderDefaults.colors(thumbColor = ThemeGreen, activeTrackColor = ThemeGreen))
                Text("选择周期", style = MaterialTheme.typography.titleMedium)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("DAILY" to "每天", "WEEKDAYS" to "工作日", "WEEKLY" to "每周", "MONTHLY" to "每月").forEach { (id, label) ->
                        FilterChip(selected = frequency == id, onClick = { frequency = id; if (id == "DAILY" || id == "WEEKDAYS") frequencyValue = "" }, label = { Text(label) }, shape = RoundedCornerShape(8.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ThemeGreen, selectedLabelColor = Color.White))
                    }
                }
                if (frequency == "WEEKLY" || frequency == "MONTHLY") {
                    OutlinedTextField(value = frequencyValue, onValueChange = { frequencyValue = it }, placeholder = { Text(if(frequency == "WEEKLY") "例如: 1,3,5 (周几)" else "例如: 1,15 (几号)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) { Text("取消") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onUpdate(habit.copy(name = name, frequency = frequency, frequencyValue = frequencyValue, icon = selectedIcon, targetCount = targetCount)) }, enabled = name.isNotBlank(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ThemeGreen)) { Text("保存") }
                }
            }
        }
    }
}
