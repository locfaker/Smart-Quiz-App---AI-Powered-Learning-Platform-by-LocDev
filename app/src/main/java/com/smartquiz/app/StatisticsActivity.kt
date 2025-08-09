package com.smartquiz.app

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.smartquiz.app.databinding.ActivityStatisticsBinding
import com.smartquiz.app.ui.viewmodel.StatisticsViewModel
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private lateinit var viewModel: StatisticsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        observeViewModel()
        setupCharts()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[StatisticsViewModel::class.java]
        viewModel.loadStatistics()
    }

    private fun observeViewModel() {
        viewModel.totalQuizzes.observe(this) { total ->
            binding.textTotalQuizzes.text = total.toString()
        }

        viewModel.averageScore.observe(this) { average ->
            binding.textAverageScore.text = "${average.toInt()}%"
        }

        viewModel.scoreDistribution.observe(this) { distribution ->
            setupPieChart(distribution)
        }

        viewModel.subjectPerformance.observe(this) { performance ->
            setupBarChart(performance)
        }

        viewModel.progressOverTime.observe(this) { progress ->
            setupLineChart(progress)
        }
    }

    private fun setupCharts() {
        // Configure charts appearance
        binding.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            setExtraOffsets(5f, 10f, 5f, 5f)
            dragDecelerationFrictionCoef = 0.95f
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleColor(Color.WHITE)
            setTransparentCircleAlpha(110)
            holeRadius = 58f
            transparentCircleRadius = 61f
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
        }

        binding.barChart.apply {
            description.isEnabled = false
            setMaxVisibleValueCount(60)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
            }
            
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            axisRight.isEnabled = false
            legend.isEnabled = false
        }

        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setDragEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
            }
            
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
            }
            
            axisRight.isEnabled = false
        }
    }

    private fun setupPieChart(distribution: Map<String, Int>) {
        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        distribution.forEach { (range, count) ->
            entries.add(PieEntry(count.toFloat(), range))
        }

        colors.addAll(listOf(
            Color.parseColor("#F44336"), // Red for low scores
            Color.parseColor("#FF9800"), // Orange for medium scores  
            Color.parseColor("#4CAF50"), // Green for high scores
            Color.parseColor("#2196F3")  // Blue for excellent scores
        ))

        val dataSet = PieDataSet(entries, "Phân bố điểm").apply {
            setDrawIcons(false)
            sliceSpace = 3f
            iconsOffset = MPPointF(0f, 40f)
            selectionShift = 5f
            setColors(colors)
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter())
            setValueTextSize(11f)
            setValueTextColor(Color.WHITE)
        }

        binding.pieChart.data = data
        binding.pieChart.invalidate()
    }

    private fun setupBarChart(performance: Map<String, Double>) {
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        performance.entries.forEachIndexed { index, (subject, score) ->
            entries.add(BarEntry(index.toFloat(), score.toFloat()))
            labels.add(subject)
        }

        val dataSet = BarDataSet(entries, "Điểm trung bình").apply {
            color = Color.parseColor("#667eea")
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        val data = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        binding.barChart.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate()
        }
    }

    private fun setupLineChart(progress: List<Pair<String, Double>>) {
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        progress.forEachIndexed { index, (date, score) ->
            entries.add(Entry(index.toFloat(), score.toFloat()))
            labels.add(date)
        }

        val dataSet = LineDataSet(entries, "Tiến độ").apply {
            color = Color.parseColor("#667eea")
            setCircleColor(Color.parseColor("#764ba2"))
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            valueTextSize = 10f
            setDrawFilled(true)
            fillColor = Color.parseColor("#667eea")
            fillAlpha = 50
        }

        val data = LineData(dataSet)

        binding.lineChart.apply {
            this.data = data
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate()
        }
    }
}
