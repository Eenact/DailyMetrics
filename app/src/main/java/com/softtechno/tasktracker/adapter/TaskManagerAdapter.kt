package com.softtechno.tasktracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.enums.Units

class TaskManagerAdapter() : RecyclerView.Adapter<TaskManagerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val itemName: TextView
        val itemType: TextView
        val itemUnit: TextView
        val itemColor: ImageView

        init {
            itemName = view.findViewById(R.id.item_task_manager_name)
            itemType = view.findViewById(R.id.item_task_manager_type)
            itemUnit = view.findViewById(R.id.item_task_manager_unit)
            itemColor = view.findViewById(R.id.item_task_manager_color)
        }

    }

    private var contentList: List<TaskEntity> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_task_manager_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskItem = contentList[position]
        holder.itemName.text = taskItem.name
        holder.itemType.text = taskItem.type.name
        if (taskItem.unit != Units.Other) {
            holder.itemUnit.visibility = View.VISIBLE
            holder.itemUnit.text = taskItem.unit.name
        }
        holder.itemColor.setBackgroundColor(taskItem.color)
    }

    override fun getItemCount() = contentList.size

    fun updateItems(items: List<TaskEntity>) {
        contentList = items
    }
}