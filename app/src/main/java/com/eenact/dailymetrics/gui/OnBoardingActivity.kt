package com.eenact.dailymetrics.gui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.eenact.dailymetrics.adapter.OnBoardingAdapter
import com.eenact.dailymetrics.databinding.ActivityOnboardingBinding
import com.eenact.dailymetrics.managers.AppSettingsManager
import com.eenact.dailymetrics.managers.AppSettingsManager.isAppOnboarded
import com.eenact.dailymetrics.viewmodels.TaskEntityViewModel
import com.eenact.dailymetrics.viewmodels.TaskEntityViewModelFactory

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var mViewPagerAdapter: OnBoardingAdapter

    private val mTaskEntityModel: TaskEntityViewModel by viewModels {
        TaskEntityViewModelFactory(baseContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mViewPagerAdapter = OnBoardingAdapter()
        binding.onboardingViewpager.isUserInputEnabled = false
        binding.onboardingViewpager.adapter = mViewPagerAdapter

        TabLayoutMediator(binding.onboardingTabLayoutView, binding.onboardingViewpager) { _, _ ->
        }.attach()


        binding.onboardingViewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.onboardingNextBtn.visibility = View.VISIBLE
                        binding.onboardingFinishBtn.visibility = View.GONE
                    }
                    1 -> {
                        binding.onboardingNextBtn.visibility = View.VISIBLE
                        binding.onboardingFinishBtn.visibility = View.GONE
                    }
                    2 -> {
                        binding.onboardingNextBtn.visibility = View.GONE
                        binding.onboardingFinishBtn.visibility = View.VISIBLE
                    }
                }
            }
        })
        binding.onboardingNextBtn.setOnClickListener {

            when (binding.onboardingViewpager.currentItem) {
                0 -> {
                    binding.onboardingViewpager.currentItem = 1
                }
                1 -> {
                    binding.onboardingViewpager.currentItem = 2
                }
            }
        }
        binding.onboardingFinishBtn.setOnClickListener {
            val taskInsertStarted = mViewPagerAdapter.performTaskInsert(mTaskEntityModel)

            // Task was correct and successfully inserted
            if (taskInsertStarted) {

                binding.onboardingFinishBtn.visibility = View.GONE
                binding.onboardingProgressIndicator.visibility = View.VISIBLE
            }
        }

        mTaskEntityModel.insertSuccessful.observe(this) {
            AppSettingsManager.instance(baseContext).isAppOnboarded = true

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

}