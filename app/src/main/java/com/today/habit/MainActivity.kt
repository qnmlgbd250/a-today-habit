package com.today.habit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.today.habit.ui.theme.ConstantTrackTheme
import com.today.habit.ui.screen.HomeScreen
import com.today.habit.ui.screen.StatsScreen
import com.today.habit.ui.screen.ManageHabitsScreen
import com.today.habit.ui.component.BottomNavigationBar
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { 
            if (currentRoute != "manage_habits") {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController, viewModel) }
            composable("stats") { StatsScreen(viewModel) }
            composable("manage_habits") { ManageHabitsScreen(navController, viewModel) }
        }
    }
}
