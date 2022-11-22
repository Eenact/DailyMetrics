package com.softtechno.tasktracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.softtechno.tasktracker.R

class OnBoardingAdapter() : RecyclerView.Adapter<OnBoardingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val lottieAnimationView: LottieAnimationView
        val titleTextView: TextView
        val messageTextView: TextView

        init {
            lottieAnimationView = view.findViewById(R.id.onboarding_screen_animation)
            titleTextView = view.findViewById(R.id.onboarding_screen_title)
            messageTextView = view.findViewById(R.id.onboarding_screen_msg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_onboarding_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (position) {
            0 -> {
                holder.lottieAnimationView.setAnimation("lottie_onboarding_first.json")
                holder.titleTextView.text =
                    holder.titleTextView.resources.getString(R.string.onboarding_screen1_title)
                holder.messageTextView.text =
                    holder.messageTextView.resources.getString(R.string.onboarding_screen1_msg)
            }
            1 -> {
                holder.lottieAnimationView.setAnimation("lottie_onboarding_second.json")
                holder.titleTextView.text =
                    holder.titleTextView.resources.getString(R.string.onboarding_screen2_title)
                holder.messageTextView.text =
                    holder.messageTextView.resources.getString(R.string.onboarding_screen2_msg)
            }
        }
    }

    override fun getItemCount(): Int = 2


}