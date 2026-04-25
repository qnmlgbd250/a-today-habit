package com.today.habit.data.dao

import androidx.room.*
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM check_in_records WHERE date = :date")
    fun getCheckInsByDate(date: String): Flow<List<CheckInRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInRecord)

    @Query("SELECT * FROM check_in_records WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCheckInDirectly(habitId: Long, date: String): CheckInRecord?

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckInRecord)
    
    @Query("SELECT * FROM check_in_records WHERE habitId = :habitId")
    fun getCheckInsByHabitId(habitId: Long): Flow<List<CheckInRecord>>

    @Query("SELECT * FROM check_in_records")
    fun getAllCheckIns(): Flow<List<CheckInRecord>>

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM check_in_records")
    suspend fun deleteAllCheckIns()
}

