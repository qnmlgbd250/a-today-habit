package com.today.habit.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.today.habit.ui.component.CheckRingButton
import com.today.habit.ui.component.GlassCard
import com.today.habit.ui.component.GlassIconButton
import com.today.habit.ui.component.HabitTile
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.CheckInSoundPlayer
import com.today.habit.ui.component.IOSMenuCard
import com.today.habit.ui.component.IOSMenuDivider
import com.today.habit.ui.component.IOSMenuItem
import com.today.habit.ui.component.IOSToast
import com.today.habit.ui.component.LargeTitleHeader
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.component.accent
import com.today.habit.ui.component.cheerOf
import com.today.habit.ui.component.frequencyLabel
import com.today.habit.ui.component.greetingOf
import com.today.habit.ui.component.titleStr
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
 * 首页 2.0（旗舰重构）：
 * 极光背景 + 大标题 + 今日 hero + 本周条 + 液态玻璃习惯卡。
 * 交互：点圆环打卡一次，点卡片进入编辑，长按无（保持极简）。
 */
@Composable
fun HomeScreen(navController: NavController, viewModel: HabitViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var showMenu by remember { mutableStateOf(false) }

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
    val isDark by viewModel.isDarkTheme

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) { delay(1800); toastMessage = null }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuroraBackground(dark = isDark)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(Modifier.statusBarsPadding().height(12.dp))
                LargeTitleHeader(
                    title = greetingOf(java.time.LocalTime.now().hour),
                    subtitle = selectedDate.titleStr()
                ) {
                    GlassIconButton("plus") { navController.navigate("habit_edit/new") }
                    GlassIconButton(if (isDark) "sun.max" else "moon") { viewModel.toggleTheme() }
                    GlassIconButton("gearshape") { showMenu = !showMenu }
                }
            }

            // 今日 Hero：进度环 + 问候 + 本周迷你条入口
            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.today.habit.ui.component.HeroRing(progress = progress)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (selectedDate == LocalDate.now()) "今日进度" else "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "$doneCount / $totalCount 已完成",
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                cheerOf(doneCount, totalCount),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    WeekStrip(selectedDate) { viewModel.setSelectedDate(it) }
                }
            }

            // 习惯卡片
            if (filteredHabits.isEmpty()) {
                item {
                    GlassCard {
                        Column(
                            Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(64.dp).clip(CircleShape)
                                    .background(ThemeGreen.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    painter = painterResource(SFIcons.res("sparkles")),
                                    contentDescription = null, tint = ThemeGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("还没有安排习惯", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "点击右上角 ＋ 创建第一个好习惯",
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(14.dp))
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Brush.linearGradient(listOf(Color(0xFF5BE584), ThemeGreen)))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { navController.navigate("habit_edit/new") }
                                    .padding(horizontal = 28.dp, vertical = 12.dp)
                            ) {
                                Text("新建习惯", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            } else {
                items(filteredHabits, key = { it.id }) { habit ->
                    val checkIn = checkIns.find { it.habitId == habit.id }
                    FlagshipHabitCard(
                        habit = habit,
                        checkIn = checkIn,
                        onToggle = { viewModel.toggleCheckIn(habit.id, selectedDate.toString(), habit.targetCount) },
                        onEdit = { navController.navigate("habit_edit/${habit.id}") }
                    )
                }
            }
        }

        // 右上玻璃菜单
        if (showMenu) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.08f))
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

/** 本周横条：周一～周日，选中态为品牌绿胶囊 */
@Composable
fun WeekStrip(selected: LocalDate, onSelect: (LocalDate) -> Unit) {
    val weekStart = remember(selected) {
        selected.minusDays((selected.dayOfWeek.value - 1).toLong())
    }
    val days = remember(weekStart) { (0..6).map { weekStart.plusDays(it.toLong()) } }
    val today = LocalDate.now()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        days.forEach { date ->
            val isSel = date == selected
            val isToday = date == today
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSel) ThemeGreen else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(date) }
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE),
                    fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${date.dayOfMonth}",
                    fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onBackground
                )
                Box(
                    Modifier.padding(top = 3.dp).size(4.dp).clip(CircleShape)
                        .background(
                            when {
                                isSel -> Color.White
                                isToday -> ThemeGreen
                                else -> Color.Transparent
                            }
                        )
                )
            }
        }
    }
}

/** 旗舰习惯卡：图标瓷砖 + 名称/副标题 + 圆环打卡键 */
@Composable
fun FlagshipHabitCard(habit: Habit, checkIn: CheckInRecord?, onToggle: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    val accent = remember(habit.id, habit.color) { habit.accent() }
    val count = checkIn?.count ?: 0
    val done = count >= habit.targetCount
    val surface = MaterialTheme.colorScheme.surface
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(24.dp))
            .background(surface.copy(alpha = if (isDark) 0.74f else 0.82f))
            .border(1.dp, Color.White.copy(alpha = if (isDark) 0.16f else 0.7f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onEdit() }
            .padding(14.dp)
    ) {
        HabitTile(iconRes = HabitIcons.getRes(habit.icon), accent = accent)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                habit.name, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.clip(RoundedCornerShape(7.dp))
                        .background(accent.soft)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${habit.frequencyLabel()} · 目标 $count/${habit.targetCount}",
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = accent.main
                    )
                }
            }
            // 细进度条
            if (habit.targetCount > 1) {
                Spacer(Modifier.height(7.dp))
                val frac = (count.toFloat() / habit.targetCount.toFloat()).coerceIn(0f, 1f)
                Box(
                    Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Box(
                        Modifier.fillMaxWidth(frac).height(4.dp).clip(RoundedCornerShape(2.dp))
                            .background(Brush.horizontalGradient(listOf(accent.main, accent.gradientEnd)))
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
