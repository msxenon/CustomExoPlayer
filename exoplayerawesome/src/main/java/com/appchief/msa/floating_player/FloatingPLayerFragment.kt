package com.appchief.msa.floating_player

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import com.appchief.msa.exoplayerawesome.R
import com.appchief.msa.exoplayerawesome.databinding.VideoOverViewBinding
import com.appchief.msa.exoplayerawesome.listeners.CineamaticPlayerScreen
import com.appchief.msa.exoplayerawesome.listeners.CloseReason

abstract class FloatingPLayerFragment : Fragment(),
	 CineamaticPlayerScreen {

	 private var dissmissCalled = false
	 lateinit var binding: VideoOverViewBinding
		  private set

	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(null)
	 }

	 override fun onActivityCreated(savedInstanceState: Bundle?) {
		  super.onActivityCreated(savedInstanceState)
		  view?.isFocusableInTouchMode = true
		  view?.requestFocus()
		  view?.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
			   Log.i("llll", "keyCode: $keyCode")
			   if (keyCode == KeyEvent.KEYCODE_BACK && event.action === KeyEvent.ACTION_UP) {
					Log.i("lllll", "onKey Back listener is working!!!")
					callDissmiss(CloseReason.BackButton)
					return@OnKeyListener true
			   }
			   false
		  })
	 }

	 override fun setScreenOrentation(inFullScreenMode: Boolean) {
		  activity?.requestedOrientation = if (!inFullScreenMode)
			   ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
		  else
			   ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
		  initFloating()
		  super.onViewCreated(view, savedInstanceState)
	 }

	 @SuppressLint("SourceLockedOrientationActivity")
	 private fun callDissmiss(closeReason: CloseReason = CloseReason.Swipe) {
		  if (!dissmissCalled) {
			   if (closeReason == CloseReason.BackButton && activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
					activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
			   } else {
					binding.videoOverlayView.player?.playerUiFinalListener?.onDissmiss(
						 closeReason
					)
					dissmissCalled = true
			   }
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
					if (p2 == R.id.finish_left && p3 > 0.8) {
						 Log.e("FPF", "onTransitionChange dissmisscall prog=$p3")
						 callDissmiss()
					}
			   }

			   override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
					if (p1 == R.id.finish_left) {
						 callDissmiss()
					}
					val isMain = p1 == R.id.start || p1 == R.id.fullScreen
					Log.e("FPF", "onTransitionCompleted $isMain")

					binding.videoOverlayView.player?.canShowController(isMain)
			   }
		  })
	 }

	 override fun onResume() {
		  binding.videoOverlayView.player?.onResume()
		  super.onResume()
	 }

	 override fun onPause() {
		  binding.videoOverlayView.player?.onPauseSave()
		  super.onPause()
	 }

	 fun setDetails(fragment: Fragment) {
		  childFragmentManager.beginTransaction().replace(R.id.detailsView, fragment).commit()
	 }

	 override fun onConfigurationChanged(newConfig: Configuration) {
		  super.onConfigurationChanged(newConfig)
		  // Checking the orientation of the screen
		  if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			   //First Hide other objects (listview or recyclerview), better hide them using Gone.
			   activity?.window?.decorView?.apply {
					// Hide both the navigation bar and the status bar.
					// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
					// a general rule, you should design your app to hide the status bar whenever you
					// hide the navigation bar.
					systemUiVisibility =
						 View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
			   }
//			   val m = binding.videoOverlayView.container_motionLayout ?: return
//			   val params =  m.layoutParams
//			   params?.width = ViewGroup.LayoutParams.MATCH_PARENT
//			   params?.height = ViewGroup.LayoutParams.MATCH_PARENT
//			   m.layoutParams = params
//			   m.requestLayout()
			   binding.videoOverlayView.player?.isInFullScreenMode = true
			   binding.videoOverlayView.motionLayout?.transitionToState(R.id.fullScreen)
			   // binding.videoOverlayView.motionLayout?.loadLayoutDescription(0)
		  } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			   //unhide your objects here.
			   activity?.window?.decorView?.apply {
					// Hide both the navigation bar and the status bar.
					// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
					// a general rule, you should design your app to hide the status bar whenever you
					// hide the navigation bar.
					systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE
			   }
//			   val m = binding.videoOverlayView.container_motionLayout ?: return
//			   val params =  m.layoutParams as ConstraintLayout.LayoutParams
//			   params.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
//			   params.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
//			   m.layoutParams = params
//			   binding.videoOverlayView.player?.isInFullScreenMode = false
//			   binding.videoOverlayView.motionLayout?.loadLayoutDescription(R.xml.floating_player_scene)
//			   val m = binding.videoOverlayView.container_motionLayout ?: return
//			   val params =  m.layoutParams
//			   params?.width = ViewGroup.LayoutParams.MATCH_PARENT
//			   params?.height = 200.DpToPx()
//			   m.layoutParams = params
//			   m.requestLayout()
			   binding.videoOverlayView.player?.isInFullScreenMode = false
			   binding.videoOverlayView.motionLayout?.transitionToStart()
		  }
	 }
}

fun Int.DpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
