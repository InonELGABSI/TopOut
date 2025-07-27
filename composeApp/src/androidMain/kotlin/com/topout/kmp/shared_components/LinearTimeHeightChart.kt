package com.topout.kmp.shared_components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import java.util.Locale

@Composable
fun TimeHeightChart(
    samples: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier,
    useHighContrast: Boolean = false,
    useDarkTheme: Boolean = false,
    showGrid: Boolean = false,
    showGradientFill: Boolean = true
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    // Select the appropriate theme with grid control
    val vicoTheme = when {
        useHighContrast -> rememberTopOutHighContrastVicoTheme(showGrid = showGrid)
        useDarkTheme -> rememberTopOutDarkVicoTheme(showGrid = showGrid)
        else -> rememberTopOutVicoTheme(showGrid = showGrid)
    }

    // Create a custom value formatter for the bottom axis
    val bottomAxisValueFormatter = remember(samples) {
        object : CartesianValueFormatter {
            override fun format(
                context: CartesianMeasuringContext,
                value: Double,
                verticalAxisPosition: Axis.Position.Vertical?,
            ): String {
                val index = value.toInt()
                return if (index >= 0 && index < samples.size) {
                    String.format(Locale.getDefault(), "%.1fs", samples[index].first)
                } else {
                    ""
                }
            }
        }
    }

    LaunchedEffect(samples) {
        if (samples.isNotEmpty()) {
            modelProducer.runTransaction {
                lineSeries {
                    series(
                        x = samples.mapIndexed { index, _ -> index.toFloat() },
                        y = samples.map { it.second }
                    )
                }
            }
        }
    }

    if (samples.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "No altitude data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    ProvideVicoTheme(vicoTheme) {
        val lineColor = MaterialTheme.colorScheme.primary

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = if (showGradientFill) {
                        LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                                areaFill = LineCartesianLayer.AreaFill.single(
                                    fill(
                                        ShaderProvider.verticalGradient(
                                            arrayOf(lineColor.copy(alpha = 0.4f), Color.Transparent)
                                        )
                                    )
                                )
                            )
                        )
                    } else {
                        LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill(lineColor))
                            )
                        )
                    }
                ),
                startAxis = VerticalAxis.rememberStart(
                    title = "Height (m)",
                    titleComponent = TextComponent(
                        textSizeSp = 12f
                    ),
                    guideline = null
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    title = "Time (s)",
                    titleComponent = TextComponent(
                        textSizeSp = 12f
                    ),
                    valueFormatter = bottomAxisValueFormatter,
                    guideline = null
                ),
            ),
            modelProducer = modelProducer,
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(8.dp)
        )
    }
}
