package com.appchief.msa

import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.appchief.msa.MainActivity.Companion.isChannel
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

	 private var snackBar: Snackbar? = null
	 //	 private var loadingView: View? = null
	 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		  binding.videoOverlayView.playerContainer?.cinematicPlayerViews = CinematicOnce()
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

	 fun initPlayer() {
		  binding.videoOverlayView.playerContainer?.playLinkNSub(
			   MainActivity.link,
			   null,
			   null,
			   if (isChannel) PlayerType.CHANNEL else PlayerType.MOVIE,
			   "https://mkvtoolnix.download/samples/vsshort-en.srt",
			   MainActivity.poster, "Action", MainActivity.movieName, 10000
		  )
		  binding.videoOverlayView.playerContainer?.setDoubleTapActivated()
	 }

	 override fun onMessageRecived(msg: String?, state: Int) {
		  msg?.takeIf { view != null && state >= 0 }?.let {
			   snackBar = Snackbar.make(view!!, msg, Snackbar.LENGTH_INDEFINITE)
			   snackBar?.setAction("Retry") {
					snackBar?.dismiss()
					initPlayer()
			   }
			   snackBar?.show()
		  } ?: kotlin.run {
			   Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
		  }

		  Log.e("main", "$msg $state")
	 }

	 override fun onDestroy() {
		  snackBar?.dismiss()

		  super.onDestroy()
	 }
	 override fun getLastPosition(modelId: NowPlaying?): Long {
		  Log.e("main", "getLastPosition $modelId")
		  return 6000
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

	 override fun showSettings(forCasting: Boolean) {
//		  Log.e("smdd", "showsettttt ${binding.videoOverlayView.player != null}")
		  if (!forCasting) {
			   TrackSelectionDialog.createForTrackSelector(childFragmentManager,
					activity,
					binding.videoOverlayView.playerContainer!!.trackSelector,
					DialogInterface.OnDismissListener { })
		  } else {
			   activity?.startActivity(Intent(activity, ExpandedControlsActivity::class.java))
		  }
	 }

	 override fun setMoviePoster(result: (it: Drawable) -> Unit) {
		  val myIcon =
			   resources.getDrawable(com.appchief.msa.awesomeplayer.R.drawable.castbg)
		  result(myIcon)
	 }


}
