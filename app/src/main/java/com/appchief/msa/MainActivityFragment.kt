package com.appchief.msa

import android.os.Bundle
import android.util.Log
import android.view.View
import com.appchief.msa.exoplayerawesome.listeners.NowPlaying
import com.appchief.msa.exoplayerawesome.listeners.PlayerType
import com.appchief.msa.floating_player.FloatingPLayerFragment

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : FloatingPLayerFragment() {

	 //	 private var loadingView: View? = null
	 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		  super.onViewCreated(view, savedInstanceState)
//		  loadingView = LoadingScBinding.inflate(layoutInflater).root
//		  binding.videoOverlayView.player?.addView(loadingView)
//		  loadingView?.updateLayoutParams<FrameLayout.LayoutParams> {
//			   width = 160
//			   width = 160
//			   this.gravity = Gravity.CENTER
//		  }
		  binding.videoOverlayView.player?.cinematicPlayerViews = CinematicOnce()
		  binding.videoOverlayView.player?.playLinkNSub(
			   "http://93.191.114.6:8081/vod/f1a21737-0a5c-4dec-8bca-7bd4b431cb26/t1Ak5ejQKKHiwf4/,t1Ak5ejQKKHiwf4_720.mp4,t1Ak5ejQKKHiwf4_480.mp4,t1Ak5ejQKKHiwf4.srt,.urlset/master.m3u8",//"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
			   null,
			   null,
			   PlayerType.MOVIE,
			   null,
			   "null", "", "", 596000
		  )
		  setDetails(DetailsFrag())
	 }

	 override fun onMessageRecived(msg: String?, state: Int) {
		  Log.e("main", "$msg $state")
	 }



	 override fun getLastPosition(modelId: NowPlaying?): Long {
		  return 591000
	 }

	 override fun savePlayPosition(nowWasPlaying: NowPlaying?, position: Long, duration: Long) {
	 }



	 override fun canMinimize(): Boolean {
		  return true
	 }



	 override fun isFirstItem(): Boolean {
		  return false
	 }

	 override fun isPlayList(): Boolean {
		  return false
	 }

	 override fun isConnectedToCast(): Boolean {
		  return false
	 }
}
