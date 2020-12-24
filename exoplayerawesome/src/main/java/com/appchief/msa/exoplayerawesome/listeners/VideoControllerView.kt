package com.appchief.msa.exoplayerawesome.listeners

interface MediaPlayerControl {
	 fun start()
	 fun pause()
	 fun onPauseSave()
	 val duration: Long
	 val currentPosition: Long
	 fun seekTo(pos: Long)
	 val isPlaying: Boolean
	 val bufferPercentage: Int
	 fun canPause(): Boolean
	 fun canSeekBackward(): Boolean
	 fun canSeekForward(): Boolean
val canHaveFullScreen: Boolean
	 fun toggleFullScreen()
	 fun canShowController(useController: Boolean)
	 fun minimizeAble(): Boolean
	 fun minmize()
}