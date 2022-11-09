package com.softtechno.tasktracker.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskValue
import com.softtechno.tasktracker.enums.EntityType
import com.softtechno.tasktracker.enums.Units

data class DashboardListData(
    val isHeader: Boolean,
    val headerString: String?,
    val entryValue: TaskValue?,
    val entity: TaskEntity?
)

class DashboardListAdapter(private var dataSet: MutableList<DashboardListData>) :
    RecyclerView.Adapter<DashboardListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val name: TextView
        val unit: TextView
        val value: TextView
        val comment: TextView

        init {
            name = view.findViewById(R.id.itemName)
            unit = view.findViewById(R.id.itemUnit)
            value = view.findViewById(R.id.itemValue)
            comment = view.findViewById(R.id.itemComment)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_dashboard_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = dataSet[position]
        if (data.isHeader) {
            holder.name.text = data.headerString
            holder.name.typeface = Typeface.DEFAULT_BOLD
        } else {
            holder.name.text = data.entity?.name
            holder.value.text = when(data.entity?.type) {
                EntityType.YesNo -> if ((data.entryValue?.value?.toInt() ?: 0) == 1) "True" else "False"
                else -> data.entryValue?.value.toString()
            }
            if (data.entity?.unit != null && data.entity.unit != Units.Other) {
                holder.unit.visibility = View.VISIBLE
                holder.unit.text = data.entity.unit.name
            }
            if (!data.entryValue?.comments.isNullOrEmpty()) {
                holder.comment.visibility = View.VISIBLE
                holder.comment.text = data.entryValue?.comments ?: ""
            }
        }

    }

    override fun getItemCount() = dataSet.size

    fun updateItems(dataItems: List<DashboardListData>) {
        dataSet.clear()
        dataSet.addAll(dataItems)
    }

}