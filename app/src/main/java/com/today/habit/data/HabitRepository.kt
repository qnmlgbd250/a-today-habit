package com.today.habit.data

import com.today.habit.data.dao.HabitDao
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allCheckIns: Flow<List<CheckInRecord>> = habitDao.getAllCheckIns()

    fun getCheckInsByDate(date: String): Flow<List<CheckInRecord>> {
        return habitDao.getCheckInsByDate(date)
    }

    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleCheckIn(habitId: Long, date: String, targetCount: Int) {
        val existing = habitDao.getCheckInDirectly(habitId, date)
        if (existing == null) {
            habitDao.insertCheckIn(CheckInRecord(habitId, date, 1))
        } else {
            if (existing.count < targetCount) {
                habitDao.insertCheckIn(existing.copy(count = existing.count + 1))
            } else {
                habitDao.deleteCheckIn(existing)
            }
        }
    }
    
    fun getCheckInsByHabitId(habitId: Long): Flow<List<CheckInRecord>> {
        return habitDao.getCheckInsByHabitId(habitId)
    }
}
