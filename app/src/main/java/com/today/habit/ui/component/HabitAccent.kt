package com.today.habit.ui.component

import androidx.compose.ui.graphics.Color
import com.today.habit.data.entity.Habit
import com.today.habit.ui.theme.IOSBlue
import com.today.habit.ui.theme.IOSOrange
import com.today.habit.ui.theme.IOSPink
import com.today.habit.ui.theme.IOSPurple
import com.today.habit.ui.theme.IOSRed
import com.today.habit.ui.theme.IOSTeal
import com.today.habit.ui.theme.IOSYellow
import com.today.habit.ui.theme.ThemeGreen
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** 习惯配色：8 色 iOS 系统级色板，商业 App 标配的个性化外观 */
data class HabitAccent(
    val main: Color,
    val soft: Color,
    val gradientEnd: Color
)

val HabitAccents = listOf(
    HabitAccent(Color(0xFFFF9F0A), Color(0xFFFF9F0A).copy(alpha = 0.14f), Color(0xFFFFD60A)),
    HabitAccent(Color(0xFFFF375F), Color(0xFFFF375F).copy(alpha = 0.12f), Color(0xFFFF6482)),
    HabitAccent(Color(0xFFBF5AF2), Color(0xFFBF5AF2).copy(alpha = 0.13f), Color(0xFF7D7AFF)),
    HabitAccent(IOSBlue, IOSBlue.copy(alpha = 0.12f), Color(0xFF64D2FF)),
    HabitAccent(IOSTeal, IOSTeal.copy(alpha = 0.14f), Color(0xFF30D158)),
    HabitAccent(ThemeGreen, ThemeGreen.copy(alpha = 0.13f), Color(0xFF8FE388)),
    HabitAccent(Color(0xFFFFD60A), Color(0xFFFFD60A).copy(alpha = 0.16f), Color(0xFFFF9F0A)),
    HabitAccent(IOSRed, IOSRed.copy(alpha = 0.12f), Color(0xFFFF9F0A))
)

/** 取习惯配色：优先使用用户选择的 color 下标，否则按 id 散列，保证稳定 */
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

/** 打卡鼓励语 */
fun cheerOf(done: Int, total: Int): String = when {
    total == 0 -> "去创建你的第一个好习惯吧"
    done == 0 -> "新的一天，从第一步开始"
    done < total -> "已完成 $done/$total，继续加油"
    else -> "全部完成，太棒了"
}
