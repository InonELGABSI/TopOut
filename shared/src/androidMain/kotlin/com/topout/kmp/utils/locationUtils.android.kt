package com.topout.kmp.utils

import kotlin.math.*

/**
 * Converts latitude/longitude to normalized [x, y] in [0,1] for OSM/Web Mercator.
 * Used to position markers and scroll the map in MapCompose.
 */
actual fun latLonToXY(lat: Double, lon: Double): Pair<Double, Double> {
    val x = (lon + 180.0) / 360.0
    val latRad = Math.toRadians(lat)
    val y = (1 - ln(tan(latRad) + 1 / cos(latRad)) / PI) / 2
    return x to y
}