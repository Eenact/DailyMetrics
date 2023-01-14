package com.softtechno.tasktracker.gui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.snackbar.Snackbar
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.adapter.DashboardListAdapter
import com.softtechno.tasktracker.adapter.DashboardListData
import com.softtechno.tasktracker.databinding.FragmentDashboardBinding
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskValue
import com.softtechno.tasktracker.enums.AxisXTypes
import com.softtechno.tasktracker.gui.MainActivity
import com.softtechno.tasktracker.utils.AxisUtils
import com.softtechno.tasktracker.utils.RecyclerViewUtils
import com.softtechno.tasktracker.viewmodels.*
import java.util.*
import kotlin.math.max


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DashboardFragment : Fragment() {

    private var mAxisXLabels: Map<Int, String> = mapOf()
    private var mAxisXSelection: AxisXTypes = AxisXTypes.Week
    private var mDashboardCalendar: Calendar = Calendar.getInstance()

    private lateinit var mRecyclerListAdapter: DashboardListAdapter

    private var _binding: FragmentDashboardBinding? = null

    // View models
    private val mFilteredTaskIds: FilterViewModel by activityViewModels()
    private val mTaskValuesModel: TaskValueViewModel by activityViewModels {
        TaskValueViewModelFactory(requireActivity())
    }
    private val mTaskEntityModel: TaskEntityViewModel by activityViewModels {
        TaskEntityViewModelFactory(requireActivity())
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        setupMenu()

        initChartView()
        setListGestures()

        // Chart view Days month toggle buttons
        binding.toggleButtons.check(binding.weekToggleButton.id)
        binding.toggleButtons.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) {
                return@addOnButtonCheckedListener
            }
            mAxisXSelection = when (checkedId) {
                R.id.dayToggleButton -> AxisXTypes.Day
                R.id.weekToggleButton -> AxisXTypes.Week
                R.id.monthToggleButton -> AxisXTypes.Month
                R.id.days90ToggleButton -> AxisXTypes.ThreeMonths
                else -> AxisXTypes.SixMonths
            }
            mTaskValuesModel.allValues.value?.let { constructGraph(it, mAxisXSelection, -1, true) }
        }

        // Chart view navigational view buttons
        binding.leftArrowButton.setOnClickListener {
            mTaskValuesModel.allValues.value?.let { constructGraph(it, mAxisXSelection, -1, false) }
        }
        binding.rightArrowButton.setOnClickListener {
            mTaskValuesModel.allValues.value?.let { constructGraph(it, mAxisXSelection, 1, false) }
        }

        // Filter view buttons
        binding.dashboardFilterBtn.setOnClickListener { view ->

            val items = mTaskEntityModel.allEntries.value
            if (items.isNullOrEmpty()) {
                noTaskSnack(view)
            } else {
                (activity as MainActivity).getBottomSheet(
                    R.layout.dialog_filter_bottom_sheet, items.toTypedArray(), null
                ).show(requireActivity().supportFragmentManager, MainBottomSheetFragment.TAG)
            }
        }

        // Setting up recycler view
        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mRecyclerListAdapter = DashboardListAdapter(mutableListOf())
        binding.dashboardRecyclerView.adapter = mRecyclerListAdapter
        binding.dashboardRecyclerView.layoutManager = linearLayoutManager
        binding.dashboardRecyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(), linearLayoutManager.orientation
            )
        )

        // Adding all the viewmodel observers
        mTaskValuesModel.allValues.observe(viewLifecycleOwner) { entries ->
            if (entries.isNullOrEmpty()) {
                return@observe
            }
            if (mFilteredTaskIds.filteredTasks.value.isNullOrEmpty()) {
                mFilteredTaskIds.setFilteredTasks(entries.keys.associate { it.id to true }
                    .toMutableMap())
            }

            updateUIItems(entries)
        }
        mTaskEntityModel.allEntries.observe(viewLifecycleOwner) { entries ->
            if (entries.isNullOrEmpty()) {
                noTaskSnack(binding.root)
            } else {
                entries.forEach {
                    if (mFilteredTaskIds.filteredTasks.value?.get(it.id) == null) {
                        mFilteredTaskIds.insertTask(it.id)
                    }
                }
            }
        }
        mFilteredTaskIds.filterUpdated.observe(viewLifecycleOwner) {
            if (it && mTaskValuesModel.allValues.value.isNullOrEmpty() == false) {
                updateUIItems(mTaskValuesModel.allValues.value!!)
            }
        }

        return binding.root

    }

    override fun onResume() {
        super.onResume()

        (activity as? MainActivity).let {
            it?.addButtonClickListener = { button ->
                if (!mTaskEntityModel.allEntries.value.isNullOrEmpty()) {
                    findNavController().navigate(R.id.action_DashboardFragment_to_TaskEntryFragment)
                } else {
                    noTaskSnack(button)
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()

        (activity as? MainActivity).let {
            it?.addButtonClickListener = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button.
                return when (menuItem.itemId) {
                    R.id.action_manager_tasks -> {
                        findNavController().navigate(R.id.action_DashboardFragment_to_TaskManagerFragment)
                        true
                    }
                    R.id.action_legal_terms -> {
                        findNavController().navigate(R.id.action_DashboardFragment_to_LegalTermsFragment)
                        true
                    }
                    R.id.action_export_data -> {
                        val allContent = mTaskValuesModel.allValues.value
                        if (allContent.isNullOrEmpty()) {
                            noEntryValueSnack(binding.root)
                        } else {
                            (activity as MainActivity).getBottomSheet(
                                R.layout.dialog_export_bottom_sheet, null, allContent
                            ).show(
                                requireActivity().supportFragmentManager,
                                MainBottomSheetFragment.TAG
                            )
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setListGestures() {
        val itemTouch = RecyclerViewUtils.swipeToDelete(resources) { viewHolder ->

            // below line is to get the position
            // of the item at that position.
            val position = viewHolder.adapterPosition

            val deleteValue: TaskValue =
                mRecyclerListAdapter.getItem(position).entryValue ?: return@swipeToDelete

            // below line is to notify and delete in db.
            mRecyclerListAdapter.notifyItemRemoved(position)
            mTaskValuesModel.delete(deleteValue)

            // below line is to display our snackbar
            Snackbar.make(
                binding.dashboardRecyclerView,
                getString(R.string.item_deleted, getString(R.string.deleted_value)),
                Snackbar.LENGTH_LONG
            ).show()
        }
        itemTouch.attachToRecyclerView(binding.dashboardRecyclerView)
    }

    private fun noTaskSnack(view: View) {
        Snackbar.make(view, R.string.dashboard_no_task, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.task_manager_fragment_label) {
                findNavController().navigate(R.id.action_DashboardFragment_to_TaskManagerFragment)
            }.show()
    }

    private fun noEntryValueSnack(view: View) {
        Snackbar.make(view, R.string.dashboard_no_entry, Snackbar.LENGTH_LONG)
            .setAction(R.string.content_desc_insert_value_btn) {
                findNavController().navigate(R.id.action_DashboardFragment_to_TaskEntryFragment)
            }.show()
    }

    private fun updateUIItems(entries: Map<TaskEntity, TaskValue>) {

        constructGraph(entries, mAxisXSelection, -1, true)
        updateListViewAsync(entries)
    }

    private fun updateListViewAsync(entries: Map<TaskEntity, TaskValue>) {

        val dateValues = mutableListOf<DashboardListData>()
        for (entityKey in entries.keys) {

            if (mFilteredTaskIds.filteredTasks.value?.get(entityKey.id) == true) {
                val entityValue = entries[entityKey]
                dateValues.add(DashboardListData(false, null, entityValue, entityKey))
            }
        }

        mRecyclerListAdapter.updateItems(dateValues.sortedByDescending { it.entryValue?.time })
        mRecyclerListAdapter.notifyDataSetChanged()
    }

    private fun constructGraph(
        entries: Map<TaskEntity, TaskValue>,
        selection: AxisXTypes,
        paging: Int,
        shouldResetPage: Boolean
    ) {

        if (shouldResetPage) {
            mDashboardCalendar = Calendar.getInstance()
        }

        // Change the time axis values
        setTimeAxisLabels(mDashboardCalendar.clone() as Calendar, selection)

        mDashboardCalendar.add(
            Calendar.DAY_OF_YEAR, (when (selection) {
                AxisXTypes.Day -> 0
                AxisXTypes.Week -> 7
                AxisXTypes.Month -> 30
                AxisXTypes.ThreeMonths -> 90
                AxisXTypes.SixMonths -> 180
            } * paging)
        )

        if (selection == AxisXTypes.Day) {
            mDashboardCalendar.set(Calendar.HOUR_OF_DAY, 0)
        } else {
            mDashboardCalendar.set(
                Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            )
        }

        val endTime = mDashboardCalendar.timeInMillis

        var maxYAxis: Long = -1
        var taskId: TaskEntity? = null
        var entryList: MutableList<Entry> = mutableListOf()
        val graphLineDataSet: MutableList<ILineDataSet> = mutableListOf()
        for (entityKey in entries.keys) {

            if (mFilteredTaskIds.filteredTasks.value?.get(entityKey.id) == true) {
                if (entityKey.id != (taskId?.id ?: -1)) {

                    if (entryList.isNotEmpty() && taskId != null) {
                        val lineDataSet =
                            createDataSet(taskId.color, taskId.name, entryList.toList())

                        graphLineDataSet.add(lineDataSet)
                    }

                    entryList = mutableListOf()
                    taskId = entityKey
                }
                if (entries[entityKey] != null && endTime <= entries[entityKey]!!.time) {
                    maxYAxis = max(maxYAxis, entries[entityKey]!!.value)
                    entryList.add(
                        Entry(
                            AxisUtils.getTimeAxisValue(entries[entityKey]!!.time, selection),
                            entries[entityKey]!!.value.toFloat()
                        )
                    )
                }
            }
        }

        // Add last added value
        if (entryList.size > 0 && taskId != null) {
            graphLineDataSet.add(createDataSet(taskId.color, taskId.name, entryList))
        }

        val lineData = LineData(graphLineDataSet)
        lineData.setValueTextColor(Color.WHITE)
        lineData.setValueTextSize(9f)

        binding.chartView.clear()
        binding.chartView.data = lineData
        binding.chartView.axisLeft.axisMaximum = maxYAxis.toFloat() + 1.0F
        binding.chartView.animateX(500, Easing.Linear)
        binding.chartView.invalidate()

    }

    private fun createDataSet(color: Int, name: String, list: List<Entry>): LineDataSet {
        val lineDataSet = LineDataSet(list, name)
        lineDataSet.axisDependency = YAxis.AxisDependency.LEFT
        lineDataSet.color = color
        lineDataSet.valueTextColor = color
        lineDataSet.lineWidth = 1.5f
        lineDataSet.setDrawCircles(true)
        lineDataSet.setDrawValues(false)
        lineDataSet.setCircleColor(color)
        lineDataSet.fillAlpha = 65
        lineDataSet.fillColor = color
        lineDataSet.setDrawCircleHole(false)

        return lineDataSet
    }

    private fun setTimeAxisLabels(selectedCalendar: Calendar, selection: AxisXTypes) {

        mAxisXLabels = AxisUtils.getTimeAxisLabels(selectedCalendar, selection, requireContext())

        val xAxis = binding.chartView.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return mAxisXLabels[value.toInt()]
                    ?: mAxisXLabels.values.toList()[value.toInt() % mAxisXLabels.size]
            }
        }
        xAxis.axisMinimum = (mAxisXLabels.keys.minOrNull() ?: 0).toFloat()
        xAxis.axisMaximum = (mAxisXLabels.keys.maxOrNull() ?: 0).toFloat()
        xAxis.setLabelCount(mAxisXLabels.size, true)
        xAxis.granularity = AxisUtils.getGranularity(selection).toFloat()
    }

    private fun initChartView() {

        // no description text
        binding.chartView.description.isEnabled = false

        // enable touch gestures
        binding.chartView.setTouchEnabled(true)

        binding.chartView.dragDecelerationFrictionCoef = 0.9f

        binding.chartView.setNoDataText(getString(R.string.dashboard_no_chart_data))
        // enable scaling and dragging
        binding.chartView.isDragEnabled = true
        binding.chartView.setScaleEnabled(true)
        binding.chartView.setPinchZoom(true)
        binding.chartView.setDrawGridBackground(false)
        binding.chartView.isHighlightPerDragEnabled = true

        // set an alternative background color
        binding.chartView.setBackgroundColor(
            ResourcesCompat.getColor(
                resources, R.color.device_theme_color, null
            )
        )

        // get the legend (only possible after setting data)
        binding.chartView.legend.isEnabled = true

        val xAxis: XAxis = binding.chartView.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.textSize = 10f
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.textColor = ResourcesCompat.getColor(resources, R.color.chart_xaxis_labels, null)
        xAxis.setCenterAxisLabels(false)
        xAxis.granularity = 1f // one hour

        val leftAxis: YAxis = binding.chartView.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.textColor = ResourcesCompat.getColor(resources, R.color.chart_yaxis_labels, null)
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true
        leftAxis.axisMinimum = -0.2f
        leftAxis.granularity = 1f
//        leftAxis.axisMaximum = 170f
//        leftAxis.yOffset = -9f
//        leftAxis.textColor = Color.rgb(255, 192, 56)

        val l: Legend = binding.chartView.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.textColor = ResourcesCompat.getColor(resources, R.color.chart_legend_text, null)
        l.setDrawInside(false)
        l.form = LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f

        binding.chartView.axisRight.isEnabled = false
    }

}