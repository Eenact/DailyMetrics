package com.softtechno.tasktracker.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskRepository
import kotlinx.coroutines.launch

class TaskEntityViewModel(private val repository: TaskRepository): ViewModel() {

    val selectedItem = MutableLiveData<TaskEntity>()

    val allEntries: LiveData<List<TaskEntity>> = repository.getAllTaskEntities().asLiveData()

    suspend fun get(taskId: Int) = repository.getTaskEntity(taskId)

    fun insert(entity: TaskEntity) = viewModelScope.launch {
        repository.insertTaskEntity(entity)
    }

    fun update(entity: TaskEntity) = viewModelScope.launch {
        repository.updateTaskEntity(entity)
    }

    fun delete(entity: TaskEntity) = viewModelScope.launch {
        repository.deleteTaskEntity(entity)
    }

    fun setSelectedItem(itemIndex: Int) {
        selectedItem.value = allEntries.value?.get(itemIndex)
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