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

class TaskManagerAdapter(private val onClickListener: OnClickListener) : RecyclerView.Adapter<TaskManagerAdapter.ViewHolder>() {

    class OnClickListener(val clickListener: (taks: TaskEntity) -> Unit) {
        fun onClick(task: TaskEntity) = clickListener(task)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val itemName: TextView
        private val itemType: TextView
        private val itemUnit: TextView
        private val itemColor: ImageView

        init {
            itemName = view.findViewById(R.id.item_task_manager_name)
            itemType = view.findViewById(R.id.item_task_manager_type)
            itemUnit = view.findViewById(R.id.item_task_manager_unit)
            itemColor = view.findViewById(R.id.item_task_manager_color)
        }

        fun bind(task: TaskEntity) {
            itemName.text = task.name
            itemType.text = task.type.toString()
            if (task.unit != Units.Other) {
                itemUnit.visibility = View.VISIBLE
                itemUnit.text = task.unit.toString()
            }
            itemColor.setBackgroundColor(task.color)
        }

    }

    private var contentList: MutableList<TaskEntity> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_task_manager_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskItem = contentList[position]

        holder.bind(taskItem)
        holder.itemView.setOnClickListener { onClickListener.onClick(taskItem) }
    }

    override fun getItemCount() = contentList.size

    fun updateItems(items: List<TaskEntity>) {
        contentList = items.toMutableList()
        notifyDataSetChanged()
    }

    fun addItem(position: Int, item: TaskEntity) {
        contentList.add(position, item)
        notifyItemChanged(position)
    }

}