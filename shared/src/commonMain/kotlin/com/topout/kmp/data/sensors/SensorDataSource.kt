// commonMain/com/topout/kmp/data/sensors/SensorDataSource.kt
package com.topout.kmp.data.sensors

import com.topout.kmp.models.sensor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/** Factory + lifecycle wrapper around the three sensor streams. */
expect class SensorDataSource {
    val accelFlow: Flow<AccelerationData>
    val baroFlow : Flow<AltitudeData>
    val locFlow  : Flow<LocationData>

    fun start(scope: CoroutineScope)
    fun stop()
}
