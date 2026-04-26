package com.today.habit.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIcons {
    val DefaultIcon = "Sunny"

    val IconsMap = mapOf(
        "Sunny" to Icons.Outlined.WbSunny,
        "Book" to Icons.Outlined.Book,
        "Fitness" to Icons.Outlined.FitnessCenter,
        "Water" to Icons.Outlined.LocalDrink,
        "Coffee" to Icons.Outlined.Coffee,
        "Eat" to Icons.Outlined.Restaurant,
        "Run" to Icons.Outlined.DirectionsRun,
        "Walk" to Icons.Outlined.DirectionsWalk,
        "Bike" to Icons.Outlined.DirectionsBike,
        "Sleep" to Icons.Outlined.Bedtime,
        "Meditation" to Icons.Outlined.SelfImprovement,
        "Code" to Icons.Outlined.Code,
        "Work" to Icons.Outlined.Work,
        "Music" to Icons.Outlined.MusicNote,
        "Art" to Icons.Outlined.Palette,
        "Language" to Icons.Outlined.Translate,
        "Money" to Icons.Outlined.Savings,
        "Star" to Icons.Outlined.Star,
        "Heart" to Icons.Outlined.Favorite,
        "Home" to Icons.Outlined.Home,
        "Clean" to Icons.Outlined.CleaningServices,
        "Pet" to Icons.Outlined.Pets
    )

    fun getIcon(name: String): ImageVector {
        return IconsMap[name] ?: Icons.Outlined.WbSunny
    }
}
