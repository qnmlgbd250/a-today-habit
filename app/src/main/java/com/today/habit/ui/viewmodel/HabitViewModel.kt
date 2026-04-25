package com.today.habit.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.today.habit.data.HabitRepository
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _selectedDate = mutableStateOf(LocalDate.now())
    val selectedDate: State<LocalDate> = _selectedDate

    val allHabits: LiveData<List<Habit>> = repository.allHabits.asLiveData()
    val allCheckIns: LiveData<List<CheckInRecord>> = repository.allCheckIns.asLiveData()

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun getFilteredHabits(habits: List<Habit>, date: LocalDate): List<Habit> {
        return habits.filter { habit ->
            when (habit.frequency) {
                "DAILY" -> true
                "WEEKDAYS" -> {
                    val dayOfWeek = date.dayOfWeek.value // 1 (Mon) to 7 (Sun)
                    dayOfWeek in 1..5
                }
                "WEEKLY" -> {
                    val dayOfWeek = date.dayOfWeek.value.toString() // 1-7
                    habit.frequencyValue.split(",").contains(dayOfWeek)
                }
                "MONTHLY" -> {
                    val dayOfMonth = date.dayOfMonth.toString()
                    habit.frequencyValue.split(",").contains(dayOfMonth)
                }
                else -> true
            }
        }
    }

    fun getCheckInsByDate(date: String): LiveData<List<CheckInRecord>> {
        return repository.getCheckInsByDate(date).asLiveData()
    }

    fun insertHabit(name: String, description: String, frequency: String, frequencyValue: String, icon: String = "Sunny", targetCount: Int = 1) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                icon = icon,
                color = 0,
                frequency = frequency,
                frequencyValue = frequencyValue,
                targetCount = targetCount
            )
            repository.insertHabit(habit)
        }
    }

    fun toggleCheckIn(habitId: Long, date: String, targetCount: Int) {
        viewModelScope.launch {
            repository.toggleCheckIn(habitId, date, targetCount)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }
    
    fun getCheckInsByHabitId(habitId: Long): Flow<List<CheckInRecord>> {
        return repository.getCheckInsByHabitId(habitId)
    }
}

class HabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
