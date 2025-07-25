package com.topout.kmp.map

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

/**
 * Live‑tracking Google Map that keeps the camera centred on [location] and
 * provides simple "+ / –" zoom buttons. Optionally displays a route based on [trackPoints].
 */
@Composable
fun LiveMap(
    location: com.topout.kmp.models.LatLng?,
    trackPoints: List<com.topout.kmp.models.TrackPoint>? = null,
    modifier: Modifier = Modifier,
    initialZoom: Float = 17f,
    showLocationFocus: Boolean = false,
    showTrackFocus: Boolean = false,
    useTopContentSpacing: Boolean = false
) {
    /* ── state ---------------------------------------------------------------- */
    val coroutineScope = rememberCoroutineScope()
    val isRouteMode = !trackPoints.isNullOrEmpty() && location == null

    // Get top content spacing and convert to pixels for map padding (optional)
    val topContentSpacing = if (useTopContentSpacing) rememberTopContentSpacingDp() else 0.dp
    val density = LocalDensity.current
    val topPaddingPx = with(density) { topContentSpacing.toPx().toInt() }
    val bottomPaddingPx = 100 // Keep some bottom padding for controls
    val sidePaddingPx = 50 // Side padding

    // Calculate effective padding - use top spacing for uniform padding but ensure bottom stays reasonable
    val effectivePadding = if (useTopContentSpacing && topPaddingPx > sidePaddingPx) {
        // When top spacing is significant, use it but cap the effective bottom padding
        maxOf(topPaddingPx, bottomPaddingPx)
    } else {
        // Use standard padding when top spacing is not significant
        maxOf(sidePaddingPx, bottomPaddingPx)
    }

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

    // MarkerState MUST be remembered – prevents "state object during composition" warning
    val markerState = remember(target) { MarkerState(position = target!!) }

    /* ── follow the climber (live mode) --------------------------------------- */
    LaunchedEffect(location) {
        if (!isRouteMode && location != null) {
            val latLng = location.toLatLng()
            markerState.position = latLng // move marker

            if (useTopContentSpacing && topPaddingPx > 0) {
                // Use the same top spacing logic as the focus button for live following
                val offset = 0.001 // Small offset to create bounds around the point
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

                // Apply the same top-only padding logic as in route mode
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
                // Standard behavior - just center on location
                cameraState.animate(CameraUpdateFactory.newLatLng(latLng))
            }
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
                    if (useTopContentSpacing && topPaddingPx > 0) {
                        // Calculate the bounds with additional top padding by expanding the north bound
                        val latSpan = bounds.northeast.latitude - bounds.southwest.latitude
                        val lngSpan = bounds.northeast.longitude - bounds.southwest.longitude

                        // Calculate how much extra latitude we need to add based on the screen height and top padding
                        // This is an approximation - adjust the multiplier as needed
                        val topPaddingRatio = topPaddingPx.toFloat() / 500f // Assuming 500dp map height
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
                        // Standard uniform padding
                        cameraState.move(
                            CameraUpdateFactory.newLatLngBounds(bounds, sidePaddingPx)
                        )
                    }
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
            // Show track points if available (both in live and route mode)
            if (!trackPoints.isNullOrEmpty()) {
                val points = trackPoints.mapNotNull { it.latLngOrNull()?.toLatLng() }
                if (points.isNotEmpty()) {
                    Polyline(points = points, color = MaterialTheme.colorScheme.primary, width = 10f)
                    if (isRouteMode) {
                        points.firstOrNull()?.let { Marker(MarkerState(position = it), title = "Start") }
                        points.lastOrNull()?.let { Marker(MarkerState(position = it), title = "End") }
                    }
                }
            }

            // Show current location marker in live mode
            if (!isRouteMode && location != null) {
                Marker(
                    state = markerState,
                    title = "You",
                    snippet = "Live location"
                )
            }
        }

        /* floating controls */
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
                            // Create a small bounds around the location point to account for top spacing
                            val offset = 0.001 // Small offset to create bounds around the point
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

                            // Apply the same top-only padding logic as in route mode
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
                            // Standard behavior - just center on location
                            cameraState.animate(CameraUpdateFactory.newLatLngZoom(latLng, initialZoom))
                        }
                    }
                }
            }

            // Focus on track button
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
                                    // Apply the same top-only padding logic as in route mode
                                    val latSpan = bounds.northeast.latitude - bounds.southwest.latitude
                                    val topPaddingRatio = topPaddingPx.toFloat() / 500f // Assuming 500dp map height
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
                                // Log error or handle, e.g. when no valid points
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

/* helper composables & extensions ------------------------------------------- */

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

/** Zooms the camera by [delta] steps (+ for in, – for out). Must be called inside a coroutine. */
private suspend fun CameraPositionState.zoomBy(delta: Float) =
    animate(CameraUpdateFactory.zoomBy(delta))
