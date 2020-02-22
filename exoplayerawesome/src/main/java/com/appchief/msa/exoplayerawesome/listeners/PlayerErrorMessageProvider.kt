package com.appchief.msa.exoplayerawesome.listeners

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.util.ErrorMessageProvider

internal open class PlayerErrorMessageProvider : ErrorMessageProvider<ExoPlaybackException> {

	 override fun getErrorMessage(e: ExoPlaybackException): android.util.Pair<Int, String> {
		  var errorString = "error generic"
		  if (e.type == ExoPlaybackException.TYPE_RENDERER) {
			   val cause = e.rendererException
			   if (cause is MediaCodecRenderer.DecoderInitializationException) {
					// Special case for decoder initialization failures.
					val decoderInitializationException = cause
					errorString = " error init"
			   }
		  }
		  return android.util.Pair.create(0, errorString)
	 }
}