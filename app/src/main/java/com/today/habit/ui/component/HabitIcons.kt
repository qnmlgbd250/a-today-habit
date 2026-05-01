package com.today.habit.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.today.habit.R

object HabitIcons {
    val DefaultIcon = "Sunny"

    val IconsMap = mapOf(
        "Sunny" to Icons.Outlined.WbSunny,
        "Book" to Icons.Outlined.AutoStories,
        "Fitness" to Icons.Outlined.SportsGymnastics,
        "Water" to Icons.Outlined.LocalDrink,
        "Coffee" to Icons.Outlined.Coffee,
        "Eat" to Icons.Outlined.Restaurant,
        "Run" to Icons.Outlined.DirectionsRun,
        "Walk" to Icons.Outlined.DirectionsWalk,
        "Bike" to Icons.Outlined.DirectionsBike,
        "Sleep" to Icons.Outlined.Bedtime,
        "Meditation" to Icons.Outlined.SelfImprovement,
        "Code" to Icons.Outlined.Code,
        "Work" to Icons.Outlined.BusinessCenter,
        "Music" to Icons.Outlined.MusicNote,
        "Art" to Icons.Outlined.Palette,
        "Language" to Icons.Outlined.Translate,
        "Money" to Icons.Outlined.Savings,
        "Movie" to Icons.Outlined.Movie,
        "Clean" to Icons.Outlined.CleaningServices
    )

    val DrawableIconsMap = mapOf(
        "Comb" to R.drawable.ic_habit_comb
    )

    fun getIcon(name: String): ImageVector {
        return IconsMap[name] ?: Icons.Outlined.WbSunny
    }

    fun getDrawableRes(name: String): Int? {
        return DrawableIconsMap[name]
    }
}
