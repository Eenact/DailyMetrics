package com.softtechno.tasktracker.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.softtechno.tasktracker.db.TaskRepository
import com.softtechno.tasktracker.db.TaskValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskValueViewModel(private val repository: TaskRepository): ViewModel() {

    val taskEnteredId = MutableLiveData<Int>()
    val taskEntryTime = MutableLiveData<Long>()
    val taskEntryDate = MutableLiveData<Long>()
    val taskEnteredValue = MutableLiveData<Long>()

    val allValues: LiveData<List<TaskValue>> = repository.getAllTaskValues().asLiveData()

    fun getValues(startTime: Long, endTime: Long): LiveData<List<TaskValue>> = repository.getAllTaskValuesBetween(startTime, endTime).asLiveData()

    fun insert(value: TaskValue) = viewModelScope.launch {
        repository.insertTaskValue(value)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAllTaskValues()
    }

    fun delete(value: TaskValue) = viewModelScope.launch {
        repository.deleteTaskValue(value)
    }
}

class TaskViewViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(TaskValueViewModel::class.java)) {
            return TaskValueViewModel(TaskRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}