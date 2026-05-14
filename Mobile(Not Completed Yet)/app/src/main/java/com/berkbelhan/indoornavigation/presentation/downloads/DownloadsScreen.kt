package com.berkbelhan.indoornavigation.presentation.downloads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.berkbelhan.indoornavigation.domain.model.DownloadState
import com.berkbelhan.indoornavigation.domain.model.MapBundleDownload
import com.berkbelhan.indoornavigation.presentation.components.EmptyState
import com.berkbelhan.indoornavigation.presentation.theme.TrackingGreen
import com.berkbelhan.indoornavigation.presentation.theme.WarningAmber
import com.berkbelhan.indoornavigation.presentation.theme.ErrorRed

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            DownloadsTopBar(
                totalCacheBytes = uiState.totalCacheBytes,
                onBack = onBack,
                onClearAll = { viewModel.clearAllCaches() }
            )
        }
    ) { padding ->
        if (uiState.downloads.isEmpty()) {
            EmptyState(
                emoji = "📥",
                title = "No downloads",
                subtitle = "Downloaded map bundles will appear here.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.downloads, key = { it.mapId }) { download ->
                    DownloadItem(
                        download = download,
                        onPause = { viewModel.pause(download.mapId) },
                        onResume = { viewModel.resume(download.mapId) },
                        onDelete = { viewModel.delete(download.mapId) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadsTopBar(
    totalCacheBytes: Long,
    onBack: () -> Unit,
    onClearAll: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text("Map Downloads", style = MaterialTheme.typography.titleLarge)
                if (totalCacheBytes > 0) {
                    Text(
                        text = "Storage: ${formatBytes(totalCacheBytes)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (totalCacheBytes > 0) {
                TextButton(onClick = onClearAll) {
                    Text("Clear all")
                }
            }
        }
    )
}

@Composable
private fun DownloadItem(
    download: MapBundleDownload,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when (download.state) {
                            DownloadState.COMPLETED -> Icons.Filled.CheckCircle
                            DownloadState.DOWNLOADING -> Icons.Filled.CloudDownload
                            DownloadState.PAUSED -> Icons.Filled.Pause
                            DownloadState.FAILED -> Icons.Filled.Error
                            DownloadState.QUEUED -> Icons.Filled.Schedule
                        },
                        contentDescription = null,
                        tint = when (download.state) {
                            DownloadState.COMPLETED -> TrackingGreen
                            DownloadState.DOWNLOADING -> MaterialTheme.colorScheme.primary
                            DownloadState.PAUSED -> WarningAmber
                            DownloadState.FAILED -> ErrorRed
                            DownloadState.QUEUED -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Column {
                        Text(
                            text = download.mapId,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v${download.version} · ${formatBytes(download.bytesTotal)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                // Action buttons
                Row {
                    when (download.state) {
                        DownloadState.DOWNLOADING -> {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Filled.Pause, contentDescription = "Pause")
                            }
                        }
                        DownloadState.PAUSED, DownloadState.FAILED -> {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Resume")
                            }
                        }
                        else -> {}
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Progress bar for active downloads
            if (download.state == DownloadState.DOWNLOADING || download.state == DownloadState.QUEUED) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { download.progressPercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${download.progressPercent}% · ${formatBytes(download.bytesDownloaded)} / ${formatBytes(download.bytesTotal)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
    else -> "${String.format("%.1f", bytes / (1024.0 * 1024 * 1024))} GB"
}
