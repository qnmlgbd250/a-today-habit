package com.today.habit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val icon: String,
    val color: Int,
    val frequency: String = "DAILY", // DAILY, WEEKLY, MONTHLY
    val frequencyValue: String = "", // 如 "1,3,5" 表示周一三五
    val targetCount: Int = 1, // 每日目标次数
    val createdAt: Long = System.currentTimeMillis()
)
