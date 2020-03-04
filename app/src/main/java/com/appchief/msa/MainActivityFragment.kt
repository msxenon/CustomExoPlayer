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

	 companion object {
		  var isFirstVideo = true
	 }
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
		  val video =
			   "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4"
		  binding.videoOverlayView.player?.cinematicPlayerViews = CinematicOnce()
		  binding.videoOverlayView.player?.playLinkNSub(
			   video,
			   null,
			   null,
			   PlayerType.MOVIE,
			   null,
			   "null", "", "", 10000
		  )
		  setDetails(DetailsFrag())
		  isFirstVideo = !isFirstVideo
	 }

	 override fun onMessageRecived(msg: String?, state: Int) {
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
