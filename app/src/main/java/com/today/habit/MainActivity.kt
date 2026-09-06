package com.today.habit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
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
import com.today.habit.ui.screen.IconPickerScreen
import com.today.habit.ui.screen.HabitEditScreen
import com.today.habit.ui.screen.DeleteHabitScreen
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
    // 全屏页面（无底栏）：管理习惯、图标选择、习惯表单、删除确认
    val isFullScreenPage = currentRoute != null && (
        currentRoute == "manage_habits" ||
            currentRoute.startsWith("icon_picker") ||
            currentRoute.startsWith("habit_edit") ||
            currentRoute.startsWith("habit_delete")
        )

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

        // 全局 iOS 风格侧滑转场：新页面从右推入，返回时滑出，底层页面做小幅反向位移
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
            enterTransition = { slideInHorizontally(tween(350)) { it } },
            exitTransition = { slideOutHorizontally(tween(350)) { -it / 4 } },
            popEnterTransition = { slideInHorizontally(tween(350)) { -it / 4 } },
            popExitTransition = { slideOutHorizontally(tween(350)) { it } }
        ) {
            composable("home") { HomeScreen(navController, viewModel, backdrop) }
            composable("stats") { StatsScreen(navController, viewModel) }
            composable("manage_habits") { ManageHabitsScreen(navController, viewModel) }
            composable("icon_picker/{selected}") { entry ->
                IconPickerScreen(
                    navController = navController,
                    selectedKey = entry.arguments?.getString("selected") ?: ""
                )
            }
            composable("habit_edit/{habitId}") { entry ->
                HabitEditScreen(
                    navController = navController,
                    viewModel = viewModel,
                    habitId = entry.arguments?.getString("habitId") ?: "new"
                )
            }
            composable("habit_delete/{habitId}") { entry ->
                DeleteHabitScreen(
                    navController = navController,
                    viewModel = viewModel,
                    habitId = entry.arguments?.getString("habitId") ?: ""
                )
            }
        }

        androidx.compose.animation.AnimatedVisibility(
            visible = !isFullScreenPage,
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
