package com.mysticraccoon.runningappyt.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.mysticraccoon.runningappyt.R
import com.mysticraccoon.runningappyt.core.utils.TrackingUtility
import com.mysticraccoon.runningappyt.databinding.FragmentStatisticsBinding
import com.mysticraccoon.runningappyt.ui.viewModels.MainViewModel
import com.mysticraccoon.runningappyt.ui.viewModels.StatisticsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment: Fragment() {

    // Inner mutable binding
    private var _binding: FragmentStatisticsBinding? = null
    // Binding used by subclasses
    val binding: FragmentStatisticsBinding
        get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    private fun subscribeToObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner) {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        }
        viewModel.totalDistance.observe(viewLifecycleOwner){
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"
                binding.tvTotalDistance.text = totalDistanceString
            }
        }
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner){
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                binding.tvAverageSpeed.text = avgSpeedString
            }
        }
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                val totalCalories = "${it}kcal"
                binding.tvTotalCalories.text = totalCalories
            }
        }

//        viewModel.runsSortedByDate.observe(viewLifecycleOwner){
//            it?.let {
//                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
//                val bardataSet = BarDataSet(allAvgSpeeds, "Avg Speed Over Time").apply {
//                    valueTextColor = Color.WHITE
//                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
//                }
//                binding.lineChart.data = BarData(bardataSet)
//                binding.lineChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
//                binding.lineChart.invalidate()
//            }
//        }
    }


    private fun setupBarChart() {
        binding.lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.lineChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.lineChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.lineChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }
    }



}