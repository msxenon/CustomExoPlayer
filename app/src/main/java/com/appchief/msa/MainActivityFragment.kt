package com.appchief.msa

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.appchief.msa.awesomeplayer.R
import com.appchief.msa.awesomeplayer.databinding.LoadingScBinding
import com.appchief.msa.exoplayerawesome.listeners.NowPlaying
import com.appchief.msa.exoplayerawesome.listeners.PlayerType
import com.appchief.msa.floating_player.FloatingPLayerFragment

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : FloatingPLayerFragment() {

	 private var loadingView: View? = null
	 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		  super.onViewCreated(view, savedInstanceState)
		  loadingView = LoadingScBinding.inflate(layoutInflater).root
		  binding.videoOverlayView.player?.addView(loadingView)
		  loadingView?.updateLayoutParams<FrameLayout.LayoutParams> {
			   width = 160
			   width = 160
			   this.gravity = Gravity.CENTER
		  }
		  binding.videoOverlayView.player?.playLinkNSub(
			   "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
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

	 override fun ControllerLayout(): Int? {
		  return R.layout.exo_player_controller
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

	 override fun loadingView(): View? {
		  return loadingView
	 }

	 override fun isConnectedToCast(): Boolean {
		  return false
	 }
}
