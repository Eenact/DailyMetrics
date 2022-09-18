package com.softtechno.tasktracker.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.softtechno.tasktracker.db.TaskRepository
import com.softtechno.tasktracker.db.TaskValue
import kotlinx.coroutines.launch

class TaskValueViewModel(private val repository: TaskRepository): ViewModel() {

    val allValues: LiveData<List<TaskValue>> = repository.getAllTaskValues()

    fun getValues(startTime: Long, endTime: Long): LiveData<List<TaskValue>> = repository.getAllTaskValuesBetween(startTime, endTime)

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