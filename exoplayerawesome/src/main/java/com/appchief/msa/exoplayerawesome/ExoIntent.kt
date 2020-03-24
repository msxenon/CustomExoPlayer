package com.appchief.msa.exoplayerawesome

object ExoIntent {
	 //private var player: Player? = null
	 var usedInistances = 0
	 //	 var isInFullScreen = false
	 var pview: CinamaticExoPlayer? = null
	 // var savedPlayer : Player? = null
	 var fullScreenActivity: FullScreenActivity? = null
	 var paused = false



	 val Tag = "ExoIntent"

	 fun reInit() {
		  pview = null
		  fullScreenActivity = null
		  paused = false
	 }
}

interface FullScreenActivity {
	 fun ondissmiss()
}