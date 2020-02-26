package com.appchief.msa.floating_player

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

abstract class FloatingPLayerFragment : Fragment(),
	 CineamaticPlayerScreen {

	 lateinit var binding: VideoOverViewBinding
		  private set

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
			   }

			   override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
					if (p1 == R.id.finish_left) {
						 binding.videoOverlayView.player?.playerUiFinalListener?.onDissmiss(
							  CloseReason.Swipe
						 )
					}
					val isMain = p1 == R.id.start
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
}