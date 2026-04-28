package com.today.habit.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
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
import androidx.compose.material.icons.outlined.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.today.habit.data.AppDatabase
import com.today.habit.data.HabitRepository
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.viewmodel.HabitViewModel
import com.today.habit.ui.viewmodel.HabitViewModelFactory
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.theme.ThemeGreenDark
import com.today.habit.ui.theme.ThemeGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = HabitRepository(database.habitDao())
    val viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory(repository))
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDateIfNecessary()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.exportDataJson()
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(json)
                        }
                    }
                    android.widget.Toast.makeText(context, "备份成功", android.widget.Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "备份失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            val json = reader.readText()
                            viewModel.importDataJson(json) { success, message ->
                                if (success) {
                                    android.widget.Toast.makeText(context, "恢复成功", android.widget.Toast.LENGTH_SHORT).show()
                                    val current = viewModel.selectedDate.value
                                    viewModel.setSelectedDate(current)
                                } else {
                                    android.widget.Toast.makeText(context, "恢复失败: $message", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.widget.Toast.makeText(context, "文件读取失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val allHabits by viewModel.allHabits.observeAsState(emptyList())
    val selectedDate by viewModel.selectedDate
    val checkIns by viewModel.getCheckInsByDate(selectedDate.toString()).observeAsState(emptyList())
    val filteredHabits = viewModel.getFilteredHabits(allHabits, selectedDate)
    
    var showDateBar by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

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
                                if (showDateBar) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Outlined.Add, contentDescription = "操作")
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
                                    leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        showAddDialog = true
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("管理习惯", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("manage_habits")
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("备份数据", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Backup, contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        exportLauncher.launch("habit_backup_${LocalDate.now()}.json")
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("恢复数据", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(Icons.Outlined.Restore, contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        importLauncher.launch(arrayOf("application/json"))
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
                AnimatedVisibility(
                    visible = showDateBar,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
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
}

@Composable
fun DateSelectionBar(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val dates = remember(today) {
        (-30..30).map { today.plusDays(it.toLong()) }
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
                        imageVector = Icons.Outlined.Check,
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
                    "新建习惯",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name, 
                        onValueChange = { name = it }, 
                        label = { Text("习惯名称") },
                        placeholder = { Text("例如：早起跑步") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("选择图标", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HabitIcons.IconsMap.forEach { (name, icon) ->
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedIcon == name) ThemeGreen.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(width = 1.dp, color = if (selectedIcon == name) ThemeGreen else Color.Transparent, shape = CircleShape)
                                        .clickable { selectedIcon = name }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = null, tint = if (selectedIcon == name) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        val targetLabel = when (frequency) {
                            "DAILY" -> "目标次数 (每日)"
                            "WEEKDAYS" -> "目标次数 (工作日)"
                            "WEEKLY" -> "目标次数 (每周)"
                            "MONTHLY" -> "目标次数 (每月)"
                            else -> "目标次数 (每日)"
                        }
                        Text(targetLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$targetCount 次", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = ThemeGreen)
                    }
                    Slider(
                        value = targetCount.toFloat(),
                        onValueChange = { targetCount = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.height(24.dp),
                        colors = SliderDefaults.colors(thumbColor = ThemeGreen, activeTrackColor = ThemeGreen, inactiveTrackColor = ThemeGreen.copy(alpha = 0.2f))
                    )
                }
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("重复周期", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        listOf("DAILY" to "每天", "WEEKDAYS" to "工作日", "WEEKLY" to "每周", "MONTHLY" to "每月").forEach { (id, label) ->
                            FilterChip(
                                selected = frequency == id,
                                onClick = { frequency = id; frequencyValue = "" },
                                label = { Text(label, fontSize = 13.sp) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ThemeGreen, selectedLabelColor = Color.White, containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
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
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { onConfirm(name, "", frequency, frequencyValue, selectedIcon, targetCount) }, enabled = name.isNotBlank(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = ThemeGreen)) {
                        Text("创建")
                    }
                }
            }
        }
    }
}
