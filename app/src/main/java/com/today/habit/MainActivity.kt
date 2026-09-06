package com.today.habit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.today.habit.ui.theme.ConstantTrackTheme
import com.today.habit.ui.screen.HomeScreen
import com.today.habit.ui.screen.StatsScreen
import com.today.habit.ui.screen.ManageHabitsScreen
import com.today.habit.ui.component.GlassBottomNavigationBar
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.today.habit.data.AppDatabase
import com.today.habit.data.HabitRepository
import com.today.habit.data.SettingsManager
import com.today.habit.ui.viewmodel.HabitViewModel
import com.today.habit.ui.viewmodel.HabitViewModelFactory
import androidx.compose.runtime.remember

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = applicationContext
            val database = AppDatabase.getDatabase(context)
            val repository = HabitRepository(database.habitDao())
            val settingsManager = SettingsManager(context)
            val viewModel: HabitViewModel = viewModel(factory = HabitViewModelFactory(repository, settingsManager))
            val isDarkTheme by viewModel.isDarkTheme

            ConstantTrackTheme(darkTheme = isDarkTheme) {
                MainApp(viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: HabitViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 将 NavHost 内容录制为背景层，供液态玻璃底栏取样
        val backdrop = rememberLayerBackdrop {
            drawRect(backgroundColor)
            drawContent()
        }

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        ) {
            composable("home") { HomeScreen(navController, viewModel) }
            composable("stats") { StatsScreen(navController, viewModel) }
            composable("manage_habits") { ManageHabitsScreen(navController, viewModel) }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = currentRoute != "manage_habits",
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            GlassBottomNavigationBar(
                navController = navController,
                backdrop = backdrop,
                modifier = Modifier.navigationBarsPadding().padding(bottom = 8.dp)
            )
        }
    }
}
