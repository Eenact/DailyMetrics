package com.softtechno.tasktracker.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskEntityViewModel(private val repository: TaskRepository): ViewModel() {

    val allEntries: Flow<List<TaskEntity>> = repository.getAllTaskEntities()

    fun insert(entity: TaskEntity) = viewModelScope.launch {
        repository.insertTaskEntity(entity)
    }

    fun update(entity: TaskEntity) = viewModelScope.launch {
        repository.updateTaskEntity(entity)
    }

    fun delete(entity: TaskEntity) = viewModelScope.launch {
        repository.deleteTaskEntity(entity)
    }
}

class TaskEntityViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(TaskEntityViewModel::class.java)) {
            return TaskEntityViewModel(TaskRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}