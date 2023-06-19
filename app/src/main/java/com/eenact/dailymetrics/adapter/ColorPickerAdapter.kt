package com.eenact.dailymetrics.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.eenact.dailymetrics.R

internal class ColorPickerAdapter(
    private val mColorList: List<Int>,
    private val mContext: Context
) : BaseAdapter() {

    override fun getCount(): Int {
        return mColorList.size
    }

    override fun getItem(p0: Int): Any? {
        return mColorList[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {

        val contentView: View = view ?: LayoutInflater.from(mContext)
            .inflate(R.layout.adapter_color_pick_item, parent, false)

        val imageView = contentView.findViewById<ImageView>(R.id.color_pick_item_image_view)
        imageView.setBackgroundColor(mColorList[position])

        return contentView

    }


}