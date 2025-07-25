package com.topout.kmp.shared_components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun TimeHeightChart(
    samples: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier
) {
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(samples) {
        producer.runTransaction {
            lineSeries {
                series(
                    x = samples.map { it.first },
                    y = samples.map { it.second }
                )
            }
        }
    }

    val lineLayer = rememberLineCartesianLayer()
    val chart = rememberCartesianChart(lineLayer)

    CartesianChartHost(
        chart = chart,
        modelProducer = producer,
        modifier = modifier
    )
}
