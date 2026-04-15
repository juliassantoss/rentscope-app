package com.example.rentscope.ui.components

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import android.graphics.Color as AndroidColor

private const val BRAND_BLUE = -0xD0792A
private const val GRID_COLOR = -0x2f2f30

fun mapToEntries(data: List<Pair<String, Float>>): Pair<List<Entry>, List<String>> {
    val entries = mutableListOf<Entry>()
    val labels = mutableListOf<String>()

    data.forEachIndexed { index, pair ->
        entries.add(Entry(index.toFloat(), pair.second))
        labels.add(formatQuarterLabel(pair.first))
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
                    760
                )

                setTouchEnabled(true)
                setDragEnabled(true)
                setScaleEnabled(false)
                setPinchZoom(false)
                setNoDataText("Sem dados para apresentar")
                setExtraOffsets(12f, 16f, 12f, 8f)

                description.isEnabled = false

                legend.apply {
                    isEnabled = true
                    textSize = 12f
                    form = Legend.LegendForm.LINE
                    verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                    orientation = Legend.LegendOrientation.HORIZONTAL
                    setDrawInside(false)
                }

                axisRight.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textSize = 11f
                    labelRotationAngle = 0f
                    setAvoidFirstLastClipping(true)
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = GRID_COLOR
                    axisLineColor = AndroidColor.TRANSPARENT
                    textSize = 11f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return String.format(Locale("pt", "PT"), "%.1f", value)
                        }
                    }
                }
            }
        },
        update = { chart ->
            val sorted = data.sortedBy { parseQuarterIndex(it.first) }
            val (entries, labels) = mapToEntries(sorted)

            val dataSet = LineDataSet(entries, "Preço médio (€/m²)").apply {
                color = BRAND_BLUE
                lineWidth = 2.4f
                setDrawCircles(true)
                circleRadius = 3.2f
                setCircleColor(BRAND_BLUE)
                setDrawCircleHole(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.LINEAR
                setDrawFilled(false)
                highLightColor = BRAND_BLUE
            }

            chart.data = LineData(dataSet).apply {
                setHighlightEnabled(true)
            }

            chart.xAxis.valueFormatter = object : IndexAxisValueFormatter(labels) {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in labels.indices) labels[index] else ""
                }
            }

            val labelCount = min(6, max(2, labels.size))
            chart.xAxis.setLabelCount(labelCount, false)

            if (entries.isNotEmpty()) {
                val minY = entries.minOf { it.y }
                val maxY = entries.maxOf { it.y }
                val padding = max(0.3f, (maxY - minY) * 0.15f)

                chart.axisLeft.axisMinimum = floor(minY - padding)
                chart.axisLeft.axisMaximum = ceil(maxY + padding)
                chart.xAxis.axisMinimum = -0.3f
                chart.xAxis.axisMaximum = entries.lastIndex.toFloat() + 0.3f
            }

            chart.marker = PriceMarkerView(chart.context, labels)
            chart.animateX(450)
            chart.invalidate()
        }
    )
}

private class PriceMarkerView(
    context: android.content.Context,
    private val labels: List<String>
) : MarkerView(context, android.R.layout.simple_list_item_2) {

    private val titleView = findViewById<android.widget.TextView>(android.R.id.text1)
    private val valueView = findViewById<android.widget.TextView>(android.R.id.text2)

    override fun refreshContent(e: Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
        val index = e?.x?.toInt() ?: 0
        val label = labels.getOrNull(index).orEmpty()
        val value = e?.y ?: 0f

        titleView.text = label
        titleView.textSize = 13f
        titleView.setTextColor(AndroidColor.BLACK)

        valueView.text = String.format(Locale("pt", "PT"), "%.2f €/m²", value)
        valueView.textSize = 12f
        valueView.setTextColor(AndroidColor.DKGRAY)

        super.refreshContent(e, highlight)
    }

    override fun getOffset(): com.github.mikephil.charting.utils.MPPointF {
        return com.github.mikephil.charting.utils.MPPointF((-width / 2).toFloat(), -height.toFloat())
    }
}

private fun formatQuarterLabel(value: String): String {
    val quarter = Regex("""(\d)""").find(value)?.groupValues?.getOrNull(1) ?: return value
    val year = Regex("""(20\d{2})""").find(value)?.groupValues?.getOrNull(1) ?: return value
    return "T$quarter/${year.takeLast(2)}"
}

private fun parseQuarterIndex(value: String): Int {
    val quarterMatch = Regex("""(\d)""").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    val yearMatch = Regex("""(20\d{2})""").find(value)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    return (yearMatch * 10) + quarterMatch
}