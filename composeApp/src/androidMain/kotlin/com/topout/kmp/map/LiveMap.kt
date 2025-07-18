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
 * provides simple “+ / –” zoom buttons.
 */
@Composable
fun LiveMap(
    location: com.topout.kmp.models.LatLng?,
    modifier: Modifier = Modifier,
    initialZoom: Float = 17f
) {
    /* ── state ---------------------------------------------------------------- */
    val coroutineScope = rememberCoroutineScope()

    val target = location?.toLatLng() ?: LatLng(32.0853, 34.7818) // Tel‑Aviv fallback

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(target, initialZoom)
    }

    // MarkerState MUST be remembered – prevents “state object during composition” warning
    val markerState = remember { MarkerState(position = target) }

    /* ── follow the climber --------------------------------------------------- */
    LaunchedEffect(location) {
        location?.let {
            markerState.position = it.toLatLng()                       // move marker
            cameraState.animate(CameraUpdateFactory.newLatLng(it.toLatLng()))
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
            Marker(
                state = markerState,
                title = "You",
                snippet = "Live location"
            )
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

/** Zooms the camera by [delta] steps (+ for in, – for out). Must be called inside a coroutine. */
private suspend fun CameraPositionState.zoomBy(delta: Float) =
    animate(CameraUpdateFactory.zoomBy(delta))
