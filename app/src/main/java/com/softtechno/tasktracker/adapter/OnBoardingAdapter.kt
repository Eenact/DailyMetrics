package com.softtechno.tasktracker.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.enums.EntityType
import com.softtechno.tasktracker.enums.Units
import com.softtechno.tasktracker.managers.TaskCreationManager
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel

class OnBoardingAdapter() : RecyclerView.Adapter<OnBoardingAdapter.ViewHolder>() {

    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        private val mTitleTextView: TextView = view.findViewById(R.id.onboarding_screen_title)
        private val mMessageTextView: TextView = view.findViewById(R.id.onboarding_screen_msg)
        private val mLottieAnimationView: LottieAnimationView =
            view.findViewById(R.id.onboarding_screen_animation)

        private val mMainLayout: LinearLayout = view.findViewById(R.id.onboarding_main_layout)
        private val mTaskEntryLayout: ConstraintLayout =
            view.findViewById(R.id.onboarding_task_layout)

        // Task items
        private val mTaskNameLayout: TextInputLayout =
            view.findViewById(R.id.task_name_input_layout)
        private val mTaskUnitLayout: TextInputLayout =
            view.findViewById(R.id.task_units_input_layout)
        private val mTaskNameEditText: TextInputEditText =
            view.findViewById(R.id.task_name_edittext)
        private val mTaskTypeEditText: MaterialAutoCompleteTextView =
            view.findViewById(R.id.task_type_edittext)
        private val mTaskUnitEditText: MaterialAutoCompleteTextView =
            view.findViewById(R.id.task_units_edittext)
        private val mTaskColorButton: MaterialButton =
            view.findViewById(R.id.task_color_input_button)
        private var mTaskSelectedColor: Int = -1

        fun populatePage(position: Int) {
            when (position) {
                0 -> {
                    mMainLayout.visibility = View.VISIBLE
                    mTaskEntryLayout.visibility = View.GONE
                    mLottieAnimationView.setAnimation("lottie_onboarding_first.json")
                    mTitleTextView.text =
                        mTitleTextView.resources.getString(R.string.onboarding_screen1_title)
                    mMessageTextView.text =
                        mMessageTextView.resources.getString(R.string.onboarding_screen1_msg)
                }
                1 -> {
                    mMainLayout.visibility = View.VISIBLE
                    mTaskEntryLayout.visibility = View.GONE
                    mLottieAnimationView.setAnimation("lottie_onboarding_second.json")
                    mTitleTextView.text =
                        mTitleTextView.resources.getString(R.string.onboarding_screen2_title)
                    mMessageTextView.text =
                        mMessageTextView.resources.getString(R.string.onboarding_screen2_msg)
                }
                2 -> {
                    mMainLayout.visibility = View.GONE
                    mTaskEntryLayout.visibility = View.VISIBLE
                    view.findViewById<Button>(R.id.button_add).visibility = View.GONE

                    TaskCreationManager.fillUnits(mTaskUnitEditText)
                    TaskCreationManager.fillTypes(mTaskTypeEditText)

                    mTaskTypeEditText.setOnItemClickListener { _, _, i, _ ->
                        mTaskUnitLayout.isEnabled = (i != 0)
                    }
                    mTaskSelectedColor =
                        ResourcesCompat.getColor(view.resources, R.color.legend_red, null)

                    mTaskColorButton.setOnClickListener {
                        TaskCreationManager.colorPickerDialog(
                            view.context,
                            mTaskSelectedColor
                        ) { selectedColor ->
                            mTaskSelectedColor = selectedColor
                            mTaskColorButton.iconTint = ColorStateList.valueOf(selectedColor)
                        }
                    }
                }
            }
        }

        fun insertTask(taskViewModel: TaskEntityViewModel): Boolean {

            val name: String = mTaskNameEditText.editableText.toString()
            val type: EntityType = EntityType.fromString(mTaskTypeEditText.text.toString())
            val unit: Units = Units.fromString(mTaskUnitEditText.text.toString())

            if (name.isNotEmpty()) {
                taskViewModel.insert(TaskEntity(name, type, unit, mTaskSelectedColor))
                return true
            } else {
                mTaskNameLayout.error = view.resources.getString(R.string.error_field_required)
            }

            return false
        }
    }

    private lateinit var mTaskViewHolder: ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_onboarding_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.populatePage(position)
        if (position == itemCount - 1) {
            mTaskViewHolder = holder
        }
    }

    override fun getItemCount(): Int = 3

    fun performTaskInsert(taskViewModel: TaskEntityViewModel): Boolean {
        return mTaskViewHolder.insertTask(taskViewModel)
    }

}