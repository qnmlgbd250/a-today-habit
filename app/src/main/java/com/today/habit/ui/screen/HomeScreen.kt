package com.today.habit.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.today.habit.data.AppDatabase
import com.today.habit.data.HabitRepository
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.CheckInSoundPlayer
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.viewmodel.HabitViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.theme.ThemeGreenDark
import com.today.habit.ui.theme.ThemeGreenLight

import com.today.habit.data.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HabitViewModel) {
    val context = LocalContext.current
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
    
    var showDateBar by rememberSaveable { mutableStateOf(false) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    // 新建习惯时选中的图标（跳转图标选择页后需保留）
    var addIcon by rememberSaveable { mutableStateOf(HabitIcons.DefaultIcon) }

    // 接收图标选择页回传的结果
    val pickedIcon = navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow("picked_icon", "")?.collectAsState()?.value
    LaunchedEffect(pickedIcon) {
        if (!pickedIcon.isNullOrEmpty()) {
            addIcon = pickedIcon
            navController.currentBackStackEntry?.savedStateHandle?.set("picked_icon", "")
        }
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
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
                                painterResource(SFIcons.res(if (showDateBar) "chevron.up" else "chevron.down")),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(painterResource(SFIcons.res("plus")), contentDescription = "操作")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                offset = androidx.compose.ui.unit.DpOffset(0.dp, 8.dp)
                            ) {
                                val isDark by viewModel.isDarkTheme
                                DropdownMenuItem(
                                    text = { Text(if (isDark) "浅色模式" else "深色模式", fontWeight = FontWeight.Medium) },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(SFIcons.res(if (isDark) "sun.max" else "moon")),
                                            contentDescription = null,
                                            tint = ThemeGreen
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        viewModel.toggleTheme()
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("新建习惯", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(painterResource(SFIcons.res("plus")), contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        showAddDialog = true
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("管理习惯", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(painterResource(SFIcons.res("gearshape")), contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        navController.navigate("manage_habits")
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("备份数据", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(painterResource(SFIcons.res("square.and.arrow.up")), contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        exportLauncher.launch("habit_backup_${LocalDate.now()}.json")
                                    }
                                )
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                DropdownMenuItem(
                                    text = { Text("恢复数据", fontWeight = FontWeight.Medium) },
                                    leadingIcon = { Icon(painterResource(SFIcons.res("clock.arrow.circlepath")), contentDescription = null, tint = ThemeGreen) },
                                    onClick = {
                                        showMenu = false
                                        importLauncher.launch(arrayOf("application/json"))
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { showDateBar = false }
        ) {
            if (filteredHabits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("该日期没有需要完成的习惯", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
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
    }

    if (showAddDialog) {
        AddHabitDialog(
            icon = addIcon,
            onPickIcon = {
                navController.currentBackStackEntry?.savedStateHandle?.set("current_icon", addIcon)
                navController.navigate("icon_picker/$addIcon")
            },
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, freq, freqVal, icon, target ->
                viewModel.insertHabit(name, desc, freq, freqVal, icon, target)
                showAddDialog = false
                addIcon = HabitIcons.DefaultIcon
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
                    text = "${date.monthValue}/${date.dayOfMonth}",
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
    val context = LocalContext.current
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
            ) {
                CheckInSoundPlayer.playCompletionSound(context)
                onClick()
            }
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
                        painter = painterResource(HabitIcons.getRes(habit.icon)),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = ThemeGreenDark
                    )
                } else {
                    Icon(
                        painter = painterResource(HabitIcons.getRes(habit.icon)),
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
fun AddHabitDialog(
    icon: String,
    onPickIcon: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, Int) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf("DAILY") }
    var frequencyValue by rememberSaveable { mutableStateOf("") }
    var targetCount by rememberSaveable { mutableStateOf(1) }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { onPickIcon() }
                            .padding(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ThemeGreen.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                painter = painterResource(HabitIcons.getRes(icon)),
                                contentDescription = null,
                                tint = ThemeGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            SFIcons.label(HabitIcons.resKey(icon)),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            painter = painterResource(SFIcons.res("chevron.right")),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
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
                            placeholder = { Text(if(frequency == "WEEKLY") "例如: 1 3 5 (周几)" else "例如: 1 15 (几号)") },
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                        )
                    }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))) {
                        Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = { onConfirm(name, "", frequency, frequencyValue, icon, targetCount) }, enabled = name.isNotBlank(), modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = ThemeGreen)) {
                        Text("创建")
                    }
                }
            }
        }
    }
    }
}
