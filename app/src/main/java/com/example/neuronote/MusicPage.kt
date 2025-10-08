package com.example.neuronote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Model used by the playlist row UI (kept for minimal changes). */
data class LocalTrack(
    val title: String,
    val artist: String,
    val resId: Int,
    val durationText: String? = null
)

@Composable
fun MusicPage(
    darkColor: Color,
    lightColor: Color,
    textColor: Color
) {
    val isDark by AppThemeManager.isDarkTheme

    // Light page in light mode (as per your screenshot), transparent in dark (parent sets bg)
    val pageBg = if (isDark) Color.Transparent else Color.White
    val cardColor = lightColor

    // Observe global player state
    val isPlaying by MusicPlayerManager.isPlaying.collectAsState()
    val currentIndex by MusicPlayerManager.currentIndex.collectAsState()
    val tracks = remember { MusicPlayerManager.tracks }

    // Timer to show position/duration
    var pos by remember { mutableLongStateOf(0L) }
    var dur by remember { mutableLongStateOf(0L) }
    LaunchedEffect(isPlaying, currentIndex) {
        while (true) {
            pos = MusicPlayerManager.currentPosition()
            dur = MusicPlayerManager.duration()
            delay(250)
        }
    }

    // Map manager Track -> LocalTrack for the existing row UI
    val localList = remember(tracks) {
        tracks.map { LocalTrack(it.title, it.subtitle, it.resId) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Music",
            color = textColor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        if (localList.isNotEmpty()) {
            val now = localList[currentIndex]
            Surface(
                color = cardColor,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = if (isDark) 2.dp else 0.dp,
                shadowElevation = if (isDark) 1.dp else 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        now.title,
                        color = textColor,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        now.artist,
                        color = textColor.copy(alpha = if (isDark) 0.8f else 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { MusicPlayerManager.prev() }) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = textColor)
                        }
                        Button(
                            onClick = {
                                if (isPlaying) MusicPlayerManager.pause()
                                else MusicPlayerManager.play(currentIndex)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = darkColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (isPlaying) "Pause" else "Play")
                        }
                        IconButton(onClick = { MusicPlayerManager.next() }) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = textColor)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatMs(pos), color = textColor.copy(alpha = 0.9f))
                        Text(formatMs(dur), color = textColor.copy(alpha = 0.9f))
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No offline songs yet", color = textColor, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add mp3 files to res/raw and register them in MusicPlayerManager.init().",
                        color = textColor.copy(alpha = if (isDark) 0.8f else 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Text(
            text = "My Offline Playlist",
            color = textColor.copy(alpha = if (isDark) 0.95f else 0.9f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            localList.forEachIndexed { index, track ->
                val selected = index == currentIndex && isPlaying
                PlaylistRow(
                    track = track,
                    selected = selected,
                    isDark = isDark,
                    lightColor = lightColor,
                    textColor = textColor,
                    darkColor = darkColor,
                    onPlay = { MusicPlayerManager.play(index) }
                )
            }
        }
    }
}

@Composable
private fun PlaylistRow(
    track: LocalTrack,
    selected: Boolean,
    isDark: Boolean,
    lightColor: Color,
    textColor: Color,
    darkColor: Color,
    onPlay: () -> Unit
) {
    val cardColor = lightColor
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    track.artist,
                    color = textColor.copy(alpha = if (isDark) 0.8f else 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = darkColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (selected) "Now" else "Play")
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "$m:${s.toString().padStart(2, '0')}"
}
