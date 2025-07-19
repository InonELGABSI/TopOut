// NEW  LiveMap.kt  ────────────────────────────────────────────────────────────
package com.topout.kmp.map


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * Live‑tracking Google Map that keeps the camera centred on [location] and
 * provides simple “+ / –” zoom buttons. Optionally displays a route based on [trackPoints].
 */
@Composable
fun LiveMap(
    location: com.topout.kmp.models.LatLng?,
    trackPoints: List<com.topout.kmp.models.TrackPoint>? = null,
    modifier: Modifier = Modifier,
    initialZoom: Float = 17f
) {
    /* ── state ---------------------------------------------------------------- */
    val coroutineScope = rememberCoroutineScope()
    val isRouteMode = !trackPoints.isNullOrEmpty()

    // Wait for real location in live mode, use route points in route mode
    val target = when {
        isRouteMode -> trackPoints!!.mapNotNull { it.latLngOrNull() }.firstOrNull()?.toLatLng()
        else -> location?.toLatLng() // Only set target when we have real location
    }

    // Don't show map until we have a valid target location
    if (target == null && !isRouteMode) {
        // Show loading state while waiting for location
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Waiting for GPS...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(target!!, initialZoom)
    }

    // MarkerState MUST be remembered – prevents “state object during composition” warning
    val markerState = remember(target) { MarkerState(position = target!!) }

    /* ── follow the climber (live mode) --------------------------------------- */
    LaunchedEffect(location) {
        if (!isRouteMode && location != null) {
            val latLng = location.toLatLng()
            markerState.position = latLng // move marker
            cameraState.animate(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    /* ── show the whole route (route mode) ------------------------------------ */
    LaunchedEffect(trackPoints) {
        if (isRouteMode) {
            val points = trackPoints!!.mapNotNull { it.latLngOrNull()?.toLatLng() }
            if (points.size > 1) {
                val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.builder()
                points.forEach { boundsBuilder.include(it) }
                try {
                    val bounds = boundsBuilder.build()
                    cameraState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } catch (e: IllegalStateException) {
                    // Log error or handle, e.g. when no valid points
                }
            } else if (points.isNotEmpty()) {
                cameraState.move(CameraUpdateFactory.newLatLngZoom(points.first(), initialZoom))
            }
        }
    }

    /* ── UI ------------------------------------------------------------------- */
    Box(modifier = modifier) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            if (isRouteMode) {
                val points = trackPoints!!.mapNotNull { it.latLngOrNull()?.toLatLng() }
                if (points.isNotEmpty()) {
                    Polyline(points = points, color = MaterialTheme.colorScheme.primary, width = 10f)
                    points.firstOrNull()?.let { Marker(MarkerState(position = it), title = "Start") }
                    points.lastOrNull()?.let { Marker(MarkerState(position = it), title = "End") }
                }
            } else {
                Marker(
                    state = markerState,
                    title = "You",
                    snippet = "Live location"
                )
            }
        }

        /* floating “+ / –” buttons */
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ZoomButton("+") { coroutineScope.launch { cameraState.zoomBy(+1f) } }
            ZoomButton("–") { coroutineScope.launch { cameraState.zoomBy(-1f) } }
        }
    }
}

/* helper composables & extensions ------------------------------------------- */

@Composable
private fun ZoomButton(label: String, onClick: () -> Unit) =
    ElevatedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.small
    ) { Text(label) }

private fun com.topout.kmp.models.LatLng.toLatLng() =
    LatLng(latitude, longitude)

private fun com.topout.kmp.models.TrackPoint.latLngOrNull(): com.topout.kmp.models.LatLng? {
    val lat = latitude
    val lon = longitude
    return if (lat != null && lon != null) {
        com.topout.kmp.models.LatLng(lat, lon)
    } else {
        null
    }
}

/** Zooms the camera by [delta] steps (+ for in, – for out). Must be called inside a coroutine. */
private suspend fun CameraPositionState.zoomBy(delta: Float) =
    animate(CameraUpdateFactory.zoomBy(delta))
