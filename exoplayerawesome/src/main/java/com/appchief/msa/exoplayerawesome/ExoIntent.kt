package com.appchief.msa.exoplayerawesome

import android.util.Log

object ExoIntent {
	 //private var player: Player? = null
	 var usedInistances = 0
	 //	 var isInFullScreen = false
	 var pview: CinamaticExoPlayer? = null
	 // var savedPlayer : Player? = null
	 var fullScreenActivity: FullScreenActivity? = null
	 var paused = false
	 fun openFullScreenMode(playerView: CinamaticExoPlayer?) {
//		  savePlayer(playerView)?.let {
//			   playerView?.context?.startActivity(
////					Intent(
////						 playerView.context,
////						 FullScreenPlayer::class.java
////					)
//			   )
//		  }
	 }

	 fun savePlayer(playerView: CinamaticExoPlayer?): Any? {
		  if (playerView?.player == null)
			   return null
		  // savedPlayer = playerView?.player
		  pview = createCopyFrom(playerView)
//		 // playerView.player = null
//		  Log.e(
//			   Tag,
//			   "savePlayer $itWasFullScreen pv =  ${pview?.player != null} viewp = ${playerView.player != null}"
//		  )

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
		  playerView: CinamaticExoPlayer?
	 ) {
		  Log.e(Tag, "getPlayerHere ${playerView != null} ${pview != null}")
		  if (pview == null)
			   return
		  playerView?.copyFrom(pview!!)
		  // playerView?.player = savedPlayer
//		  if (playerView == null || listener == null || singeltonPlayer == null)
//			   return
		  //		  singeltonPlayer?.playWhenReady = true
//		  playerView.cinematicPlayerViews = views
//		  playerView.playerUiFinalListener = listener
//		  playerView.player = singeltonPlayer
	 }



	 fun dismissFullScreen() {
		  Log.e(Tag, "dismissFullScreen")
		  fullScreenActivity?.ondissmiss()
	 }



	 val Tag = "ExoIntent"
	 fun createCopyFrom(oldCinamaticExoPlayer: CinamaticExoPlayer): CinamaticExoPlayer {
		  val x = CinamaticExoPlayer(oldCinamaticExoPlayer.context)
		  Log.e("createCopyFrom", "pnull ${x.player == null}")
		  x.nowPlaying = oldCinamaticExoPlayer.nowPlaying
		  x.hasSettingsListener = oldCinamaticExoPlayer.hasSettingsListener
		  x.cinematicPlayerViews = oldCinamaticExoPlayer.cinematicPlayerViews
		  x.player = oldCinamaticExoPlayer.player
		  x.playerUiFinalListener = oldCinamaticExoPlayer.playerUiFinalListener
		  oldCinamaticExoPlayer.player = null
		  return x
	 }

	 fun reInit() {
		  pview = null
		  fullScreenActivity = null
		  paused = false
	 }
}

interface FullScreenActivity {
	 fun ondissmiss()
}