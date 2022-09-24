package com.softtechno.tasktracker.gui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.softtechno.tasktracker.databinding.FragmentDashboardBinding
import com.softtechno.tasktracker.viewmodels.TaskValueViewModel
import com.softtechno.tasktracker.viewmodels.TaskViewViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DashboardFragment : Fragment() {

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

            val graphLineDataSet: MutableList<ILineDataSet> = mutableListOf()
            entries.keys.forEach { entityKey ->
                val entryList: List<Entry>? =
                    entries[entityKey]?.map { Entry(it.time.toFloat(), it.value.toFloat()) }
                if (entryList != null) {
                    val lineDataSet = LineDataSet(entryList, entityKey.name)
                    lineDataSet.axisDependency = YAxis.AxisDependency.LEFT
                    lineDataSet.color = entityKey.color
                    lineDataSet.valueTextColor = entityKey.color
                    lineDataSet.lineWidth = 1.5f
                    lineDataSet.setDrawCircles(false)
                    lineDataSet.setDrawValues(false)
                    lineDataSet.fillAlpha = 65
                    lineDataSet.fillColor = entityKey.color
                    lineDataSet.setDrawCircleHole(false)

                    graphLineDataSet.add(lineDataSet)
                }
            }

            val lineData = LineData(graphLineDataSet)
            lineData.setValueTextColor(Color.WHITE)
            lineData.setValueTextSize(9f)

            binding.chartView.data = lineData
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun init() {

        // no description text
        binding.chartView.description.isEnabled = false

        // enable touch gestures
        binding.chartView.setTouchEnabled(true)

        binding.chartView.dragDecelerationFrictionCoef = 0.9f

        // enable scaling and dragging
        binding.chartView.isDragEnabled = true
        binding.chartView.setScaleEnabled(true)
        binding.chartView.setDrawGridBackground(false)
        binding.chartView.isHighlightPerDragEnabled = true

        // set an alternative background color
        binding.chartView.setBackgroundColor(Color.WHITE)
        binding.chartView.setViewPortOffsets(0f, 0f, 0f, 0f)

        // get the legend (only possible after setting data)
        binding.chartView.legend.isEnabled = false

        val xAxis: XAxis = binding.chartView.xAxis
        xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
        xAxis.textSize = 10f
        xAxis.textColor = Color.WHITE
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.textColor = Color.rgb(255, 192, 56)
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f // one hour

        xAxis.valueFormatter = object : ValueFormatter() {
            private val mFormat: SimpleDateFormat = SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH)
            override fun getFormattedValue(value: Float): String {
                val millis: Long = TimeUnit.HOURS.toMillis(value.toLong())
                return mFormat.format(Date(millis))
            }
        }

        val leftAxis: YAxis = binding.chartView.axisLeft
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        leftAxis.textColor = ColorTemplate.getHoloBlue()
        leftAxis.setDrawGridLines(true)
        leftAxis.isGranularityEnabled = true
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 170f
        leftAxis.yOffset = -9f
        leftAxis.textColor = Color.rgb(255, 192, 56)

        binding.chartView.axisRight.isEnabled = false
    }

}