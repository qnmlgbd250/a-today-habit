package com.today.habit.ui.component

import android.content.Context
import android.media.MediaPlayer
import com.today.habit.R

object CheckInSoundPlayer {

    fun playCompletionSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.check_in_sound) ?: return
            mediaPlayer.setOnCompletionListener {
                it.release()
            }
            mediaPlayer.setOnErrorListener { mp, _, _ ->
                mp.release()
                true
            }
            mediaPlayer.start()
        } catch (_: Exception) {
        }
    }
}
