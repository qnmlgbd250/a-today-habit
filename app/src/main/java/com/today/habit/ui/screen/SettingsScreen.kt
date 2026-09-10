package com.today.habit.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.BuildConfig
import com.today.habit.ui.component.IOSChevron
import com.today.habit.ui.component.IOSDivider
import com.today.habit.ui.component.IOSGroup
import com.today.habit.ui.component.IOSRow
import com.today.habit.ui.component.IOSSettingsIcon
import com.today.habit.ui.component.IOSSwitch
import com.today.habit.ui.component.IOSToast
import com.today.habit.ui.component.iosElevatedCard
import com.today.habit.ui.component.iosEntrance
import com.today.habit.ui.component.IOSTopScrim
import com.today.habit.ui.component.rememberIOSCollapsed
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.viewmodel.HabitViewModel
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 设置页（iOS 设置风分组列表）：品牌头 / 外观 / 习惯 / 数据 / 落款。
 * 备份恢复逻辑由首页迁移至此。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: HabitViewModel) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var toastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(1800)
            toastMessage = null
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = viewModel.exportDataJson()
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        OutputStreamWriter(outputStream).use { writer ->
                            writer.write(json)
                        }
                    }
                    toastMessage = "备份成功"
                } catch (e: Exception) {
                    toastMessage = "备份失败"
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        InputStreamReader(inputStream).use { reader ->
                            val json = reader.readText()
                            viewModel.importDataJson(json) { success, message ->
                                toastMessage = if (success) {
                                    val current = viewModel.selectedDate.value
                                    viewModel.setSelectedDate(current)
                                    "恢复成功"
                                } else {
                                    "恢复失败：$message"
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    toastMessage = "文件读取失败"
                }
            }
        }
    }

    val isDark by viewModel.isDarkTheme
    val listState = rememberLazyListState()
    val collapsed = rememberIOSCollapsed(listState)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = IOSColors.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 品牌头
                    item {
                        Box(modifier = Modifier.iosEntrance("settings:brand", 0)) {
                            SettingsBrandHeader()
                        }
                    }
                    // 外观
                    item {
                        Box(modifier = Modifier.iosEntrance("settings:look", 1)) {
                            IOSGroup(header = "外观") {
                                IOSRow(
                                    leading = { IOSSettingsIcon("moon", IOSColors.purple) },
                                    trailing = {
                                        IOSSwitch(checked = isDark, onCheckedChange = { viewModel.toggleTheme() })
                                    }
                                ) {
                                    Text("深色模式", style = IOSType.body, color = IOSColors.label)
                                }
                            }
                        }
                    }
                    // 习惯
                    item {
                        Box(modifier = Modifier.iosEntrance("settings:habits", 2)) {
                            IOSGroup(header = "习惯") {
                                IOSRow(
                                    onClick = { navController.navigate("habit_edit/new") },
                                    leading = { IOSSettingsIcon("plus", IOSColors.green) },
                                    trailing = { IOSChevron() }
                                ) {
                                    Text("新建习惯", style = IOSType.body, color = IOSColors.label)
                                }
                                IOSDivider()
                                IOSRow(
                                    onClick = { navController.navigate("manage_habits") },
                                    leading = { IOSSettingsIcon("gearshape", IOSColors.gray) },
                                    trailing = { IOSChevron() }
                                ) {
                                    Text("管理习惯", style = IOSType.body, color = IOSColors.label)
                                }
                            }
                        }
                    }
                    // 数据
                    item {
                        Box(modifier = Modifier.iosEntrance("settings:data", 3)) {
                            IOSGroup(
                                header = "数据",
                                footer = "备份文件为 JSON 格式，可在重装后恢复全部习惯与打卡记录。"
                            ) {
                                IOSRow(
                                    onClick = { exportLauncher.launch("habit_backup_${LocalDate.now()}.json") },
                                    leading = { IOSSettingsIcon("square.and.arrow.up", IOSColors.blue) },
                                    trailing = { IOSChevron() }
                                ) {
                                    Text("备份数据", style = IOSType.body, color = IOSColors.label)
                                }
                                IOSDivider()
                                IOSRow(
                                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                                    leading = { IOSSettingsIcon("clock.arrow.circlepath", IOSColors.orange) },
                                    trailing = { IOSChevron() }
                                ) {
                                    Text("恢复数据", style = IOSType.body, color = IOSColors.label)
                                }
                            }
                        }
                    }
                    // 落款
                    item {
                        Box(modifier = Modifier.iosEntrance("settings:sign", 4)) {
                            Text(
                                "小日常 v${BuildConfig.VERSION_NAME} · 每天进步一点点",
                                style = IOSType.footnote,
                                color = IOSColors.tertiaryLabel,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
                IOSTopScrim(collapsed)
            }
        }
        IOSToast(toastMessage)
    }
}

/** 品牌头：渐变徽标 + 应用名 +  slogan */
@Composable
private fun SettingsBrandHeader() {
    val green = IOSColors.green
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .iosElevatedCard(18.dp)
            .padding(18.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(lerp(green, Color.White, 0.15f), green, lerp(green, Color.Black, 0.15f))
                    )
                )
        ) {
            Icon(
                painter = painterResource(com.today.habit.ui.component.SFIcons.res("checkmark")),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("小日常", style = IOSType.title2, color = IOSColors.label)
            Text("每天进步一点点", style = IOSType.subhead, color = IOSColors.secondaryLabel)
        }
    }
}
