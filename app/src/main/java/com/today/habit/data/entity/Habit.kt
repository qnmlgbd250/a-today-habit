package com.today.habit.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("icon")
    val icon: String = "Sunny",

    @SerializedName("color")
    val color: Int = 0,

    @SerializedName("frequency")
    val frequency: String = "DAILY", // DAILY, WEEKLY, MONTHLY

    @SerializedName("frequencyValue")
    val frequencyValue: String = "",

    @SerializedName("targetCount")
    val targetCount: Int = 1,

    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis()
)

