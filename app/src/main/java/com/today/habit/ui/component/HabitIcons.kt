package com.today.habit.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object HabitIcons {
    val DefaultIcon = "Sunny"
    
    val IconsMap = mapOf(
        "Sunny" to Icons.Default.WbSunny,
        "Book" to Icons.Default.Book,
        "Fitness" to Icons.Default.FitnessCenter,
        "Water" to Icons.Default.LocalDrink,
        "Coffee" to Icons.Default.Coffee,
        "Eat" to Icons.Default.Restaurant,
        "Run" to Icons.Default.DirectionsRun,
        "Walk" to Icons.Default.DirectionsWalk,
        "Bike" to Icons.Default.DirectionsBike,
        "Sleep" to Icons.Default.Bedtime,
        "Meditation" to Icons.Default.SelfImprovement,
        "Code" to Icons.Default.Code,
        "Work" to Icons.Default.Work,
        "Music" to Icons.Default.MusicNote,
        "Art" to Icons.Default.Palette,
        "Language" to Icons.Default.Translate,
        "Money" to Icons.Default.Savings,
        "Star" to Icons.Default.Star,
        "Heart" to Icons.Default.Favorite,
        "Home" to Icons.Default.Home,
        "Clean" to Icons.Default.CleaningServices,
        "Pet" to Icons.Default.Pets
    )

    fun getIcon(name: String): ImageVector {
        return IconsMap[name] ?: Icons.Default.WbSunny
    }
}
