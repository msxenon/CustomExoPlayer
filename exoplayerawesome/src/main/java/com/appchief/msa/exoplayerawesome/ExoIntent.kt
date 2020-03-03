package com.appchief.msa.exoplayerawesome

import android.content.Intent
import android.util.Log
import com.appchief.msa.exoplayerawesome.listeners.CineamaticPlayerScreen
import com.appchief.msa.exoplayerawesome.listeners.CinematicPlayerViews
import com.google.android.exoplayer2.Player

object ExoIntent {
	 var player: Player? = null
	 var listener: CineamaticPlayerScreen? = null
	 var isInFullScreen = false
	 var views: CinematicPlayerViews? = null
	 var fullScreenActivity: FullScreenActivity? = null
	 fun openFullScreenMode(playerView: CinamaticExoPlayer?) {
		  savePlayer(playerView, false)?.let {
			   playerView?.context?.startActivity(
					Intent(
						 playerView.context,
						 FullScreenPlayer::class.java
					)
			   )
		  }
	 }

	 fun savePlayer(playerView: CinamaticExoPlayer?, itWasFullScreen: Boolean): Any? {
		  Log.e(Tag, "savePlayer $itWasFullScreen")
		  isInFullScreen = !itWasFullScreen
		  if (playerView?.player == null)
			   return null
		  views = playerView.cinematicPlayerViews
		  listener = playerView.playerUiFinalListener
		  player = playerView.player

		  playerView.player = null
		  return true
	 }

	 fun get(playerView: CinamaticExoPlayer?) {
		  Log.e(Tag, "get")
		  if (playerView?.player == null)
			   return
		  listener = playerView.playerUiFinalListener
		  player = playerView.player
		  playerView.player = null
		  playerView.context?.startActivity(
			   Intent(
					playerView.context,
					FullScreenPlayer::class.java
			   )
		  )
	 }

	 fun getPlayerHere(
		  playerView: CinamaticExoPlayer?,
		  fullScreenActivityP: FullScreenActivity? = null
	 ) {
		  Log.e(Tag, "getPlayerHere ${playerView != null} ${listener != null} ${player != null}")
		  if (playerView == null || listener == null || player == null)
			   return
		  fullScreenActivity = fullScreenActivityP
		  isInFullScreen = fullScreenActivityP != null
		  player?.playWhenReady = true
		  playerView.cinematicPlayerViews = views
		  playerView.playerUiFinalListener = listener
		  playerView.player = player
	 }

	 fun onDestroy() {
		  Log.e(Tag, "OnDestroy")
		  player = null
		  listener = null
	 }

	 fun dismissFullScreen() {
		  Log.e(Tag, "dismissFullScreen")
		  fullScreenActivity?.ondissmiss()
	 }

	 fun onPause(playerView: CinamaticExoPlayer?) {
		  Log.e(Tag, "onPause")
		  player?.playWhenReady = false
		  playerView?.onPauseSave()
	 }

	 val Tag = "ExoIntent"
}

interface FullScreenActivity {
	 fun ondissmiss()
}