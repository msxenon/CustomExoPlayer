package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.util.Log
import com.appchief.msa.exoplayerawesome.listeners.CineamaticPlayerScreen
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

class PlayerEventListener(
	 private val context: Context?,
	 private val cinemPlayer: CinamaticExoPlayer?,
	 private val playerUiFinalListener: CineamaticPlayerScreen?
) : Player.EventListener {

	 companion object {
		  private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
			   if (e.type != ExoPlaybackException.TYPE_SOURCE) {
					return false
			   }
			   var cause: Throwable? = e.sourceException
			   while (cause != null) {
					if (cause is BehindLiveWindowException) {
						 return true
					}
					cause = cause.cause
			   }
			   return false
		  }
	 }


	 private var lastSeenTrackGroupArray: TrackGroupArray? = null
	 override fun onPlayerError(error: ExoPlaybackException) {
		  val m = isBehindLiveWindow(error)
		  Log.e("PlayerEventListener", "err ${error.type} isbehind=$m")
		  if (m) {
			   val z = cinemPlayer?.initializePlayer(true)
			   Log.e("PlayerEventListener", "err ${error.type} isReinit = $z")
		  } else {
			   error.printStackTrace()
			   // cinemPlayer?.useController = false
			   val errMsg = context?.getExoString(error)
			   errMsg?.let {
					playerUiFinalListener?.onMessageRecived(
						 errMsg,
						 error.type
					)
			   } ?: kotlin.run {
					cinemPlayer?.playerUiFinalListener?.onMessageRecived(errMsg, error.type)
			   }
		  }
	 }

	 override fun onTracksChanged(
		  trackGroups: TrackGroupArray,
		  trackSelections: TrackSelectionArray
	 ) {
		  if (trackGroups !== lastSeenTrackGroupArray) {
			   val mappedTrackInfo = cinemPlayer?.trackSelector?.currentMappedTrackInfo
			   if (mappedTrackInfo != null) {
					if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
						 playerUiFinalListener?.onMessageRecived(
							  context?.getString(com.appchief.msa.exoplayerawesome.R.string.error_unsupported_video),
							  ExoPlaybackException.TYPE_RENDERER
						 )
					}
					if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
						 playerUiFinalListener?.onMessageRecived(
							  context?.getString(com.appchief.msa.exoplayerawesome.R.string.error_unsupported_audio),
							  ExoPlaybackException.TYPE_RENDERER
						 )
					}
			   }
			   lastSeenTrackGroupArray = trackGroups
		  }
	 }

	 override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
		  try {
			   Log.e("PEL", "$playWhenReady $playbackState ")
			   cinemPlayer?.customController?.showLoading(false)
			   if (playbackState == ExoPlayer.STATE_BUFFERING) {
					cinemPlayer?.customController?.showLoading(true)
					cinemPlayer?.hideController()
			   } else if (playbackState == ExoPlayer.STATE_READY) {
					cinemPlayer?.forceReplay = false

					cinemPlayer?.checkHasSettings()
			   } else if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) {
					//cinemPlayer?.seekTo(0)
					if (playWhenReady && cinemPlayer?.forceReplay == true) {
						 cinemPlayer.start()
					}
			   }
			   cinemPlayer?.customController?.updateViews(playbackState == ExoPlayer.STATE_BUFFERING)
		  } catch (e: Exception) {
			   e.printStackTrace()
		  }
	 }
}

private fun Context?.getExoString(error: ExoPlaybackException): String? {
	 if (this == null)
		  return null
	 return when (error.type) {
		  ExoPlaybackException.TYPE_SOURCE -> this.getString(R.string.faild_to_connect)
		  ExoPlaybackException.TYPE_RENDERER -> this.getString(R.string.player_render_issue)
		  ExoPlaybackException.TYPE_UNEXPECTED -> this.getString(R.string.player_unknown)
		  ExoPlaybackException.TYPE_REMOTE -> this.getString(R.string.videonotav)
		  ExoPlaybackException.TYPE_OUT_OF_MEMORY -> this.getString(R.string.player_outofmem)
		  else -> this.getString(R.string.player_unknown)
	 }
}
