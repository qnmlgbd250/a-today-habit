package com.today.habit.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.today.habit.ui.theme.WallpaperScaffold
import com.today.habit.ui.component.HabitIcons
import com.today.habit.ui.component.IOSNavBar
import com.today.habit.ui.component.iosPressable
import com.today.habit.ui.theme.IOSColors
import com.today.habit.ui.theme.IOSType
import com.today.habit.ui.theme.WallpaperPreview
import com.today.habit.ui.theme.Wallpapers
import com.today.habit.ui.viewmodel.HabitViewModel

/**
 * 壁纸屏：五套内置底衬，点选即全局生效（持久化）。
 * 卡片是液态玻璃——底衬就是它的底气。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperScreen(navController: NavController, viewModel: HabitViewModel) {
    val wallpaperId = viewModel.wallpaperId.value
    val current by viewModel.wallpaperId
    val gridState = rememberLazyGridState()

    WallpaperScaffold(
        wallpaperId = wallpaperId,
        topBar = {
            IOSNavBar(
                title = "壁纸",
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Wallpapers, key = { it.id }) { def ->
                val isSel = current == def.id
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .iosPressable { viewModel.setWallpaper(def.id) }
                ) {
                    WallpaperPreview(
                        id = def.id,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                    )
                    // 选中徽标
                    if (isSel) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(IOSColors.green)
                        ) {
                            Icon(
                                painter = painterResource(HabitIcons.getRes("checkmark")),
                                contentDescription = "已选",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        def.name,
                        style = IOSType.headline,
                        color = IOSColors.label,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}
