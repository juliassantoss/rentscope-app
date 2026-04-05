package com.example.rentscope.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

fun mapToEntries(data: List<Pair<String, Float>>): Pair<List<Entry>, List<String>> {
    val entries = mutableListOf<Entry>()
    val labels = mutableListOf<String>()

    data.forEachIndexed { index, pair ->
        entries.add(Entry(index.toFloat(), pair.second))
        labels.add(pair.first)
    }

    return Pair(entries, labels)
}

@Composable
fun PriceHistoryChart(data: List<Pair<String, Float>>) {

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    600
                )
            }
        },
        update = { chart ->

            val (entries, labels) = mapToEntries(data)

            val dataSet = LineDataSet(entries, "€/m²").apply {
                setDrawCircles(true)
                setDrawValues(false)
                lineWidth = 2f
            }

            chart.data = LineData(dataSet)

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            chart.xAxis.granularity = 1f
            chart.xAxis.labelRotationAngle = -45f

            chart.axisRight.isEnabled = false

            chart.description = Description().apply {
                text = "Evolução da renda"
            }

            chart.invalidate()
        }
    )
}