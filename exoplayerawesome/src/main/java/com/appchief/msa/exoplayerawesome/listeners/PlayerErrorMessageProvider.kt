package com.appchief.msa.exoplayerawesome.listeners

import android.content.Context
import com.appchief.msa.exoplayerawesome.getExoString
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.util.ErrorMessageProvider

internal open class PlayerErrorMessageProvider(val context: Context) :
    ErrorMessageProvider<ExoPlaybackException> {

    override fun getErrorMessage(e: ExoPlaybackException): android.util.Pair<Int, String> {
        if (e.type == ExoPlaybackException.TYPE_RENDERER) {
            val cause = e.rendererException
            if (cause is MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                val decoderInitializationException = cause

            }
        }
        val x = context.getExoString(e)
        return android.util.Pair.create(0, x ?: "Generic Error")
    }
}