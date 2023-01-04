package com.softtechno.tasktracker.gui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.databinding.ActivityMainBinding
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.gui.fragments.MainBottomSheetFragment
import com.softtechno.tasktracker.managers.AppSettingsManager
import com.softtechno.tasktracker.managers.AppSettingsManager.isAppOnboarded
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    var addButtonClickListener: ((View) -> Unit)? by Delegates.observable(null) { _, _, newValue ->
        binding.addButton.visibility = if (newValue == null) View.GONE else View.VISIBLE
    }

    private lateinit var mBottomSheetDialog: MainBottomSheetFragment
    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        if (!AppSettingsManager.instance(this).isAppOnboarded) {
            val intent = Intent(this, OnBoardingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        mAppBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, mAppBarConfiguration)

        binding.addButton.setOnClickListener {
            addButtonClickListener?.invoke(it)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(mAppBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun getBottomSheet(layout: Int, entries: Array<TaskEntity>): MainBottomSheetFragment {
        mBottomSheetDialog = MainBottomSheetFragment(layout, entries)
        return mBottomSheetDialog
    }
}