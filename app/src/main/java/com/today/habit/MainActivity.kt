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

        // iOS 风格转场：标签页互切淡入淡出，推入页从右滑入、返回时滑出，
        // 底层页面随推入/返回做小幅位移动画
        val tween220 = tween<Float>(220)
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
            enterTransition = { fadeIn(tween220) },
            exitTransition = { fadeOut(tween220) },
            popEnterTransition = { fadeIn(tween220) },
            popExitTransition = { fadeOut(tween220) }
        ) {
            composable(
                "home",
                exitTransition = {
                    if (targetState.destination.route == "stats") fadeOut(tween220)
                    else slideOutHorizontally(tween(320)) { -it / 4 } + fadeOut(tween(320))
                },
                popEnterTransition = {
                    if (initialState.destination.route == "stats") fadeIn(tween220)
                    else slideInHorizontally(tween(320)) { -it / 4 } + fadeIn(tween(320))
                }
            ) { HomeScreen(navController, viewModel) }
            composable(
                "stats",
                exitTransition = {
                    if (targetState.destination.route == "home") fadeOut(tween220)
                    else slideOutHorizontally(tween(320)) { -it / 4 } + fadeOut(tween(320))
                },
                popEnterTransition = {
                    if (initialState.destination.route == "home") fadeIn(tween220)
                    else slideInHorizontally(tween(320)) { -it / 4 } + fadeIn(tween(320))
                }
            ) { StatsScreen(navController, viewModel) }
            composable(
                "manage_habits",
                enterTransition = { slideInHorizontally(tween(350)) { it } },
                exitTransition = { slideOutHorizontally(tween(350)) { -it / 4 } },
                popEnterTransition = { slideInHorizontally(tween(350)) { -it / 4 } },
                popExitTransition = { slideOutHorizontally(tween(350)) { it } }
            ) { ManageHabitsScreen(navController, viewModel) }
            composable(
                "icon_picker/{selected}",
                enterTransition = { slideInHorizontally(tween(350)) { it } },
                exitTransition = { slideOutHorizontally(tween(350)) { -it / 4 } },
                popEnterTransition = { slideInHorizontally(tween(350)) { -it / 4 } },
                popExitTransition = { slideOutHorizontally(tween(350)) { it } }
            ) { entry ->
                IconPickerScreen(
                    navController = navController,
                    selectedKey = entry.arguments?.getString("selected") ?: ""
                )
            }
            composable(
                "habit_edit/{habitId}",
                enterTransition = { slideInHorizontally(tween(350)) { it } },
                exitTransition = { slideOutHorizontally(tween(350)) { -it / 4 } },
                popEnterTransition = { slideInHorizontally(tween(350)) { -it / 4 } },
                popExitTransition = { slideOutHorizontally(tween(350)) { it } }
            ) { entry ->
                HabitEditScreen(
                    navController = navController,
                    viewModel = viewModel,
                    habitId = entry.arguments?.getString("habitId") ?: "new"
                )
            }
            composable(
                "habit_delete/{habitId}",
                enterTransition = { slideInHorizontally(tween(350)) { it } },
                exitTransition = { slideOutHorizontally(tween(350)) { -it / 4 } },
                popEnterTransition = { slideInHorizontally(tween(350)) { -it / 4 } },
                popExitTransition = { slideOutHorizontally(tween(350)) { it } }
            ) { entry ->
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
