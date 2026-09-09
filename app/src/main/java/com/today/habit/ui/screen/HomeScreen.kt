package com.today.habit.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import com.today.habit.ui.component.AuroraBackground
import com.today.habit.ui.component.CheckInSoundPlayer
import com.today.habit.ui.component.CheckRingButton
import com.today.habit.ui.component.GlassIconButton
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.HairlineDivider
import com.today.habit.ui.component.HeroRing
import com.today.habit.ui.component.IOSMenuCard
import com.today.habit.ui.component.IOSMenuDivider
import com.today.habit.ui.component.IOSMenuItem
import com.today.habit.ui.component.IOSToast
import com.today.habit.ui.component.InsetGroup
import com.today.habit.ui.component.LargeTitleHeader
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.SectionLabel
import com.today.habit.ui.component.accent
import com.today.habit.ui.component.cheerOf
import com.today.habit.ui.component.frequencyLabel
import com.today.habit.ui.component.greetingOf
import com.today.habit.ui.component.isDark
import com.today.habit.ui.component.pressable
import com.today.habit.ui.component.staggerIn
import com.today.habit.ui.component.titleStr
import com.today.habit.ui.theme.Body17
import com.today.habit.ui.theme.Footnote13
import com.today.habit.ui.theme.Headline17
import com.today.habit.ui.theme.ThemeGreen
import com.today.habit.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 首页 3.0（高级感重构）：
 * 大标题 + 今日摘要 + 本周条 + iOS 内嵌分组习惯表。
 * 高级感来源：纯平图标、发丝分隔、黑白选中、按压缩放、stagger 入场。
 */
@Composable
fun HomeScreen(navController: NavController, viewModel: HabitViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    val dark = isDark()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshDateIfNecessary()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.exportDataJson()
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        OutputStreamWriter(out).use { w -> w.write(json) }
                    }
                    toastMessage = "备份成功"
                } catch (e: Exception) { toastMessage = "备份失败: ${e.message}" }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(it)?.use { ins ->
                        InputStreamReader(ins).use { r ->
                            viewModel.importDataJson(r.readText()) { success, message ->
                                toastMessage = if (success) "恢复成功" else "恢复失败: $message"
                                if (success) viewModel.setSelectedDate(viewModel.selectedDate.value)
                            }
                        }
                    }
                } catch (e: Exception) { toastMessage = "文件读取失败: ${e.message}" }
            }
        }
    }

    val allHabits by viewModel.allHabits.observeAsState(emptyList())
    val selectedDate by viewModel.selectedDate
    val checkIns by viewModel.getCheckInsByDate(selectedDate.toString()).observeAsState(emptyList())
    val filteredHabits = remember(allHabits, selectedDate) { viewModel.getFilteredHabits(allHabits, selectedDate) }
    val doneCount = remember(filteredHabits, checkIns) {
        filteredHabits.count { h ->
            val c = checkIns.find { it.habitId == h.id }?.count ?: 0
            c >= h.targetCount
        }
    }
    val totalCount = filteredHabits.size
    val progress = if (totalCount == 0) 0f else doneCount.toFloat() / totalCount.toFloat()
    val isDarkTheme by viewModel.isDarkTheme

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) { delay(1800); toastMessage = null }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuroraBackground(dark = dark)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                Spacer(Modifier.statusBarsPadding().height(10.dp))
                LargeTitleHeader(
                    title = greetingOf(java.time.LocalTime.now().hour),
                    subtitle = selectedDate.titleStr()
                ) {
                    GlassIconButton("plus") { navController.navigate("habit_edit/new") }
                    GlassIconButton(if (isDarkTheme) "sun.max" else "moon") { viewModel.toggleTheme() }
                    GlassIconButton("gearshape") { showMenu = !showMenu }
                }
                Spacer(Modifier.height(14.dp))
            }

            // 今日摘要
            item {
                InsetGroup {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        HeroRing(progress = progress, size = 58.dp, stroke = 6.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (selectedDate == LocalDate.now()) "今日进度" else "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                                style = Footnote13,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(1.dp))
                            Text(
                                "$doneCount / $totalCount",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.3).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                cheerOf(doneCount, totalCount),
                                style = Footnote13,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item { SectionLabel("本周") }
            item {
                InsetGroup {
                    WeekStrip(selectedDate) { viewModel.setSelectedDate(it) }
                }
            }

            item { SectionLabel(if (totalCount == 0) "习惯" else "习惯 · 已完成 $doneCount / $totalCount") }
            if (filteredHabits.isEmpty()) {
                item {
                    InsetGroup {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(68.dp).clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                            ) {
                                Icon(
                                    painter = painterResource(SFIcons.res("sparkles")),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("还没有习惯", style = Headline17, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(4.dp))
                            Text("点右上角 ＋ 创建第一个", style = Footnote13, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "新建习惯",
                                fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = ThemeGreen,
                                modifier = Modifier.pressable { navController.navigate("habit_edit/new") }.padding(8.dp)
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(filteredHabits, key = { _, h -> h.id }) { index, habit ->
                    val checkIn = checkIns.find { it.habitId == habit.id }
                    InsetGroup(Modifier.staggerIn(index)) {
                        HabitRow(
                            habit = habit,
                            checkIn = checkIn,
                            onToggle = { viewModel.toggleCheckIn(habit.id, selectedDate.toString(), habit.targetCount) },
                            onEdit = { navController.navigate("habit_edit/${habit.id}") }
                        )
                    }
                    if (index < filteredHabits.size - 1) Spacer(Modifier.height(10.dp))
                }
            }
        }

        if (showMenu) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.06f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showMenu = false }
            )
            IOSMenuCard(
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(top = 108.dp, end = 20.dp)
            ) {
                IOSMenuItem("pencil", "管理习惯") { showMenu = false; navController.navigate("manage_habits") }
                IOSMenuDivider()
                IOSMenuItem("square.and.arrow.up", "备份数据") {
                    showMenu = false
                    exportLauncher.launch("habit_backup_${LocalDate.now()}.json")
                }
                IOSMenuDivider()
                IOSMenuItem("clock.arrow.circlepath", "恢复数据") {
                    showMenu = false
                    importLauncher.launch(arrayOf("application/json"))
                }
            }
        }

        IOSToast(toastMessage)
    }
}

/** 本周横条：黑白胶囊选中（彩色只属于进度） */
@Composable
fun WeekStrip(selected: LocalDate, onSelect: (LocalDate) -> Unit) {
    val weekStart = remember(selected) { selected.minusDays((selected.dayOfWeek.value - 1).toLong()) }
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val today = LocalDate.now()
    val ink = MaterialTheme.colorScheme.onBackground
    val paper = MaterialTheme.colorScheme.background
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { date ->
            val isSel = date == selected
            val isToday = date == today
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (isSel) ink else Color.Transparent)
                    .pressable(scaleTo = 0.93f) { onSelect(date) }
                    .padding(horizontal = 9.dp, vertical = 7.dp)
            ) {
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
                    fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = if (isSel) paper else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(1.dp))
                Text(
                    "${date.dayOfMonth}",
                    fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (isSel) paper else MaterialTheme.colorScheme.onBackground
                )
                Box(
                    Modifier.padding(top = 3.dp).size(4.dp).clip(CircleShape)
                        .background(
                            when {
                                isSel -> Color.Transparent
                                isToday -> ThemeGreen
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

/** 习惯行：纯平瓷砖 + 标题/脚注 + 发丝圆环键 */
@Composable
fun HabitRow(habit: Habit, checkIn: CheckInRecord?, onToggle: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    val accent = remember(habit.id, habit.color) { habit.accent() }
    val count = checkIn?.count ?: 0
    val done = count >= habit.targetCount

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .pressable(onClick = onEdit)
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        HabitTile(iconRes = HabitIcons.getRes(habit.icon), accent = accent)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                habit.name, style = Body17, fontWeight = FontWeight.SemiBold,
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Spacer(Modifier.height(1.dp))
            Text(
                "${habit.frequencyLabel()} · ${if (done) "已完成" else "目标 $count/${habit.targetCount}"}",
                style = Footnote13,
                color = if (done) ThemeGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (habit.targetCount > 1 && !done) {
                Spacer(Modifier.height(6.dp))
                val frac = (count.toFloat() / habit.targetCount.toFloat()).coerceIn(0f, 1f)
                Box(
                    Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(1.5.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(frac).height(3.dp)
                            .background(accent.main)
                    )
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        CheckRingButton(count = count, target = habit.targetCount, accent = accent) {
            CheckInSoundPlayer.playCompletionSound(context)
            onToggle()
        }
    }
}
