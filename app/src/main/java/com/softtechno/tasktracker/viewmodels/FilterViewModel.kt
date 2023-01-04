package com.softtechno.tasktracker.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilterViewModel() : ViewModel() {

    private val mFilterUpdated = MutableLiveData(false)
    val filterUpdated: LiveData<Boolean> get() = mFilterUpdated
    fun notifyFilterChanged() {
        mFilterUpdated.value = true
    }

    private val mFilteredTaskIds = MutableLiveData<MutableMap<Long, Boolean>>()
    val filteredTasks: LiveData<MutableMap<Long, Boolean>> get() = mFilteredTaskIds

    fun setFilteredTasks(taskIds: MutableMap<Long, Boolean>) {
        mFilteredTaskIds.value = taskIds
    }

}