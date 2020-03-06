package com.appchief.msa

import android.os.Bundle
import android.util.Log
import android.view.View
import com.appchief.msa.exoplayerawesome.listeners.NowPlaying
import com.appchief.msa.exoplayerawesome.listeners.PlayerType
import com.appchief.msa.floating_player.FloatingPLayerFragment
import com.google.android.material.snackbar.Snackbar

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : FloatingPLayerFragment() {

	 companion object {
		  var isFirstVideo = true
	 }
	 //	 private var loadingView: View? = null
	 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		  binding.videoOverlayView.player?.cinematicPlayerViews = CinematicOnce()
		  super.onViewCreated(view, savedInstanceState)
//		  loadingView = LoadingScBinding.inflate(layoutInflater).root
//		  binding.videoOverlayView.player?.addView(loadingView)
//		  loadingView?.updateLayoutParams<FrameLayout.LayoutParams> {
//			   width = 160
//			   width = 160
//			   this.gravity = Gravity.CENTER
//		  }
//		  val video =
//			   "http://tv.supercellnetwork.com:1935/nile3/mbc3.stream_360p/playlist.m3u8"//"https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4"
		  initPlayer()
		  setDetails(DetailsFrag())
		  isFirstVideo = !isFirstVideo
	 }

	 private fun initPlayer() {
		  binding.videoOverlayView.player?.playLinkNSub(
			   MainActivity.link,
			   null,
			   null,
			   PlayerType.CHANNEL,
			   null,
			   "null", "", "", 10000
		  )
	 }

	 override fun onMessageRecived(msg: String?, state: Int) {
		  msg?.takeIf { view != null }?.let {
			   val snack = Snackbar.make(view!!, msg, Snackbar.LENGTH_INDEFINITE)
			   snack.setAction("Retry") {
					snack.dismiss()
					initPlayer()
			   }
			   snack.show()
		  }

		  Log.e("main", "$msg $state")
	 }



	 override fun getLastPosition(modelId: NowPlaying?): Long {
		  return 10000
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

	 override fun showSettings() {
		  binding.videoOverlayView.player?.trackSelector
		  Log.e("smdd", "showsettttt")
	 }
}
