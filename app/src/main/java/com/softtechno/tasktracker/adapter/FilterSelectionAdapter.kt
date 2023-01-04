package com.softtechno.tasktracker.adapter

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.db.TaskEntity

class FilterSelectionAdapter(
    private val entities: Array<TaskEntity>,
    private val filtered: Map<Long, Boolean>
) :
    RecyclerView.Adapter<FilterSelectionAdapter.ViewHolder>() {

    private val mViewHolders = mutableListOf<ViewHolder>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var mEntityIndex: Long = -1
        private val mCheckBox: CheckBox

        init {
            mCheckBox = view as CheckBox
            mCheckBox.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                view.context.resources.getDimension(R.dimen.filter_checkbox_text)
            )
        }

        fun setText(entity: TaskEntity, isChecked: Boolean) {
            mEntityIndex = entity.id
            mCheckBox.text = entity.name
            mCheckBox.isChecked = isChecked
        }

        fun isChecked(): Boolean = mCheckBox.isChecked
        fun entityId(): Long = mEntityIndex

        fun setChecked(checked: Boolean) {
            mCheckBox.isChecked = checked
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(CheckBox(parent.context))
        mViewHolders.add(viewHolder)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val entity = entities[position]
        holder.setText(entity, filtered[entity.id] ?: true)

    }

    override fun getItemCount(): Int = entities.size

    fun checkAll(isChecked: Boolean) {
        mViewHolders.forEach { it.setChecked(isChecked) }
    }

    fun getAllFilters(): MutableMap<Long, Boolean> {
        return mViewHolders.associate { it.entityId() to it.isChecked() }.toMutableMap()
    }

}