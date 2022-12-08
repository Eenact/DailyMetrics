package com.softtechno.tasktracker.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskValue
import com.softtechno.tasktracker.enums.EntityType
import com.softtechno.tasktracker.enums.Units
import com.softtechno.tasktracker.utils.DateTimeUtils

data class DashboardListData(
    val isHeader: Boolean,
    val headerString: String?,
    val entryValue: TaskValue?,
    val entity: TaskEntity?
)

class DashboardListAdapter(private var dataSet: MutableList<DashboardListData>) :
    RecyclerView.Adapter<DashboardListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val time: TextView
        val name: TextView
        val value: TextView
        val comment: TextView
        val itemsLayout: LinearLayout

        init {
            time = view.findViewById(R.id.itemDate)
            name = view.findViewById(R.id.itemName)
            value = view.findViewById(R.id.itemValue)
            comment = view.findViewById(R.id.itemComment)
            itemsLayout = view.findViewById(R.id.itemLayout)
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
            holder.time.text = data.headerString
            holder.time.typeface = Typeface.DEFAULT_BOLD
            holder.itemsLayout.visibility = View.GONE
        } else {

            // Set date time
            val date = DateTimeUtils.DateFormatter.format(data.entryValue?.time)
            val time = DateTimeUtils.TimeFormatter.format(data.entryValue?.time)
            holder.time.text =
                holder.time.resources.getString(R.string.dashboard_item_placeholder, date, time)
            holder.name.text = data.entity?.name

            // Set value with unit
            val value = when (data.entity?.type) {
                EntityType.YesNo -> if ((data.entryValue?.value?.toInt()
                        ?: 0) == 1
                ) "True" else "False"
                else -> data.entryValue?.value.toString()
            }
            val unit =
                if (data.entity?.unit != null && data.entity.unit != Units.Other) data.entity.unit.name else ""
            holder.value.text =
                holder.value.resources.getString(R.string.dashboard_item_placeholder, value, unit)

            if (!data.entryValue?.comments.isNullOrEmpty()) {
                holder.comment.visibility = View.VISIBLE
                holder.comment.text = data.entryValue?.comments ?: ""
            }
        }

    }

    override fun getItemCount() = dataSet.size

    fun getItem(position: Int): DashboardListData {
        return dataSet[position]
    }

    fun updateItems(dataItems: List<DashboardListData>) {
        dataSet.clear()
        dataSet.addAll(dataItems)
    }

}