package com.eenact.dailymetrics.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eenact.dailymetrics.R
import com.eenact.dailymetrics.enums.ExportTypes

class ExportListAdapter(private val onClickListener: OnClickListener) :
    RecyclerView.Adapter<ExportListAdapter.ViewHolder>() {

    class OnClickListener(val clickListener: (exportType: ExportTypes) -> Unit) {
        fun onClick(exportType: ExportTypes) = clickListener(exportType)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val imageView: ImageView
        private val textView: TextView

        init {
            imageView = view.findViewById(R.id.adapter_export_item_image_view)
            textView = view.findViewById(R.id.adapter_export_item_text_view)
        }

        fun setContent(type: ExportTypes) {

            when (type) {

                ExportTypes.ShareData -> {
                    imageView.setImageResource(R.drawable.ic_share)
                    textView.setText(R.string.export_share_list)
                }

                ExportTypes.ExportData -> {
                    imageView.setImageResource(R.drawable.ic_save)
                    textView.setText(R.string.export_data_list)
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_export_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val type = ExportTypes.fromPosition(position)
        holder.setContent(type)
        holder.itemView.setOnClickListener { onClickListener.onClick(type) }

    }

    override fun getItemCount(): Int = 2

}