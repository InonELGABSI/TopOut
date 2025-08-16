package com.topout.kmp.map

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.topout.kmp.shared_components.rememberTopContentSpacingDp
import kotlinx.coroutines.launch

@Composable
fun LiveMap(
    location: com.topout.kmp.models.LatLng?,
    trackPoints: List<com.topout.kmp.models.TrackPoint>? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    initialZoom: Float = 17f,
    showLocationFocus: Boolean = false,
    showTrackFocus: Boolean = false,
    useTopContentSpacing: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val isRouteMode = !trackPoints.isNullOrEmpty() && location == null

    val topContentSpacing = if (useTopContentSpacing) rememberTopContentSpacingDp() else 0.dp
    val density = LocalDensity.current
    val topPaddingPx = with(density) { topContentSpacing.toPx().toInt() }
    val sidePaddingPx = 50 // Side padding

    val target = when {
        isRouteMode -> trackPoints.firstNotNullOfOrNull { it.latLngOrNull() }?.toLatLng()
        else -> location?.toLatLng()
    }

    if (target == null && !isRouteMode) {
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

    val markerState = remember(target) { MarkerState(position = target!!) }

    LaunchedEffect(location) {
        if (!isRouteMode && location != null) {
            val latLng = location.toLatLng()
            markerState.position = latLng

            if (useTopContentSpacing && topPaddingPx > 0) {
                val offset = 0.001
                val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                    .include(com.google.android.gms.maps.model.LatLng(
                        latLng.latitude - offset,
                        latLng.longitude - offset
                    ))
                    .include(com.google.android.gms.maps.model.LatLng(
                        latLng.latitude + offset,
                        latLng.longitude + offset
                    ))
                    .build()

                val latSpan = bounds.northeast.latitude - bounds.southwest.latitude
                val topPaddingRatio = topPaddingPx.toFloat() / 500f
                val extraLatTop = latSpan * topPaddingRatio

                val adjustedBounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                    .include(com.google.android.gms.maps.model.LatLng(
                        bounds.southwest.latitude,
                        bounds.southwest.longitude
                    ))
                    .include(com.google.android.gms.maps.model.LatLng(
                        bounds.northeast.latitude + extraLatTop,
                        bounds.northeast.longitude
                    ))
                    .build()

                cameraState.animate(
                    CameraUpdateFactory.newLatLngBounds(adjustedBounds, sidePaddingPx)
                )
            } else {
                cameraState.animate(CameraUpdateFactory.newLatLng(latLng))
            }
        }
    }

    LaunchedEffect(trackPoints) {
        if (isRouteMode) {
            val points = trackPoints!!.mapNotNull { it.latLngOrNull()?.toLatLng() }
            if (points.size > 1) {
                val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.builder()
                points.forEach { boundsBuilder.include(it) }
                try {
                    val bounds = boundsBuilder.build()
                    if (useTopContentSpacing && topPaddingPx > 0) {
                        val latSpan = bounds.northeast.latitude - bounds.southwest.latitude
                        val lngSpan = bounds.northeast.longitude - bounds.southwest.longitude

                        val topPaddingRatio = topPaddingPx.toFloat() / 500f
                        val extraLatTop = latSpan * topPaddingRatio

                        val adjustedBounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                            .include(com.google.android.gms.maps.model.LatLng(
                                bounds.southwest.latitude,
                                bounds.southwest.longitude
                            ))
                            .include(com.google.android.gms.maps.model.LatLng(
                                bounds.northeast.latitude + extraLatTop,
                                bounds.northeast.longitude
                            ))
                            .build()
                        cameraState.move(
                            CameraUpdateFactory.newLatLngBounds(adjustedBounds, sidePaddingPx)
                        )
                    } else {
                        cameraState.move(
                            CameraUpdateFactory.newLatLngBounds(bounds, sidePaddingPx)
                        )
                    }
                } catch (e: IllegalStateException) {
                }
            } else if (points.isNotEmpty()) {
                cameraState.move(CameraUpdateFactory.newLatLngZoom(points.first(), initialZoom))
            }
        }
    }

    Box(modifier = modifier) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            if (!trackPoints.isNullOrEmpty()) {
                val points = trackPoints.mapNotNull { it.latLngOrNull()?.toLatLng() }
                if (points.isNotEmpty()) {
                    Polyline(points = points, color = MaterialTheme.colorScheme.primary, width = 25f)
                    if (isRouteMode) {
                        points.firstOrNull()?.let { Marker(MarkerState(position = it), title = "Start") }
                        points.lastOrNull()?.let { Marker(MarkerState(position = it), title = "End") }
                    }
                }
            }

            if (!isRouteMode && location != null) {
                Marker(
                    state = markerState,
                    title = "You",
                    snippet = "Live location"
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Focus on current location button
            if (showLocationFocus && location != null) {
                FocusButton("📍") {
                    coroutineScope.launch {
                        val latLng = location.toLatLng()
                        if (useTopContentSpacing && topPaddingPx > 0) {
                            val offset = 0.001
                            val bounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                                .include(com.google.android.gms.maps.model.LatLng(
                                    latLng.latitude - offset,
                                    latLng.longitude - offset
                                ))
                                .include(com.google.android.gms.maps.model.LatLng(
                                    latLng.latitude + offset,
                                    latLng.longitude + offset
                                ))
                                .build()

                            val latSpan = bounds.northeast.latitude - bounds.southwest.latitude
                            val topPaddingRatio = topPaddingPx.toFloat() / 500f
                            val extraLatTop = latSpan * topPaddingRatio

                            val adjustedBounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                                .include(com.google.android.gms.maps.model.LatLng(
                                    bounds.southwest.latitude,
                                    bounds.southwest.longitude
                                ))
                                .include(com.google.android.gms.maps.model.LatLng(
                                    bounds.northeast.latitude + extraLatTop,
                                    bounds.northeast.longitude
                                ))
                                .build()

                            cameraState.animate(
                                CameraUpdateFactory.newLatLngBounds(adjustedBounds, sidePaddingPx)
                            )
                        } else {
                            cameraState.animate(CameraUpdateFactory.newLatLngZoom(latLng, initialZoom))
                        }
                    }
                }
            }

            if (showTrackFocus && !trackPoints.isNullOrEmpty()) {
                FocusButton("🗺️") {
                    coroutineScope.launch {
                        val points = trackPoints.mapNotNull { it.latLngOrNull()?.toLatLng() }
                        if (points.size > 1) {
                            val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.builder()
                            points.forEach { boundsBuilder.include(it) }
                            try {
                                val bounds = boundsBuilder.build()
                                if (useTopContentSpacing && topPaddingPx > 0) {
                                    val latSpan = bounds.northeast.latitude - bounds.southwest.latitude
                                    val topPaddingRatio = topPaddingPx.toFloat() / 500f
                                    val extraLatTop = latSpan * topPaddingRatio

                                    val adjustedBounds = com.google.android.gms.maps.model.LatLngBounds.builder()
                                        .include(com.google.android.gms.maps.model.LatLng(
                                            bounds.southwest.latitude,
                                            bounds.southwest.longitude
                                        ))
                                        .include(com.google.android.gms.maps.model.LatLng(
                                            bounds.northeast.latitude + extraLatTop,
                                            bounds.northeast.longitude
                                        ))
                                        .build()

                                    cameraState.animate(
                                        CameraUpdateFactory.newLatLngBounds(adjustedBounds, sidePaddingPx)
                                    )
                                } else {
                                    cameraState.animate(
                                        CameraUpdateFactory.newLatLngBounds(bounds, sidePaddingPx)
                                    )
                                }
                            } catch (e: IllegalStateException) {
                            }
                        }
                    }
                }
            }

            ZoomButton("+") { coroutineScope.launch { cameraState.zoomBy(+1f) } }
            ZoomButton("–") { coroutineScope.launch { cameraState.zoomBy(-1f) } }
        }
    }
}


@Composable
private fun ZoomButton(label: String, onClick: () -> Unit) =
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
    }

@Composable
private fun FocusButton(icon: String, onClick: () -> Unit) =
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = when (icon) {
                "📍" -> Icons.Default.GpsFixed
                "🗺️" -> Icons.Default.GpsFixed
                else -> Icons.Default.GpsFixed
            },
            contentDescription = when (icon) {
                "📍" -> "Focus on my location"
                "🗺️" -> "Focus on route"
                else -> "Focus"
            },
            modifier = Modifier.size(24.dp)
        )
    }

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

private suspend fun CameraPositionState.zoomBy(delta: Float) =
    animate(CameraUpdateFactory.zoomBy(delta))
