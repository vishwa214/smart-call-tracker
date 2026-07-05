package com.vishwanth.callmera

import android.content.Context
import android.media.MediaPlayer

object MusicManager {

    private var mediaPlayer: MediaPlayer? = null

    fun start(context: Context) {

        if (mediaPlayer == null) {

            mediaPlayer =
                MediaPlayer.create(
                    context,
                    R.raw.background_music
                )

            mediaPlayer?.isLooping = true
        }

        mediaPlayer?.start()
    }

    fun stop() {

        mediaPlayer?.pause()
    }

    fun isPlaying(): Boolean {

        return mediaPlayer?.isPlaying ?: false
    }
}