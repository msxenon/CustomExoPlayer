package com.appchief.msa.exoplayerawesome

import android.content.Intent
import android.util.Log
import com.appchief.msa.exoplayerawesome.listeners.CinematicPlayerViews

object ExoIntent {
	 //private var player: Player? = null
	 var isInFullScreen = false
	 var views: CinematicPlayerViews? = null
	 var pview: CinamaticExoPlayer? = null
	 var fullScreenActivity: FullScreenActivity? = null
	 fun openFullScreenMode(playerView: CinamaticExoPlayer?) {
		  pview = playerView!!
		  savePlayer(playerView, false)?.let {
			   playerView.context?.startActivity(
					Intent(
						 playerView.context,
						 FullScreenPlayer::class.java
					)
			   )
		  }
	 }

	 fun savePlayer(playerView: CinamaticExoPlayer, itWasFullScreen: Boolean): Any? {
		  Log.e(Tag, "savePlayer $itWasFullScreen")
		  isInFullScreen = !itWasFullScreen

		  pview = createCopyFrom(playerView)
		  playerView.player = null
		  Log.e(
			   Tag,
			   "savePlayer $itWasFullScreen pv =  ${pview?.player != null} viewp = ${playerView.player != null}"
		  )

		  return true
	 }
//	 fun get(playerView: CinamaticExoPlayer?) {
//		  Log.e(Tag, "get")
//		  if (playerView?.player == null)
//			   return
//		  listener = playerView.playerUiFinalListener
//		  //player = playerView.player
//		  playerView.player = null
//		  playerView.context?.startActivity(
//			   Intent(
//					playerView.context,
//					FullScreenPlayer::class.java
//			   )
//		  )
//	 }

	 fun getPlayerHere(
		  playerView: CinamaticExoPlayer,
		  fullScreenActivityP: FullScreenActivity? = null
	 ) {
		  Log.e(Tag, "getPlayerHere ${playerView != null} ${pview != null}")

		  if (pview == null)
			   return
		  playerView.copyFrom(pview!!)
//		  if (playerView == null || listener == null || singeltonPlayer == null)
//			   return
		  fullScreenActivity = fullScreenActivityP
		  isInFullScreen = fullScreenActivityP != null
//		  singeltonPlayer?.playWhenReady = true
//		  playerView.cinematicPlayerViews = views
//		  playerView.playerUiFinalListener = listener
//		  playerView.player = singeltonPlayer
	 }

	 fun onDestroy() {
		  Log.e(Tag, "OnDestroy")
		  onPause(null)
//		  singeltonPlayer?.playWhenReady = false
		  // listener
	 }

	 fun dismissFullScreen() {
		  Log.e(Tag, "dismissFullScreen")
		  fullScreenActivity?.ondissmiss()
	 }

	 fun onPause(playerView: CinamaticExoPlayer?) {
		  Log.e(Tag, "onPause")
//		  singeltonPlayer?.playWhenReady = false
		  playerView?.onPauseSave()
	 }

	 val Tag = "ExoIntent"
	 fun createCopyFrom(oldCinamaticExoPlayer: CinamaticExoPlayer): CinamaticExoPlayer {
		  val x = CinamaticExoPlayer(oldCinamaticExoPlayer.context)
		  x.hasSettingsListener = oldCinamaticExoPlayer.hasSettingsListener
		  x.cinematicPlayerViews = oldCinamaticExoPlayer.cinematicPlayerViews
		  x.player = oldCinamaticExoPlayer.player
		  x.playerUiFinalListener = oldCinamaticExoPlayer.playerUiFinalListener
		  x.setController(null)
//		  x.loadingV = oldCinamaticExoPlayer.loadingV
		  return x
	 }
}

interface FullScreenActivity {
	 fun ondissmiss()
}