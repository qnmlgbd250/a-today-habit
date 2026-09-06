package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.theme.ThemeGreen

/**
 * 图标库选择页：网格 + 分类 + 搜索，替代弹窗交互。
 * 进入时通过路由参数传入当前图标 key，选择后通过 savedStateHandle 回传 "picked_icon"。
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun IconPickerScreen(navController: NavController, selectedKey: String) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("全部") }
    val gridState = rememberLazyGridState()

    val filtered = remember(query, category) {
        val base = if (category == "全部") {
            SFIcons.Categories.flatMap { it.second }
        } else {
            SFIcons.Categories.find { it.first == category }?.second ?: emptyList()
        }.distinct()
        if (query.isBlank()) base else base.filter { key ->
            key.contains(query.trim(), ignoreCase = true) ||
                SFIcons.label(key).contains(query.trim(), ignoreCase = true)
        }
    }

    LaunchedEffect(query, category) { gridState.scrollToItem(0) }

    fun pick(key: String) {
        navController.previousBackStackEntry?.savedStateHandle?.set("picked_icon", key)
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("图标", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                painter = painterResource(SFIcons.res("chevron.left")),
                                contentDescription = "返回",
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜索图标") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(SFIcons.res("magnifyingglass")),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ThemeGreen,
                            focusedLabelColor = ThemeGreen
                        )
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf("全部") + SFIcons.Categories.map { it.first }) { label ->
                            FilterChip(
                                selected = category == label,
                                onClick = { category = label },
                                label = { Text(label) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ThemeGreen,
                                    selectedLabelColor = androidx.compose.ui.graphics.Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it }) { key ->
                val isSelected = key == selectedKey ||
                    SFIcons.res(key) == SFIcons.res(selectedKey)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { pick(key) }
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) ThemeGreen.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ThemeGreen
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(SFIcons.res(key)),
                            contentDescription = SFIcons.label(key),
                            tint = if (isSelected) ThemeGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = SFIcons.label(key),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
