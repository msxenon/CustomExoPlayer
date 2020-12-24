package com.appchief.msa.exoplayerawesome.listeners

import android.graphics.drawable.Drawable
import android.view.View
import com.appchief.msa.exoplayerawesome.CloseReason
import com.appchief.msa.exoplayerawesome.NowPlaying

interface CineamaticPlayerScreen {
	 fun onMessageRecived(msg: String?, state: Int)
	 fun getLastPosition(modelId: NowPlaying?): Long
	 fun savePlayPosition(nowWasPlaying: NowPlaying?, position: Long, duration: Long)
	fun canMinimize(): Boolean
	fun onDissmiss(reason: CloseReason)
	fun doMinimizePlayer()
	fun hasPrevItem(): Boolean
	fun hasNextItem(): Boolean
	fun playNext()
	fun playPrev()
	fun setScreenOrentation()
	fun showSettings(forCasting: Boolean)
	fun showCustomUi(show: Boolean)
	fun setMoviePoster(result: (it: Drawable?) -> Unit)
	fun isInFullScreen(): Boolean
	fun forcePortrait()
	fun canUseCast(): Boolean
	fun addtionalControllerButtonsInit(view: View?)
}

