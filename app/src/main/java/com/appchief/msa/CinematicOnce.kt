package com.appchief.msa

import com.appchief.msa.awesomeplayer.R
import com.appchief.msa.exoplayerawesome.listeners.CinematicPlayerViews

class CinematicOnce : CinematicPlayerViews {
	 override val loadingView: Int?
		  get() = R.layout.loading_sc
	 override val controlLayout: Int?
		  get() = R.layout.exo_player_controller
	 override val loaderSizeInP: Int
		  get() = 100
}