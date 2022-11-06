package com.softtechno.tasktracker.gui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
import com.github.mikephil.charting.utils.ColorTemplate
import com.softtechno.tasktracker.R
import com.softtechno.tasktracker.databinding.FragmentDashboardBinding
import com.softtechno.tasktracker.db.TaskEntity
import com.softtechno.tasktracker.db.TaskValue
import com.softtechno.tasktracker.enums.AxisXTypes
import com.softtechno.tasktracker.utils.AxisUtils
import com.softtechno.tasktracker.viewmodels.TaskValueViewModel
import com.softtechno.tasktracker.viewmodels.TaskViewViewModelFactory
import java.util.*
import kotlin.math.max


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DashboardFragment : Fragment() {

    private var mAxisXLabels: Map<Int, String> = mapOf()
    private var mAxisXSelection: AxisXTypes = AxisXTypes.Week
    private var mDashboardCalendar: Calendar = Calendar.getInstance()

    private var _binding: FragmentDashboardBinding? = null
    private val mTaskValuesModel: TaskValueViewModel by activityViewModels {
        TaskViewViewModelFactory(requireContext())
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        initChartView()

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

        binding.buttonInsertEntry.setOnClickListener {
//            val taskEntryDialogFragment = TaskEntryDialogFragment()
//            if (isLargeDisplay) {
//                taskEntryDialogFragment.show(supportFragmentManager, "TaskEntryDialog")
//            }
            findNavController().navigate(R.id.action_DashboardFragment_to_TaskEntryFragment)
            activity?.actionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
            (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)
        }

        binding.leftArrowButton.setOnClickListener {
            mTaskValuesModel.allValues.value?.let { constructGraph(it, mAxisXSelection, -1, false) }
        }
        binding.rightArrowButton.setOnClickListener {
            mTaskValuesModel.allValues.value?.let { constructGraph(it, mAxisXSelection, 1, false) }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTaskValuesModel.allValues.observe(viewLifecycleOwner) { entries ->
            constructGraph(entries, mAxisXSelection, -1, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun constructGraph(
        entries: Map<TaskEntity, TaskValue>,
        selection: AxisXTypes,
        paging: Int,
        shouldResetPage: Boolean
    ) {

        if (entries.isEmpty()) {
            return
        }

        if (shouldResetPage) {
            mDashboardCalendar = Calendar.getInstance()
        }

        // Change the time axis values
        setTimeAxisLabels(mDashboardCalendar.clone() as Calendar, selection)

        mDashboardCalendar.add(
            Calendar.DAY_OF_YEAR,
            (when (selection) {
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
                Calendar.HOUR_OF_DAY,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            )
        }

        val endTime = mDashboardCalendar.timeInMillis

        var maxYAxis: Long = -1
        var taskId: TaskEntity? = null
        var entryList: MutableList<Entry> = mutableListOf()
        val graphLineDataSet: MutableList<ILineDataSet> = mutableListOf()
        for (entityKey in entries.keys) {

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

        // Add last added value
        if (entryList.size > 0 && taskId != null) {
            graphLineDataSet.add(createDataSet(taskId.color, taskId.name, entryList))
        }

        val lineData = LineData(graphLineDataSet)
        lineData.setValueTextColor(Color.WHITE)
        lineData.setValueTextSize(9f)

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

        // enable scaling and dragging
        binding.chartView.isDragEnabled = true
        binding.chartView.setScaleEnabled(false)
        binding.chartView.setDrawGridBackground(false)
        binding.chartView.isHighlightPerDragEnabled = true

        // set an alternative background color
        binding.chartView.setBackgroundColor(Color.WHITE)

        // get the legend (only possible after setting data)
        binding.chartView.legend.isEnabled = true

        val xAxis: XAxis = binding.chartView.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
//        xAxis.textSize = 10f
        xAxis.textColor = Color.WHITE
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.textColor = Color.rgb(255, 192, 56)
        xAxis.setCenterAxisLabels(false)
        xAxis.granularity = 1f // one hour

        val leftAxis: YAxis = binding.chartView.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        leftAxis.textColor = ColorTemplate.getHoloBlue()
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
        l.setDrawInside(false)
        l.form = LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f

        binding.chartView.axisRight.isEnabled = false
    }

}