package com.appchief.msa.exoplayerawesome.listeners

import android.view.View

interface CineamaticPlayerScreen {
	 fun onMessageRecived(msg: String?, state: PlayerStatus)
	 fun getLastPosition(modelId: NowPlaying?): Long
	 fun savePlayPosition(nowWasPlaying: NowPlaying?, position: Long, duration: Long)
	 fun ControllerLayout(): Int?
	 fun canMinimize(): Boolean
	 fun onDissmiss(reason: CloseReason)
	 fun doMinimizePlayer()
	 fun isFirstItem(): Boolean
	 fun isPlayList(): Boolean
	 fun loadingView(): View?
}

enum class PlayerStatus {
	 Playing, CantPlay, Error, JustMSG
}

enum class CloseReason {
	 Swipe, BackButton
}
enum class PlayerType {
	 CHANNEL, MOVIE, EPISODE
}

data class NowPlaying(val movieId: Long?, val episodeId: Long?, val type: PlayerType)