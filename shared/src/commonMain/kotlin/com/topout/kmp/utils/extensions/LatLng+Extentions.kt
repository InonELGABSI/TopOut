package com.topout.kmp.utils.extensions

import com.topout.kmp.models.LatLng

fun LatLng.toXY(): Pair<Double, Double> =
    ((longitude + 180) / 360.0) to ((90 - latitude) / 180.0)