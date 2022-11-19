package com.softtechno.tasktracker.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskRepository
import kotlinx.coroutines.launch

class TaskEntityViewModel(private val repository: TaskRepository) : ViewModel() {

    var insertSuccessful = MutableLiveData<Boolean>()
    val selectedItem = MutableLiveData<TaskEntity>()

    val allEntries: LiveData<List<TaskEntity>> = repository.getAllTaskEntities().asLiveData()

    suspend fun get(taskId: Int) = repository.getTaskEntity(taskId)

    fun insert(entity: TaskEntity) = viewModelScope.launch {
        if (repository.containTask(entity.name)) {
            insertSuccessful.value = false
        } else {
            repository.insertTaskEntity(entity)
            insertSuccessful.value = true
        }
    }

    fun update(entity: TaskEntity, checkName: Boolean) = viewModelScope.launch {
        if (checkName && repository.containTask(entity.name)) {
            insertSuccessful.value = false
        } else {
            val update = repository.updateTaskEntity(entity)
            insertSuccessful.value = update == 1
        }
    }

    fun delete(entity: TaskEntity) = viewModelScope.launch {
        repository.deleteTaskEntity(entity)
    }

    fun setSelectedItem(itemIndex: Int): TaskEntity? {
        selectedItem.value = allEntries.value?.get(itemIndex)
        return selectedItem.value
    }
}

class TaskEntityViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        if (modelClass.isAssignableFrom(TaskEntityViewModel::class.java)) {
            return TaskEntityViewModel(TaskRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}