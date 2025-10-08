package com.example.neuronote

import android.content.Context
import androidx.annotation.RawRes
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Track(
    val id: String,
    val title: String,
    val subtitle: String,
    @RawRes val resId: Int
)

@UnstableApi
object MusicPlayerManager {

    private var player: ExoPlayer? = null
    private lateinit var appContext: Context

    // Public state for UI
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    // Tracks managed globally
    private lateinit var _tracks: List<Track>
    val tracks: List<Track> get() = _tracks

    fun init(context: Context, tracks: List<Track>) {
        if (!this::appContext.isInitialized) {
            appContext = context.applicationContext
        }
        _tracks = tracks

        if (player == null) {
            player = ExoPlayer.Builder(appContext).build().apply {
                // ✅ Ensure we hold media audio focus even when DND is enabled
                val audioAttrs = AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build()
                setAudioAttributes(audioAttrs, /* handleAudioFocus = */ true)
                setHandleAudioBecomingNoisy(true)

                repeatMode = Player.REPEAT_MODE_ALL
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        _currentIndex.value = currentMediaItemIndex.coerceAtLeast(0)
                    }
                })
            }
            rebuildPlaylist()
        }
    }

    private fun rebuildPlaylist() {
        val p = player ?: return
        p.stop()
        p.clearMediaItems()

        _tracks.forEach { t ->
            val uri = RawResourceDataSource.buildRawResourceUri(t.resId)
            p.addMediaItem(MediaItem.fromUri(uri))
        }
        p.prepare()
    }

    fun play(index: Int? = null) {
        val p = player ?: return

        if (index != null && index in tracks.indices) {
            // If new index is different from current, change track & restart
            if (index != _currentIndex.value) {
                p.seekTo(index, 0)
            }
        }
        p.playWhenReady = true
        p.play()
    }


    fun pause() {
        player?.pause()
    }

    fun next() {
        player?.seekToNextMediaItem()
    }

    fun prev() {
        player?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun currentPosition(): Long = player?.currentPosition ?: 0L
    fun duration(): Long = player?.duration ?: 0L

    fun release() {
        player?.release()
        player = null
    }
}
