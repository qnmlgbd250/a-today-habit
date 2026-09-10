package com.today.habit.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.today.habit.ui.component.IOSNavBar
import com.today.habit.ui.component.IOSPill
import com.today.habit.ui.component.IOSSearchBar
import com.today.habit.ui.component.iosPressable
import com.today.habit.ui.component.SFIcons
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType

/**
 * 图标库选择页：搜索 + 分类 + 网格。
 * 进入时通过路由参数传入当前图标 key，选择后通过 savedStateHandle 回传 "picked_icon"。
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            IOSNavBar(
                title = "图标",
                onBack = { navController.popBackStack() },
                elevated = true
            )
        },
        containerColor = IOSColors.background
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                IOSSearchBar(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "搜索图标"
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(listOf("全部") + SFIcons.Categories.map { it.first }) { label ->
                        IOSPill(label = label, selected = category == label, onClick = { category = label })
                    }
                }
            }
            items(filtered, key = { it }) { key ->
                val isSelected = key == selectedKey ||
                    SFIcons.res(key) == SFIcons.res(selectedKey)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.iosPressable(pressedScale = 0.9f) { pick(key) }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) IOSColors.blue else IOSColors.card)
                    ) {
                        Icon(
                            painter = painterResource(SFIcons.res(key)),
                            contentDescription = SFIcons.label(key),
                            tint = if (isSelected) Color.White else IOSColors.secondaryLabel,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        text = SFIcons.label(key),
                        style = IOSType.caption,
                        color = if (isSelected) IOSColors.blue else IOSColors.secondaryLabel,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                }
            }
        }
    }
}
