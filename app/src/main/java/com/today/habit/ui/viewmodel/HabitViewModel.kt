package com.today.habit.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.google.gson.Gson
import com.today.habit.data.HabitRepository
import com.today.habit.data.entity.CheckInRecord
import com.today.habit.data.entity.Habit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

import com.google.gson.annotations.SerializedName

data class BackupData(
    @SerializedName("habits")
    val habits: List<Habit>,
    
    @SerializedName("records")
    val records: List<CheckInRecord>
)

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {
    private val gson = Gson()

    private val _selectedDate = mutableStateOf(LocalDate.now())
    val selectedDate: State<LocalDate> = _selectedDate

    // 记录用户是否手动选择了特定日期
    // 如果为 false，表示用户处于“今日”视图，应该随时间自动跳转
    private var isManuallySelected = false

    val allHabits: LiveData<List<Habit>> = repository.allHabits.asLiveData()
    val allCheckIns: LiveData<List<CheckInRecord>> = repository.allCheckIns.asLiveData()

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        isManuallySelected = date != LocalDate.now()
    }

    /**
     * 当应用恢复到前台时调用，用于刷新“今日”状态
     */
    fun refreshDateIfNecessary() {
        val today = LocalDate.now()
        // 如果用户没有手动选择日期，或者当前选择的日期已经过期了
        if (!isManuallySelected || _selectedDate.value.isBefore(today)) {
            _selectedDate.value = today
            isManuallySelected = false
        }
    }

    fun getFilteredHabits(habits: List<Habit>, date: LocalDate): List<Habit> {
        return habits.filter { habit ->
            when (habit.frequency) {
                "DAILY" -> true
                "WEEKDAYS" -> {
                    val dayOfWeek = date.dayOfWeek.value
                    dayOfWeek in 1..5
                }
                "WEEKLY" -> {
                    val dayOfWeek = date.dayOfWeek.value.toString()
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

    suspend fun exportDataJson(): String {
        val habits = repository.allHabits.first()
        val records = repository.allCheckIns.first()
        val data = BackupData(habits, records)
        return gson.toJson(data)
    }

    fun importDataJson(json: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val data = gson.fromJson(json, BackupData::class.java)
                if (data == null || data.habits == null) {
                    onComplete(false, "备份文件格式不正确或内容为空")
                    return@launch
                }
                
                // 执行数据库操作
                repository.clearAllData()
                
                // 批量插入习惯
                repository.insertHabits(data.habits)
                
                // 批量插入记录
                if (data.records != null && data.records.isNotEmpty()) {
                    repository.insertCheckIns(data.records)
                }
                
                onComplete(true, null)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, "恢复过程中发生错误: ${e.localizedMessage}")
            }
        }
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
