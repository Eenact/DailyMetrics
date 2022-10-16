package com.softtechno.tasktracker.gui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

        init()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTaskValuesModel.allValues.observe(viewLifecycleOwner) { entries ->
            constructGraph(entries, mAxisXSelection)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun constructGraph(entries: Map<TaskEntity, TaskValue>, selection: AxisXTypes) {

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        when (selection) {
            AxisXTypes.Week -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            AxisXTypes.Month -> calendar.add(Calendar.MONTH, -1)
            AxisXTypes.Year -> calendar.add(Calendar.YEAR, -1)
            AxisXTypes.Day -> {}
        }

        val endTime = calendar.timeInMillis

        // Change the time axis values
        setTimeAxisValues(calendar, selection)

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
        binding.chartView.notifyDataSetChanged()

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

    private fun setTimeAxisValues(selectedCalendar: Calendar, selection: AxisXTypes) {

        mAxisXLabels = AxisUtils.getTimeAxisLabels(selectedCalendar, selection, requireContext())

        val xAxis = binding.chartView.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return mAxisXLabels[value.toInt()]
                    ?: mAxisXLabels.values.toList()[value.toInt() % mAxisXLabels.size]
            }
        }
        xAxis.axisMinimum = mAxisXLabels.keys.min().toFloat()
        xAxis.axisMaximum = mAxisXLabels.keys.max().toFloat()
        xAxis.setLabelCount(mAxisXLabels.size, true)
    }

    private fun init() {

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
        leftAxis.axisMinimum = -1f
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