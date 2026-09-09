package com.today.habit.ui.component

import androidx.compose.ui.graphics.Color
import com.today.habit.data.entity.Habit
import com.today.habit.ui.theme.IOSBlue
import com.today.habit.ui.theme.IOSOrange
import com.today.habit.ui.theme.IOSPink
import com.today.habit.ui.theme.IOSPurple
import com.today.habit.ui.theme.IOSRed
import com.today.habit.ui.theme.IOSTeal
import com.today.habit.ui.theme.ThemeGreen
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 习惯配色：iOS 设置式纯平色板。
 * 高级感铁律：图标一律纯色平涂 + 白色字形，禁止渐变、禁止彩色阴影。
 */
data class HabitAccent(
    val main: Color,
    val soft: Color
)

val HabitAccents = listOf(
    HabitAccent(Color(0xFFFF9500), Color(0xFFFF9500).copy(alpha = 0.13f)),
    HabitAccent(IOSPink, IOSPink.copy(alpha = 0.11f)),
    HabitAccent(Color(0xFFAF52DE), Color(0xFFAF52DE).copy(alpha = 0.12f)),
    HabitAccent(Color(0xFF5856D6), Color(0xFF5856D6).copy(alpha = 0.12f)),
    HabitAccent(Color(0xFF007AFF), Color(0xFF007AFF).copy(alpha = 0.11f)),
    HabitAccent(Color(0xFF5AC8FA), Color(0xFF5AC8FA).copy(alpha = 0.15f)),
    HabitAccent(ThemeGreen, ThemeGreen.copy(alpha = 0.12f)),
    HabitAccent(Color(0xFFFF3B30), Color(0xFFFF3B30).copy(alpha = 0.11f))
)

/** 取习惯配色：优先用户选择，否则按 id 散列，保证稳定 */
fun Habit.accent(): HabitAccent {
    val idx = if (color in HabitAccents.indices) color
    else ((id % HabitAccents.size).toInt().let { if (it < 0) it + HabitAccents.size else it })
    return HabitAccents[idx]
}

/** 频率中文标签 */
fun Habit.frequencyLabel(): String = when (frequency) {
    "DAILY" -> "每天"
    "WEEKDAYS" -> "工作日"
    "WEEKLY" -> "每周"
    "MONTHLY" -> "每月"
    else -> "每天"
}

/** 问候语（按小时） */
fun greetingOf(hour: Int): String = when (hour) {
    in 5..10 -> "早上好"
    in 11..13 -> "中午好"
    in 14..17 -> "下午好"
    in 18..22 -> "晚上好"
    else -> "夜深了"
}

/** 日期标题：9月9日 星期二 */
fun LocalDate.titleStr(): String {
    val week = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)
    return "${monthValue}月${dayOfMonth}日 $week"
}

/** 打卡鼓励语（克制版） */
fun cheerOf(done: Int, total: Int): String = when {
    total == 0 -> "创建第一个习惯，开始记录"
    done == 0 -> "新的一天，从第一项开始"
    done < total -> "已完成 $done / $total"
    else -> "今日全部完成"
}
