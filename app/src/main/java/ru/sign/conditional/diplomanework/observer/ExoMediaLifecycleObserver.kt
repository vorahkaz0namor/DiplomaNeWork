package ru.sign.conditional.diplomanework.observer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class ExoMediaLifecycleObserver(
    private val context: Context,
    private val attachmentUrl: String,
    private val onPlayerReady: (ExoPlayer) -> Unit
) : LifecycleEventObserver {
    private var player: ExoPlayer? = null
    private lateinit var mediaItem: MediaItem
    private var playWhenReady = false

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> initializePlayer()
            Lifecycle.Event.ON_RESUME ->
                if (player == null)
                    initializePlayer()
            Lifecycle.Event.ON_STOP -> releasePlayer()
            Lifecycle.Event.ON_DESTROY ->
                source.lifecycle.removeObserver(this)
            else -> Unit
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(context)
            .build()
            .also {
                mediaItem = MediaItem.fromUri(attachmentUrl)
                it.setMediaItem(mediaItem)
                it.playWhenReady = playWhenReady
                it.prepare()
                onPlayerReady(it)
            }
    }

    private fun releasePlayer() {
        player?.let {
            playWhenReady = it.playWhenReady
            it.release()
        }
        player = null
    }
}