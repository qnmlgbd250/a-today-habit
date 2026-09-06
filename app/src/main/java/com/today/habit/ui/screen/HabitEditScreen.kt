package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.viewmodel.HabitViewModel

/**
 * 新建/编辑习惯的统一表单页面（替代原弹窗）。
 * 路由：habit_edit/new 为新建，habit_edit/{id} 为编辑。
 * 图标选择跳转图标库页面，结果经 savedStateHandle 的 picked_icon 回传。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditScreen(navController: NavController, viewModel: HabitViewModel, habitId: String) {
    val isNew = habitId == "new"
    val id = habitId.toLongOrNull()
    val habits by viewModel.allHabits.observeAsState(emptyList())
    val existing = if (isNew) null else habits.find { it.id == id }

    var initialized by rememberSaveable { mutableStateOf(isNew) }
    var name by rememberSaveable { mutableStateOf("") }
    var frequency by rememberSaveable { mutableStateOf("DAILY") }
    var frequencyValue by rememberSaveable { mutableStateOf("") }
    var targetCount by rememberSaveable { mutableStateOf(1) }
    var icon by rememberSaveable { mutableStateOf(HabitIcons.DefaultIcon) }

    LaunchedEffect(existing) {
        if (!initialized && existing != null) {
            name = existing.name
            frequency = existing.frequency
            frequencyValue = existing.frequencyValue
            targetCount = existing.targetCount
            icon = existing.icon
            initialized = true
        }
    }

    // 接收图标选择页回传的结果
    val pickedIcon = navController.currentBackStackEntry?.savedStateHandle
        ?.getStateFlow("picked_icon", "")?.collectAsState()?.value
    LaunchedEffect(pickedIcon) {
        if (!pickedIcon.isNullOrEmpty()) {
            icon = pickedIcon
            navController.currentBackStackEntry?.savedStateHandle?.set("picked_icon", "")
        }
    }

    fun save() {
        if (name.isBlank()) return
        if (isNew) {
            viewModel.insertHabit(name, "", frequency, frequencyValue, icon, targetCount)
        } else if (existing != null) {
            viewModel.updateHabit(existing.copy(name = name, frequency = frequency, frequencyValue = frequencyValue, icon = icon, targetCount = targetCount))
        }
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "新建习惯" else "编辑习惯", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(SFIcons.res("chevron.left")),
                            contentDescription = "返回",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { save() },
                        enabled = name.isNotBlank()
                    ) {
                        Text("保存", color = if (name.isNotBlank()) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 图标（跳转图标库页面）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        navController.currentBackStackEntry?.savedStateHandle?.set("current_icon", icon)
                        navController.navigate("icon_picker/$icon")
                    }
                    .padding(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ThemeGreen.copy(alpha = 0.15f))
                ) {
                    Icon(
                        painter = painterResource(HabitIcons.getRes(icon)),
                        contentDescription = null,
                        tint = ThemeGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("图标", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(SFIcons.label(HabitIcons.resKey(icon)), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
                Icon(
                    painter = painterResource(SFIcons.res("chevron.right")),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }

            // 名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("习惯名称") },
                placeholder = { Text("例如：早起跑步") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
            )

            // 目标次数
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
                    Text(targetLabel, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$targetCount 次", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = ThemeGreen)
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

            // 重复周期
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("重复周期", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        placeholder = { Text(if (frequency == "WEEKLY") "例如: 1 3 5 (周几)" else "例如: 1 15 (几号)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ThemeGreen, focusedLabelColor = ThemeGreen)
                    )
                }
            }
        }
    }
}
