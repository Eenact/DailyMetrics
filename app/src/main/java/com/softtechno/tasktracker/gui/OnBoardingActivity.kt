package com.softtechno.tasktracker.gui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.softtechno.tasktracker.adapter.OnBoardingAdapter
import com.softtechno.tasktracker.databinding.ActivityOnboardingBinding
import com.softtechno.tasktracker.managers.AppSettingsManager
import com.softtechno.tasktracker.managers.AppSettingsManager.isAppOnboarded

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.onboardingViewpager.adapter = OnBoardingAdapter()
        binding.onboardingGetStartBtn.setOnClickListener {

            AppSettingsManager.instance(baseContext).isAppOnboarded = true

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        TabLayoutMediator(binding.onboardingTabLayoutView, binding.onboardingViewpager) { _, _ ->
        }.attach()
    }

}