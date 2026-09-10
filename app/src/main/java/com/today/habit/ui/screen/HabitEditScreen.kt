package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.IOSActionRow
import com.today.habit.ui.component.IOSChevron
import com.today.habit.ui.component.IOSDivider
import com.today.habit.ui.component.IOSGlyphTile
import com.today.habit.ui.component.IOSGroup
import com.today.habit.ui.component.IOSInlineTextField
import com.today.habit.ui.component.IOSLargeTitle
import com.today.habit.ui.component.IOSNavAction
import com.today.habit.ui.component.IOSNavBar
import com.today.habit.ui.component.IOSRow
import com.today.habit.ui.component.IOSSegmentedControl
import com.today.habit.ui.component.IOSStepper
import com.today.habit.ui.component.IOSWeekdayPicker
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.iosElevatedCard
import com.today.habit.ui.component.iosEntrance
import com.today.habit.ui.component.iosPressable
import com.today.habit.ui.component.rememberIOSCollapsed
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.viewmodel.HabitViewModel

/**
 * 新建/编辑习惯的统一表单页面（iOS 设置表单风）。
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

    // 周几选择（frequencyValue 存 "1 3 5"，1=周一 … 7=周日）
    val selectedWeekdays = remember(frequencyValue) {
        frequencyValue.split(" ").mapNotNull { it.toIntOrNull() }.filter { it in 1..7 }.toSet()
    }

    fun save() {
        if (name.isBlank()) return
        if (isNew) {
            viewModel.insertHabit(name.trim(), "", frequency, frequencyValue, icon, targetCount)
        } else if (existing != null) {
            viewModel.updateHabit(
                existing.copy(
                    name = name.trim(),
                    frequency = frequency,
                    frequencyValue = frequencyValue,
                    icon = icon,
                    targetCount = targetCount
                )
            )
        }
        navController.popBackStack()
    }

    val listState = rememberLazyListState()
    val collapsed = rememberIOSCollapsed(listState)
    val title = if (isNew) "新建习惯" else "编辑习惯"

    Scaffold(
        topBar = {
            IOSNavBar(
                title = title,
                onBack = { navController.popBackStack() },
                showTitle = collapsed,
                elevated = collapsed,
                actions = {
                    IOSNavAction(label = "保存", enabled = name.isNotBlank(), onClick = ::save)
                }
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Box(modifier = Modifier.iosEntrance("edit:title", 0)) {
                    IOSLargeTitle(title = title)
                }
            }
            // 内容组
            item {
                Box(modifier = Modifier.iosEntrance("edit:content", 1)) {
                IOSGroup {
                    // 图标行
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .iosElevatedCard(16.dp)
                            .iosPressable {
                                navController.currentBackStackEntry?.savedStateHandle?.set("current_icon", icon)
                                navController.navigate("icon_picker/$icon")
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        IOSGlyphTile(HabitIcons.resKey(icon), IOSColors.label)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("图标", style = IOSType.footnote, color = IOSColors.secondaryLabel)
                            Text(
                                SFIcons.label(HabitIcons.resKey(icon)),
                                style = IOSType.body,
                                color = IOSColors.label
                            )
                        }
                        IOSChevron()
                    }
                    IOSDivider()
                    // 名称行
                    IOSRow {
                        IOSInlineTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "名称，例如：早起跑步"
                        )
                    }
                }
                }
            }
            // 目标组
            item {
                Box(modifier = Modifier.iosEntrance("edit:goal", 2)) {
                IOSGroup(
                    header = "目标",
                    footer = when (frequency) {
                        "WEEKLY" -> "选择每周重复的星期。"
                        "MONTHLY" -> "输入每月重复的日期，多个日期用空格分隔。"
                        else -> null
                    }
                ) {
                    IOSRow(
                        trailing = {
                            IOSStepper(
                                value = targetCount,
                                onValueChange = { targetCount = it },
                                range = 1..10
                            )
                        }
                    ) {
                        Text(
                            "每次目标 ${targetCount} 次",
                            style = IOSType.body,
                            color = IOSColors.label
                        )
                    }
                    IOSDivider()
                    // 重复周期
                    IOSRow {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("重复", style = IOSType.body, color = IOSColors.label)
                            IOSSegmentedControl(
                                options = listOf(
                                    "DAILY" to "每天",
                                    "WEEKDAYS" to "工作日",
                                    "WEEKLY" to "每周",
                                    "MONTHLY" to "每月"
                                ),
                                selected = frequency,
                                onSelect = {
                                    frequency = it
                                    if (it == "DAILY" || it == "WEEKDAYS") frequencyValue = ""
                                }
                            )
                            if (frequency == "WEEKLY") {
                                IOSWeekdayPicker(
                                    selected = selectedWeekdays,
                                    onToggle = { day ->
                                        val next = selectedWeekdays.toMutableSet()
                                        if (!next.add(day)) next.remove(day)
                                        frequencyValue = next.sorted().joinToString(" ")
                                    }
                                )
                            }
                        }
                    }
                    if (frequency == "MONTHLY") {
                        IOSDivider()
                        IOSRow {
                            IOSInlineTextField(
                                value = frequencyValue,
                                onValueChange = { frequencyValue = it.filter { c -> c.isDigit() || c == ' ' } },
                                placeholder = "日期，例如：1 15"
                            )
                        }
                    }
                }
                }
            }
            // 删除入口（仅编辑既有习惯，iOS 通讯录式底部红色行）
            if (!isNew && existing != null) {
                item {
                    Box(modifier = Modifier.iosEntrance("edit:delete", 3)) {
                    IOSGroup {
                        IOSActionRow(
                            label = "删除习惯",
                            color = IOSColors.red,
                            onClick = { navController.navigate("habit_delete/${existing.id}") }
                        )
                    }
                    }
                }
            }
        }
    }
}
