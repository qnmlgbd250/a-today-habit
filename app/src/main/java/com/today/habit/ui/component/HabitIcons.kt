package com.today.habit.ui.component

import androidx.compose.ui.graphics.Color
import com.today.habit.R

object HabitIcons {
    val DefaultIcon = "Sunny"

    // 历史版本存储的图标 key -> SF Symbols 名称
    private val LegacyMap = mapOf(
        "Sunny" to "sun.max",
        "Book" to "book",
        "Fitness" to "dumbbell",
        "Water" to "drop",
        "Coffee" to "cup.and.saucer",
        "Eat" to "fork.knife",
        "Run" to "figure.run",
        "Walk" to "figure.walk",
        "Bike" to "bicycle",
        "Sleep" to "moon.zzz",
        "Meditation" to "brain.head.profile",
        "Code" to "chevron.left.forwardslash.chevron.right",
        "Work" to "briefcase",
        "Music" to "music.note",
        "Art" to "paintpalette",
        "Language" to "globe",
        "Money" to "banknote",
        "Movie" to "film",
        "Clean" to "sparkles"
    )

    // 更早期的自定义 drawable 图标
    private val LegacyDrawables = mapOf(
        "Comb" to R.drawable.ic_habit_comb
    )

    /** 取图标 drawable 资源：新数据直接存 SF Symbols 名称，旧 key 自动映射 */
    fun getRes(name: String): Int {
        LegacyDrawables[name]?.let { return it }
        return SFIcons.res(LegacyMap[name] ?: name)
    }

    /** 取图标的注册表 key（旧 key 映射为 SF 名称，用于展示与选择器回显） */
    fun resKey(name: String): String {
        if (LegacyDrawables.containsKey(name)) return name
        return LegacyMap[name] ?: name
    }

    /**
     * iOS 提醒事项风代表色：每个习惯按 id 取一个固定颜色，
     * 用于图标角标等点缀（完成态仍统一用主题绿，避免语义混乱）。
     */
    private val Palette = listOf(
        Color(0xFF007AFF),
        Color(0xFF34C759),
        Color(0xFFFF9500),
        Color(0xFFFF3B30),
        Color(0xFFAF52DE),
        Color(0xFF5AC8FA),
        Color(0xFFFF2D55)
    )

    fun colorFor(id: Long): Color =
        Palette[Math.floorMod(id, Palette.size.toLong()).toInt()]
}
