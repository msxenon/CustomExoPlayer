package com.appchief.msa.floating_player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import com.appchief.msa.exoplayerawesome.R
import com.appchief.msa.exoplayerawesome.databinding.VideoOverViewBinding
import com.appchief.msa.exoplayerawesome.listeners.CineamaticPlayerScreen
import com.appchief.msa.exoplayerawesome.listeners.CloseReason
import com.google.android.gms.cast.framework.media.widget.MiniControllerFragment

abstract class FloatingPLayerFragment : Fragment(),
	 CineamaticPlayerScreen {

	 private var dissmissCalled = false
	 lateinit var binding: VideoOverViewBinding
		  private set
	 var miniControllerFragment: MiniControllerFragment? = null
	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(null)

	 }

	 override fun isInFullScreen(): Boolean {
		  return activity?.requestedOrientation != Configuration.ORIENTATION_PORTRAIT
	 }
	 override fun onDestroy() {
		  miniControllerFragment?.onDestroy()
		  super.onDestroy()
	 }

	 override fun onActivityCreated(savedInstanceState: Bundle?) {
		  super.onActivityCreated(savedInstanceState)
		  view?.isFocusableInTouchMode = true
		  view?.requestFocus()
	 }

	 override fun onConfigurationChanged(newConfig: Configuration) {
		  super.onConfigurationChanged(newConfig)
		  if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			   binding.videoOverlayView.motionLayout?.transitionToState(R.id.fullScreen)
		  } else {
			   binding.videoOverlayView.motionLayout?.transitionToStart()

		  }
		  applyVisibility()
	 }

	 private fun applyVisibility() {
		  if (isInFullScreen()) {
			   activity?.window?.decorView?.apply {
					// Hide both the navigation bar and the status bar.
					// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
					// a general rule, you should design your app to hide the status bar whenever you
					// hide the navigation bar.
					systemUiVisibility =
						 View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			   }
			   binding.videoOverlayView.player?.fullSize()
		  } else {
			   activity?.window?.decorView?.apply {
					// Hide both the navigation bar and the status bar.
					// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
					// a general rule, you should design your app to hide the status bar whenever you
					// hide the navigation bar.
					systemUiVisibility =
						 View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			   }
			   binding.videoOverlayView.player?.minSize()
		  }
	 }

	 @SuppressLint("SourceLockedOrientationActivity")
	 override fun setScreenOrentation() {
		  Log.e("xx", "setScreenOrentation ${isInFullScreen()} ")
		  activity!!.requestedOrientation =
			   if (isInFullScreen()) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
	 }

	 override fun onResume() {
		  super.onResume()
		  Log.e("on", "Onresume called")
		  applyVisibility()
	 }
	 override fun onDissmiss(reason: CloseReason) {
		  fragmentManager?.beginTransaction()?.remove(this)?.commit()
	 }

	 override fun doMinimizePlayer() {
		  binding.videoOverlayView.minimize()
	 }
	 override fun onCreateView(
		  inflater: LayoutInflater,
		  container: ViewGroup?,
		  savedInstanceState: Bundle?
	 ): View? {
		  binding = VideoOverViewBinding.inflate(inflater)

		  return binding.root
	 }

	 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		  binding.videoOverlayView.player?.attachObersver(viewLifecycleOwner)
		  initFloating()

		  super.onViewCreated(view, savedInstanceState)
//		  val handler = Handler()
//		  handler.post(Runnable {
//			   var fm = fragmentManager?.findFragmentByTag("mini")
//			   Log.e("max11111 ","=== $fm")
//			   if (fm != null){
//					fragmentManager?.beginTransaction()?.remove(fm)?.commitNow()
//					fragmentManager?.executePendingTransactions()
//
//			   }
//			   cast_minicontroller_?.inflate()
//
//			   fm = fragmentManager?.findFragmentByTag("mini")
//			   Log.e("max2222 ","  === $fm")
//		  })

	 }

	 @SuppressLint("SourceLockedOrientationActivity")
	 private fun callDissmiss(closeReason: CloseReason = CloseReason.Swipe) {
		  if (!dissmissCalled) {
			   binding.videoOverlayView.player?.playerUiFinalListener?.onDissmiss(
						 closeReason
					)
					dissmissCalled = true
		  } else {
			   fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
		  }
	 }
	 fun initFloating() {
		  Log.e("FPF", "${binding.videoOverlayView.motionLayout != null}")
		  binding.videoOverlayView.player?.init(this)
		  binding.videoOverlayView.motionLayout?.setTransitionListener(object :
			   MotionLayout.TransitionListener {
			   override fun onTransitionTrigger(
					p0: MotionLayout?,
					p1: Int,
					p2: Boolean,
					p3: Float
			   ) {
					Log.e("FPF", "onTransitionTrigger $p2 ===== $p3")

					binding.videoOverlayView.player?.canShowController(p3 == 0f || p3 == 1f || p3 == -1f)
			   }

			   override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
					Log.e("FPF", "onTransitionStarted ")
			   }

			   override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
					Log.e("FPF", "onTransitionChange second$p2 prog=$p3")
					if (p2 == com.appchief.msa.exoplayerawesome.R.id.finish_left && p3 > 0.8) {
						 Log.e("FPF", "onTransitionChange dissmisscall prog=$p3")
						 callDissmiss()
					}
			   }

			   override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
					if (p1 == com.appchief.msa.exoplayerawesome.R.id.finish_left) {
						 callDissmiss()
					}
					val isMain =
						 p1 == com.appchief.msa.exoplayerawesome.R.id.start
					Log.e("FPF", "onTransitionCompleted $isMain")

					binding.videoOverlayView.player?.canShowController(isMain)
			   }
		  })
	 }

	 override fun onDestroyView() {
		  miniControllerFragment?.onDestroyView()
		  super.onDestroyView()
	 }
//	 override fun onPause() {
//		  ExoIntent.onPause(binding.videoOverlayView.player)
//		  super.onPause()
//	 }

	 fun setDetails(fragment: Fragment) {
		  childFragmentManager.beginTransaction()
			   .replace(com.appchief.msa.exoplayerawesome.R.id.detailsView, fragment).commit()
	 }

	 override fun showCustomUi(show: Boolean) {
		  //  binding.videoOverlayView.playerImage.visibility = show.controlVisibility()
	 }
}

