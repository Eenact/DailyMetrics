package com.softtechno.tasktracker.managers

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.ColorPickerAdapter
import com.softtechno.tasktracker.enums.EntityType
import com.softtechno.tasktracker.enums.Units
import java.util.*

class TaskCreationManager {

    companion object {

        fun fillTypes(taskTypeEditText: MaterialAutoCompleteTextView) {
            taskTypeEditText.setSimpleItems(
                EntityType.values().map { it.toString() }.toTypedArray()
            )
            taskTypeEditText.setText(
                EntityType.YesNo.toString(), false
            )
        }

        fun fillUnits(taskUnits: MaterialAutoCompleteTextView) {
            taskUnits.setSimpleItems(
                Units.values().map { it.toString() }.toTypedArray()
            )
        }

        fun colorPickerDialog(
            context: Context,
            defaultColor: Int,
            buttonCallback: (selectedColor: Int) -> Unit
        ): View {

            var selectedColor: Int = defaultColor
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_color_picker, null, false)

            val gridView = dialogView.findViewById<GridView>(R.id.color_pick_grid_view)
            val randomColor = dialogView.findViewById<Button>(R.id.color_pick_random_btn)
            val previewView = dialogView.findViewById<ImageView>(R.id.color_pick_preview_view)

            val arrayList: List<Int> = context.resources.getIntArray(R.array.legend_colors).toList()
            gridView.adapter = ColorPickerAdapter(arrayList, context)

            gridView.setOnItemClickListener { adapterView, view, i, l ->
                selectedColor = arrayList[i]

                previewView.setBackgroundColor(selectedColor)
            }
            randomColor.setOnClickListener {
                val rnd = Random()
                selectedColor =
                    Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))

                previewView.setBackgroundColor(selectedColor)
            }

            MaterialAlertDialogBuilder(
                context,
                R.style.ThemeOverlay_App_MaterialAlertDialog
            )
                .setTitle(R.string.dialog_color_pick_title)
                .setView(dialogView).setNegativeButton(android.R.string.cancel) { _, _ ->
                    buttonCallback(defaultColor)
                }.setPositiveButton(android.R.string.ok) { _, _ ->
                    buttonCallback(selectedColor)
                }
                .show()

            return dialogView
        }

    }

}