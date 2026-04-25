package com.today.habit.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "check_in_records",
    primaryKeys = ["habitId", "date"],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId")]
)
data class CheckInRecord(
    @SerializedName("habitId")
    val habitId: Long,
    
    @SerializedName("date")
    val date: String, // 格式?"yyyy-MM-dd"
    
    @SerializedName("count")
    val count: Int = 1 // 当日已完成次数
)

