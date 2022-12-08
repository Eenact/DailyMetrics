package com.softtechno.tasktracker.utils

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.softtechno.tasktracker.R

class RecyclerViewUtils {

    companion object {

        fun swipeToDelete(
            resources: Resources,
            deleteTask: (viewHolder: ViewHolder) -> Unit
        ): ItemTouchHelper {
            return ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                // we want to cache these and not allocate anything repeatedly in the onChildDraw method
                private var background: Drawable? = null
                private var deleteText: String? = null
                private var paint: Paint? = null
                private var leftMargin: Float = -1f
                private var initiated = false

                private fun init() {
                    deleteText = resources.getString(R.string.delete)
                    background = ColorDrawable(Color.RED)
                    paint = Paint()
                    paint?.color = Color.WHITE
                    paint?.textSize =
                        resources.getDimension(R.dimen.task_manager_delete_hint) * resources.displayMetrics.density
                    leftMargin =
                        resources.getDimension(R.dimen.task_manager_delete_margin) * resources.displayMetrics.density
                    initiated = true
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder,
                    target: ViewHolder
                ): Boolean {
                    // this method is called
                    // when the item is moved.
                    return false
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView

                    // not sure why, but this method get's called for viewholder that are already swiped away
                    if (viewHolder.adapterPosition == -1) {
                        // not interested in those
                        return
                    }
                    if (!initiated) {
                        init()
                    }

                    // draw red background
                    background?.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background?.draw(c)

                    // draw delete
                    val textBox = Rect(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right - leftMargin.toInt(),
                        itemView.bottom
                    )
                    val numOfChars =
                        paint?.breakText(
                            deleteText.toString(),
                            true,
                            textBox.width().toFloat(),
                            null
                        )
                    val start = (deleteText.toString().length - numOfChars!!) / 2
                    val bounds = Rect()
                    paint?.getTextBounds(
                        deleteText.toString(),
                        0,
                        deleteText.toString().length,
                        bounds
                    )
                    val y: Float =
                        itemView.bottom - (itemView.bottom - itemView.top) / 2f + (bounds.height() / 2f)
                    c.drawText(
                        deleteText.toString(),
                        start,
                        start + numOfChars,
                        textBox.exactCenterX(),
                        y,
                        paint!!
                    )


                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }

                override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
                    deleteTask(viewHolder)
                }
                // at last we are adding this
                // to our recycler view.
            })
        }

    }

}