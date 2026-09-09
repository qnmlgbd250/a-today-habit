package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.today.habit.ui.component.HabitAccents
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.HairlineDivider
import com.today.habit.ui.component.IOSSegmentedControl
import com.today.habit.ui.component.InsetGroup
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.SectionFooter
import com.today.habit.ui.component.SectionLabel
import com.today.habit.ui.component.isDark
import com.today.habit.ui.component.pressable
import com.today.habit.ui.theme.Body17
import com.today.habit.ui.theme.Footnote13
import com.today.habit.ui.theme.TabularNum
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.viewmodel.HabitViewModel

/**
 * 新建/编辑习惯 3.0：iOS 设置式分组表单。
 * 纯平配色、无衬线输入、分组脚注说明，保存收敛到导航栏。
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
    var colorIdx by rememberSaveable { mutableStateOf(6) }

    LaunchedEffect(existing) {
        if (!initialized && existing != null) {
            name = existing.name
            frequency = existing.frequency
            frequencyValue = existing.frequencyValue
            targetCount = existing.targetCount
            icon = existing.icon
            colorIdx = if (existing.color in HabitAccents.indices) existing.color else 6
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
    val dark = isDark()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isNew) "新建习惯" else "编辑习惯", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painter = painterResource(SFIcons.res("chevron.left")), contentDescription = "返回", modifier = Modifier.size(20.dp))
                    }
                },
                actions = {
                    TextButton(onClick = { save() }, enabled = name.isNotBlank()) {
                        Text(
                            "保存",
                            color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else ThemeGreen,
                            fontWeight = FontWeight.SemiBold, fontSize = 16.sp
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 实时预览
            InsetGroup {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    HabitTile(iconRes = HabitIcons.getRes(icon), accent = accent, size = 48.dp, iconSize = 23.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (name.isBlank()) "习惯名称" else name,
                            style = Body17, fontWeight = FontWeight.SemiBold,
                            color = if (name.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.onBackground,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            "${freqLabel(frequency)} · 目标 $targetCount 次",
                            style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            SectionLabel("名称")
            InsetGroup { PlainField(value = name, onChange = { if (it.length <= 20) name = it }, placeholder = "例如：早起跑步") }
            SectionFooter("最多 20 个字，首页只显示一行。")

            SectionLabel("外观")
            InsetGroup {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .pressable {
                            navController.currentBackStackEntry?.savedStateHandle?.set("current_icon", icon)
                            navController.navigate("icon_picker/$icon")
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    HabitTile(iconRes = HabitIcons.getRes(icon), accent = accent, size = 40.dp, iconSize = 19.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("图标", style = Body17, color = MaterialTheme.colorScheme.onBackground)
                        Text(
                            SFIcons.label(HabitIcons.resKey(icon)),
                            style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        painter = painterResource(SFIcons.res("chevron.right")), contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(13.dp)
                    )
                }
                HairlineDivider(startIndent = 66.dp)
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HabitAccents.forEachIndexed { idx, a ->
                        val sel = idx == colorIdx
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(30.dp).clip(CircleShape)
                                .background(a.main)
                                .pressable(scaleTo = 0.88f) { colorIdx = idx }
                        ) {
                            if (sel) {
                                Icon(
                                    painter = painterResource(SFIcons.res("checkmark")),
                                    contentDescription = null, tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
            SectionFooter("图标与配色只影响外观，不影响数据。")

            SectionLabel("目标")
            InsetGroup {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("每次完成计为", style = Body17, color = MaterialTheme.colorScheme.onBackground)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StepperBtn("−", enabled = targetCount > 1, dark = dark) { if (targetCount > 1) targetCount-- }
                        Text(
                            "$targetCount 次", style = TabularNum, fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        StepperBtn("＋", enabled = targetCount < 10, dark = dark) { if (targetCount < 10) targetCount++ }
                    }
                }
            }
            SectionFooter("一天内需要打卡的次数，适合阅读页数、喝水杯数等。")

            SectionLabel("重复")
            InsetGroup {
                IOSSegmentedControl(
                    options = listOf("DAILY" to "每天", "WEEKDAYS" to "工作日", "WEEKLY" to "每周", "MONTHLY" to "每月"),
                    selected = frequency,
                    onSelect = {
                        frequency = it
                        if (it == "DAILY" || it == "WEEKDAYS") frequencyValue = ""
                        if (it == "WEEKLY" && frequencyValue.isBlank()) frequencyValue = "1 3 5"
                        if (it == "MONTHLY" && frequencyValue.isBlank()) frequencyValue = "1 15"
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
            if (frequency == "WEEKLY") {
                Spacer(Modifier.height(10.dp))
                InsetGroup {
                    WeekdayPicker(value = frequencyValue, onChange = { frequencyValue = it })
                }
                if (frequencyValue.split(" ").filter { it.isNotBlank() }.isEmpty()) {
                    SectionFooter("请至少选择一天。")
                }
            }
            if (frequency == "MONTHLY") {
                Spacer(Modifier.height(10.dp))
                InsetGroup {
                    MonthPresetPicker(value = frequencyValue, onChange = { frequencyValue = it })
                }
                SectionFooter("输入每月几号，用空格分隔，如 1 15。")
            }
            Spacer(Modifier.height(28.dp))
        }
    }
}

/** 无衬线输入框：iOS 设置式纯白底 */
@Composable
private fun PlainField(value: String, onChange: (String) -> Unit, placeholder: String) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = Body17.copy(color = MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(ThemeGreen),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) {
                    Text(placeholder, style = Body17, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
                inner()
            }
        }
    )
}

private fun freqLabel(f: String) = when (f) {
    "DAILY" -> "每天"
    "WEEKDAYS" -> "工作日"
    "WEEKLY" -> "每周"
    "MONTHLY" -> "每月"
    else -> "每天"
}

@Composable
private fun StepperBtn(text: String, enabled: Boolean, dark: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(28.dp).clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 0.07f else 0.04f))
            .border(
                0.5.dp,
                (if (dark) Color.White else Color.Black).copy(alpha = 0.10f),
                CircleShape
            )
            .pressable(scaleTo = 0.9f) { if (enabled) onClick() }
    ) {
        Text(
            text, fontSize = 16.sp, fontWeight = FontWeight.Medium,
            color = if (enabled) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
    }
}

/** 星期选择：黑白胶囊（monochrome selection） */
@Composable
private fun WeekdayPicker(value: String, onChange: (String) -> Unit) {
    val selected = remember(value) { value.split(" ").filter { it.isNotBlank() }.toSet() }
    val names = listOf("一", "二", "三", "四", "五", "六", "日")
    val ink = MaterialTheme.colorScheme.onBackground
    val paper = MaterialTheme.colorScheme.background
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        names.forEachIndexed { idx, n ->
            val day = (idx + 1).toString()
            val sel = selected.contains(day)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(38.dp).clip(CircleShape)
                    .background(if (sel) ink else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                    .pressable(scaleTo = 0.9f) {
                        val next = selected.toMutableSet()
                        if (sel) next.remove(day) else next.add(day)
                        onChange(next.sortedBy { it.toIntOrNull() ?: 0 }.joinToString(" "))
                    }
            ) {
                Text(n, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (sel) paper else ink)
            }
        }
    }
}

/** 每月预设：黑白胶囊 */
@Composable
private fun MonthPresetPicker(value: String, onChange: (String) -> Unit) {
    val ink = MaterialTheme.colorScheme.onBackground
    val paper = MaterialTheme.colorScheme.background
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("1", "1 15", "5 15 25").forEach { preset ->
            val sel = value == preset
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clip(RoundedCornerShape(11.dp))
                    .background(if (sel) ink else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                    .pressable(scaleTo = 0.95f) { onChange(preset) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    if (preset == "5 15 25") "5・15・25" else preset.replace(" ", "・"),
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (sel) paper else ink
                )
            }
        }
    }
}
