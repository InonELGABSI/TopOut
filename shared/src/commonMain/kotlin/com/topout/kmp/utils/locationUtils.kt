package com.topout.kmp.utils

import kotlin.math.*

/**
 * Converts latitude/longitude to normalized [x, y] in [0,1] for OSM/Web Mercator.
 * Used to position markers and scroll the map in MapCompose.
 */
expect fun latLonToXY(lat: Double, lon: Double): Pair<Double, Double>