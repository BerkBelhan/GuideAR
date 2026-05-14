package com.berkbelhan.indoornavigation.presentation.maps

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import com.berkbelhan.indoornavigation.presentation.components.ErrorState
import com.berkbelhan.indoornavigation.presentation.components.LoadingOverlay
import com.berkbelhan.indoornavigation.presentation.components.StatusChip
import com.berkbelhan.indoornavigation.presentation.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IndoorMapScreen(
    mapId: String,
    onOpenAr: () -> Unit,
    onBack: () -> Unit,
    viewModel: IndoorMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading && uiState.venue == null) {
            LoadingOverlay("Loading map…")
        } else if (uiState.error != null && uiState.venue == null) {
            ErrorState(message = uiState.error!!)
        } else {
            MapViewport(
                uiState = uiState,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top controls overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            MapTopBar(
                venueName = uiState.venue?.name ?: "Indoor Map",
                trackingState = uiState.trackingState,
                onBack = onBack
            )
        }

        // Floor selector
        uiState.venue?.let { venue ->
            if (venue.floors > 1) {
                FloorSelector(
                    floors = venue.floors,
                    selectedFloor = uiState.selectedFloor,
                    onFloorSelected = { viewModel.selectFloor(it) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .navigationBarsPadding()
                )
            }
        }

        // AR FAB
        FloatingActionButton(
            onClick = onOpenAr,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 32.dp)
                .navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.ViewInAr, contentDescription = null)
                Text("AR Navigation", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapTopBar(
    venueName: String,
    trackingState: TrackingState,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = venueName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            TrackingChip(trackingState = trackingState)
            Spacer(Modifier.width(8.dp))
        }
    }
}

@Composable
private fun TrackingChip(trackingState: TrackingState) {
    val (text, color) = when (trackingState) {
        TrackingState.TRACKING -> "Tracking" to TrackingGreen
        TrackingState.INITIALIZING -> "Initializing" to WarningAmber
        TrackingState.LOST -> "Lost" to ErrorRed
        TrackingState.ERROR -> "Error" to ErrorRed
    }
    StatusChip(text = text, color = color)
}

@Composable
private fun FloorSelector(
    floors: Int,
    selectedFloor: Int,
    onFloorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            repeat(floors) { floor ->
                val isSelected = floor == selectedFloor
                TextButton(
                    onClick = { onFloorSelected(floor) },
                    modifier = Modifier.size(44.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else Color.Transparent,
                        contentColor = if (isSelected)
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = floor.toString(),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * 2D map canvas viewport.
 * In a production build, replace with MapLibre GL or a custom tile renderer.
 * This Canvas implementation draws a placeholder grid with user position.
 */
@Composable
private fun MapViewport(
    uiState: IndoorMapUiState,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val outline = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Canvas(
        modifier = modifier
            .background(surfaceVariant)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offset = Offset(offset.x + pan.x, offset.y + pan.y)
                }
            }
    ) {
        withTransform({
            translate(size.width / 2 + offset.x, size.height / 2 + offset.y)
            scale(scale, scale)
        }) {
            drawMapGrid(outline)
            drawRoomOutlines(outline)
            uiState.currentPose?.let { pose ->
                drawUserPosition(
                    xMeters = pose.xMeters.toFloat(),
                    yMeters = pose.yMeters.toFloat(),
                    headingDeg = pose.headingDegrees,
                    color = primaryColor
                )
            }
            uiState.pois.filter { it.floor == uiState.selectedFloor }
                .forEach { poi ->
                    drawPoi(
                        x = poi.xMeters.toFloat() * 40f,
                        y = -poi.yMeters.toFloat() * 40f,
                        color = primaryColor.copy(alpha = 0.7f)
                    )
                }
        }
    }
}

private fun DrawScope.drawMapGrid(color: Color) {
    val step = 40f
    val halfW = size.width
    val halfH = size.height
    var x = -halfW
    while (x < halfW) {
        drawLine(color, Offset(x, -halfH), Offset(x, halfH), strokeWidth = 0.5f)
        x += step
    }
    var y = -halfH
    while (y < halfH) {
        drawLine(color, Offset(-halfW, y), Offset(halfW, y), strokeWidth = 0.5f)
        y += step
    }
}

private fun DrawScope.drawRoomOutlines(color: Color) {
    val rooms = listOf(
        Offset(-120f, -80f) to Pair(200f, 160f),
        Offset(100f, -80f) to Pair(150f, 70f),
        Offset(100f, 10f) to Pair(150f, 70f),
        Offset(-120f, 100f) to Pair(100f, 80f)
    )
    rooms.forEach { (origin, size) ->
        drawRect(
            color = color,
            topLeft = origin,
            size = androidx.compose.ui.geometry.Size(size.first, size.second),
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth = 2f)
        )
    }
}

private fun DrawScope.drawUserPosition(xMeters: Float, yMeters: Float, headingDeg: Float, color: Color) {
    val px = xMeters * 40f
    val py = -yMeters * 40f
    // Accuracy circle
    drawCircle(color.copy(alpha = 0.1f), radius = 30f, center = Offset(px, py))
    // Position dot
    drawCircle(color, radius = 10f, center = Offset(px, py))
    // Heading arrow
    val rad = Math.toRadians(headingDeg.toDouble())
    val arrowEnd = Offset(
        px + (sin(rad) * 20f).toFloat(),
        py - (cos(rad) * 20f).toFloat()
    )
    drawLine(
        color = Color.White,
        start = Offset(px, py),
        end = arrowEnd,
        strokeWidth = 3f
    )
}

private fun DrawScope.drawPoi(x: Float, y: Float, color: Color) {
    drawCircle(color, radius = 6f, center = Offset(x, y))
    drawCircle(Color.White, radius = 3f, center = Offset(x, y))
}
