package com.appchief.msa.floating_player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import com.appchief.msa.exoplayerawesome.CinamaticExoPlayer
import com.appchief.msa.exoplayerawesome.CloseReason
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton
import com.appchief.msa.exoplayerawesome.R
import com.appchief.msa.exoplayerawesome.databinding.VideoOverViewBinding
import com.appchief.msa.exoplayerawesome.listeners.CineamaticPlayerScreen

abstract class FloatingPLayerFragment : Fragment(),
	 CineamaticPlayerScreen {

	 private var dissmissCalled = false
	 lateinit var binding: VideoOverViewBinding
		  private set

	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(null)
	 }

	 fun getPlayer(): CinamaticExoPlayer? {
		  return binding.videoOverlayView.playerContainer
	 }

	 abstract fun initPlayer(res: String? = null)

	 @SuppressLint("SourceLockedOrientationActivity")
	 override fun forcePortrait() {
		  if (!ExoFactorySingeleton.isTv)
			   activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
	 }

	 override fun isInFullScreen(): Boolean {
		  val x = isFullScreen
		  return x
	 }

	 override fun onDestroy() {
		  forcePortrait()
		  super.onDestroy()
	 }

	 private var isFullScreen = ExoFactorySingeleton.isTv
	 override fun onConfigurationChanged(newConfig: Configuration) {
		  super.onConfigurationChanged(newConfig)
		  isFullScreen = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
		  if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			   binding.videoOverlayView.motionLayout?.transitionToState(R.id.fullScreen)
		  } else {
			   binding.videoOverlayView.motionLayout?.transitionToState(R.id.start)
		  }
		  applyVisibility()
	 }

	 override fun canUseCast(): Boolean {
		  return true
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
			   binding.videoOverlayView.playerContainer?.fullSize()
		  } else {
			   activity?.window?.decorView?.apply {
					// Hide both the navigation bar and the status bar.
					// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
					// a general rule, you should design your app to hide the status bar whenever you
					// hide the navigation bar.
					systemUiVisibility =
						 View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			   }
			   binding.videoOverlayView.playerContainer?.minSize()
		  }
	 }

	 @SuppressLint("SourceLockedOrientationActivity")
	 override fun setScreenOrentation() {
		  if (!ExoFactorySingeleton.isTv) {
			   activity?.requestedOrientation =
					if (isInFullScreen()) ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
		  }
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
		  binding.videoOverlayView.playerContainer?.attachObersver(viewLifecycleOwner)
		  initFloating()

		  super.onViewCreated(view, savedInstanceState)
		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
			   view.viewTreeObserver.addOnWindowFocusChangeListener {
					applyVisibility()
			   }
		  }
	 }

	 @SuppressLint("SourceLockedOrientationActivity")
	 private fun callDissmiss(closeReason: CloseReason = CloseReason.Swipe) {
		  if (!dissmissCalled) {
			   binding.videoOverlayView.playerContainer?.playerUiFinalListener?.onDissmiss(
					closeReason
			   )
			   dissmissCalled = true
		  } else {
			   fragmentManager?.beginTransaction()?.remove(this)?.commitAllowingStateLoss()
		  }
	 }
	 fun initFloating() {
		  binding.videoOverlayView.playerContainer?.init(this)
		  binding.videoOverlayView.motionLayout?.setTransitionListener(object :
			   MotionLayout.TransitionListener {
			   override fun onTransitionTrigger(
					p0: MotionLayout?,
					p1: Int,
					p2: Boolean,
					p3: Float
			   ) {
					binding.videoOverlayView.playerContainer?.canShowController(p3 == 0f || p3 == 1f || p3 == -1f)
			   }

			   override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
					binding.videoOverlayView.playerContainer?.hideController()
			   }

			   override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {
					binding.videoOverlayView.playerContainer?.customController?.hide()
					if (p2 == com.appchief.msa.exoplayerawesome.R.id.finish_left && p3 > 0.8) {
						 callDissmiss()
					}
			   }

			   override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
					if (p1 == com.appchief.msa.exoplayerawesome.R.id.finish_left) {
						 callDissmiss()
					}
					val isMain =
						 p1 == com.appchief.msa.exoplayerawesome.R.id.start || p1 == R.id.fullScreen
					binding.videoOverlayView.playerContainer?.canShowController(isMain)
			   }
		  })
	 }

	 override fun showCustomUi(show: Boolean) {
		  //  binding.videoOverlayView.playerImage.visibility = show.controlVisibility()
	 }

	 fun canGoBack(): Boolean {
		  return if (ExoFactorySingeleton.isTv) {
			   getPlayer()?.customController?.canGoBackInTV() != false
		  } else
			   binding.videoOverlayView.isMinimized()
	 }
}

