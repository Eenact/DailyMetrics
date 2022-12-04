package com.softtechno.tasktracker.gui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayoutMediator
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.OnBoardingAdapter
import com.softtechno.tasktracker.databinding.ActivityOnboardingBinding
import com.softtechno.tasktracker.managers.AppSettingsManager
import com.softtechno.tasktracker.managers.AppSettingsManager.isAppOnboarded
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModel
import com.softtechno.tasktracker.viewmodels.TaskEntityViewModelFactory

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
        binding.onboardingViewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        (binding.onboardingNextBtn as MaterialButton).icon =
                            AppCompatResources.getDrawable(
                                applicationContext,
                                R.drawable.ic_right_arrow
                            )
                        binding.onboardingNextBtn.text = ""
                    }
                    1 -> {
                        (binding.onboardingNextBtn as MaterialButton).icon =
                            AppCompatResources.getDrawable(
                                applicationContext,
                                R.drawable.ic_right_arrow
                            )
                        binding.onboardingNextBtn.text = ""
                    }
                    2 -> {
                        binding.onboardingNextBtn.icon = null
                        binding.onboardingNextBtn.text = getText(R.string.onboarding_get_start)
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
                2 -> {

                    val taskInserted = mViewPagerAdapter.performTaskInsert(mTaskEntityModel)

                    // Task was correct and successfully inserted
                    if (taskInserted) {

                        binding.onboardingNextBtn.visibility = View.GONE
                        binding.onboardingProgressIndicator.visibility = View.VISIBLE
                    }
                }
            }
        }

        TabLayoutMediator(binding.onboardingTabLayoutView, binding.onboardingViewpager) { _, _ ->
        }.attach()

        mTaskEntityModel.insertSuccessful.observe(this) {
            AppSettingsManager.instance(baseContext).isAppOnboarded = true

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

}