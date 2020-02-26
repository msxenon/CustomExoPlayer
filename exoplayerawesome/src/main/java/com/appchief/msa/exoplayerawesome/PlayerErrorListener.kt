package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.util.Log
import com.appchief.msa.exoplayerawesome.listeners.CineamaticPlayerScreen
import com.appchief.msa.exoplayerawesome.listeners.PlayerStatus
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

class PlayerEventListener(
	 private val context: Context?,
	 private val cinemPlayer: CinamaticExoPlayer?,
	 private val playerUiFinalListener: CineamaticPlayerScreen?
) : Player.EventListener {

	 private var lastSeenTrackGroupArray: TrackGroupArray? = null

	 override fun onPlayerError(error: ExoPlaybackException) {
		  Log.e("PlayerEventListener", "err")
		  error.printStackTrace()
		  cinemPlayer?.useController = false
		  playerUiFinalListener?.onMessageRecived(
			   context?.getString(R.string.videonotav),
			   PlayerStatus.CantPlay
		  )
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
							  context?.getString(R.string.error_unsupported_video),
							  PlayerStatus.Error
						 )
					}
					if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
						 playerUiFinalListener?.onMessageRecived(
							  context?.getString(R.string.error_unsupported_audio),
							  PlayerStatus.Error
						 )
					}
			   }
			   lastSeenTrackGroupArray = trackGroups
		  }
	 }

	 override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
		  if (playbackState != Player.STATE_IDLE) {
			   //  cinemPlayer?.useController = true
		  }
	 }
}