package com.appchief.msa

import com.appchief.msa.awesomeplayer.R
import com.appchief.msa.exoplayerawesome.listeners.CinematicPlayerViews
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

class CinematicOnce : CinematicPlayerViews {
	 override fun resizeMode(): Int {
		  return AspectRatioFrameLayout.RESIZE_MODE_FIT
	 }

	 override fun videoScalingMode(): Int {
		  return C.VIDEO_SCALING_MODE_SCALE_TO_FIT
	 }

	 override val loadingView: Int?
		  get() = R.layout.loading_sc
	 override val controlLayout: Int?
		  get() = R.layout.exo_player_controller
	 override val loaderSizeInP: Int
		  get() = 100
}