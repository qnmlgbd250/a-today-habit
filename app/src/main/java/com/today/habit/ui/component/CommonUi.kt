package com.today.habit.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun HandDrawnSun(modifier: Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val center = center
        val sizeVal = size.minDimension
        val radius = sizeVal / 4.5f
        
        // 核心圆圈填充黄色
        drawCircle(
            color = Color(0xFFFFEB3B), // 明亮黄
            radius = radius,
            center = center
        )
        
        // 核心圆圈边框
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // 阳光线条
        val rayCount = 12
        val innerRadius = radius + 3.dp.toPx()
        val outerRadius = radius + 7.dp.toPx()
        
        for (i in 0 until rayCount) {
            val angle = (i * 360f / rayCount) * (Math.PI / 180f).toFloat()
            val start = Offset(
                center.x + Math.cos(angle.toDouble()).toFloat() * innerRadius,
                center.y + Math.sin(angle.toDouble()).toFloat() * innerRadius
            )
            val end = Offset(
                center.x + Math.cos(angle.toDouble()).toFloat() * outerRadius,
                center.y + Math.sin(angle.toDouble()).toFloat() * outerRadius
            )
            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = 1.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
