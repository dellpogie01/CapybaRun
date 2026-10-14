package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log

class SoundManager(private val context: Context) {
    private var toneGen: ToneGenerator? = null

    init {
        try {
            toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playJump(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playEggCollect(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_DTMF_3, 120)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playFlightStart(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_DTMF_8, 200)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playGameOver(enabled: Boolean) {
        if (!enabled) return
        try {
            toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_PBX_L, 300)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun release() {
        try {
            toneGen?.release()
            toneGen = null
        } catch (e: Exception) {
            // ignore
        }
    }
}
