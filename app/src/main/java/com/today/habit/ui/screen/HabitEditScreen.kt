package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.today.habit.ui.component.GlassCard
import com.today.habit.ui.component.HabitAccents
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.IOSFormTextField
import com.today.habit.ui.component.IOSSegmentedControl
import com.today.habit.ui.component.IOSSlider
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.SectionHeader
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.viewmodel.HabitViewModel

/**
 * 新建/编辑习惯 2.0：iOS 分组表单 + 外观（图标/配色）+ 目标步进器 + 频率芯片。
 * 路由：habit_edit/new 新建，habit_edit/{id} 编辑。
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
    var colorIdx by rememberSaveable { mutableStateOf(5) }

    LaunchedEffect(existing) {
        if (!initialized && existing != null) {
            name = existing.name
            frequency = existing.frequency
            frequencyValue = existing.frequencyValue
            targetCount = existing.targetCount
            icon = existing.icon
            colorIdx = if (existing.color in HabitAccents.indices) existing.color else 5
            initialized = true
        }
    }

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
            viewModel.insertHabit(name.trim(), "", frequency, frequencyValue, icon, targetCount, colorIdx)
        } else if (existing != null) {
            viewModel.updateHabit(
                existing.copy(
                    name = name.trim(), frequency = frequency, frequencyValue = frequencyValue,
                    icon = icon, targetCount = targetCount, color = colorIdx
                )
            )
        }
        navController.popBackStack()
    }

    val accent = HabitAccents[colorIdx]

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isNew) "新建习惯" else "编辑习惯", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(SFIcons.res("chevron.left")), contentDescription = "返回", modifier = Modifier.size(22.dp))
                    }
                },
                actions = {
                    TextButton(onClick = { save() }, enabled = name.isNotBlank()) {
                        Text(
                            "保存",
                            color = if (name.isNotBlank()) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
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
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 实时预览
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HabitTile(iconRes = HabitIcons.getRes(icon), accent = accent, size = 56.dp, iconSize = 27.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (name.isBlank()) "习惯名称" else name,
                            fontSize = 18.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onBackground,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "${freqLabel(frequency)} · 目标 $targetCount 次",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("基本信息")
                GlassCard {
                    IOSFormTextField(value = name, onValueChange = { if (it.length <= 20) name = it }, placeholder = "例如：早起跑步")
                    Spacer(Modifier.height(4.dp))
                    Text("${name.length}/20", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.End))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("外观")
                GlassCard {
                    // 图标行
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.currentBackStackEntry?.savedStateHandle?.set("current_icon", icon)
                                navController.navigate("icon_picker/$icon")
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        HabitTile(iconRes = HabitIcons.getRes(icon), accent = accent, size = 44.dp, iconSize = 21.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("图标", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                SFIcons.label(HabitIcons.resKey(icon)),
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            painter = painterResource(SFIcons.res("chevron.right")), contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text("配色", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        HabitAccents.forEachIndexed { idx, a ->
                            val sel = idx == colorIdx
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(38.dp)
                                    .shadow(if (sel) 6.dp else 0.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(a.main, a.gradientEnd)))
                                    .border(
                                        if (sel) 2.dp else 0.dp,
                                        if (sel) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { colorIdx = idx }
                            ) {
                                if (sel) {
                                    Icon(
                                        painter = painterResource(SFIcons.res("checkmark")),
                                        contentDescription = null, tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("目标次数")
                GlassCard {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("每次完成计为", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StepperBtn("-", enabled = targetCount > 1) { if (targetCount > 1) targetCount-- }
                            Spacer(Modifier.width(12.dp))
                            Text("$targetCount 次", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accent.main)
                            Spacer(Modifier.width(12.dp))
                            StepperBtn("+", enabled = targetCount < 10) { if (targetCount < 10) targetCount++ }
                        }
                    }
                    IOSSlider(
                        value = targetCount.toFloat(),
                        onValueChange = { targetCount = it.toInt() },
                        valueRange = 1f..10f, steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader("重复周期")
                GlassCard {
                    IOSSegmentedControl(
                        options = listOf("DAILY" to "每天", "WEEKDAYS" to "工作日", "WEEKLY" to "每周", "MONTHLY" to "每月"),
                        selected = frequency,
                        onSelect = {
                            frequency = it
                            if (it == "DAILY" || it == "WEEKDAYS") frequencyValue = ""
                            if (it == "WEEKLY" && frequencyValue.isBlank()) frequencyValue = "1 3 5"
                            if (it == "MONTHLY" && frequencyValue.isBlank()) frequencyValue = "1 15"
                        }
                    )
                    if (frequency == "WEEKLY") {
                        Spacer(Modifier.height(12.dp))
                        WeekdayChips(frequencyValue) { frequencyValue = it }
                    }
                    if (frequency == "MONTHLY") {
                        Spacer(Modifier.height(12.dp))
                        IOSFormTextField(
                            value = frequencyValue, onValueChange = { frequencyValue = it },
                            placeholder = "例如：1 15（每月几号）"
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1", "1 15", "10 20").forEach { preset ->
                                val sel = frequencyValue == preset
                                Box(
                                    Modifier.clip(RoundedCornerShape(12.dp))
                                        .background(if (sel) accent.soft else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { frequencyValue = preset }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                ) {
                                    Text(preset, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (sel) accent.main else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }

            // 底部大保存键
            val saveModifier = if (name.isBlank()) {
                Modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            } else {
                Modifier.background(Brush.linearGradient(listOf(Color(0xFF5BE584), ThemeGreen)))
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = ThemeGreen.copy(alpha = 0.4f))
                    .clip(RoundedCornerShape(18.dp))
                    .then(saveModifier)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { save() }
                    .padding(vertical = 15.dp)
            ) {
                Text(
                    if (isNew) "开始坚持" else "保存修改",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun freqLabel(f: String) = when (f) {
    "DAILY" -> "每天"
    "WEEKDAYS" -> "工作日"
    "WEEKLY" -> "每周"
    "MONTHLY" -> "每月"
    else -> "每天"
}

@Composable
private fun StepperBtn(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(32.dp).clip(CircleShape)
            .background(
                if (enabled) ThemeGreen.copy(alpha = 0.13f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { if (enabled) onClick() }
    ) {
        Text(
            text, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            color = if (enabled) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

/** 每周星期选择芯片：一 二 三 四 五 六 日 */
@Composable
private fun WeekdayChips(value: String, onChange: (String) -> Unit) {
    val selected = remember(value) { value.split(" ").filter { it.isNotBlank() }.toSet() }
    val names = listOf("一", "二", "三", "四", "五", "六", "日")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        names.forEachIndexed { idx, n ->
            val day = (idx + 1).toString()
            val sel = selected.contains(day)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(if (sel) ThemeGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val next = selected.toMutableSet()
                        if (sel) next.remove(day) else next.add(day)
                        onChange(next.sortedBy { it.toIntOrNull() ?: 0 }.joinToString(" "))
                    }
            ) {
                Text(n, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
    if (selected.isEmpty()) {
        Spacer(Modifier.height(6.dp))
        Text("请至少选择一天", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
    }
}
