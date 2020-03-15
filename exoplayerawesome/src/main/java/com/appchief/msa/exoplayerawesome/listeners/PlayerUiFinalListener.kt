package com.appchief.msa.exoplayerawesome.listeners

import android.graphics.drawable.Drawable

interface CineamaticPlayerScreen {
	 fun onMessageRecived(msg: String?, state: Int)
	 fun getLastPosition(modelId: NowPlaying?): Long
	 fun savePlayPosition(nowWasPlaying: NowPlaying?, position: Long, duration: Long)
	 fun canMinimize(): Boolean
	 fun onDissmiss(reason: CloseReason)
	 fun doMinimizePlayer()
	 fun isFirstItem(): Boolean
	 fun isPlayList(): Boolean
	 fun setScreenOrentation()
	 fun showSettings(forCasting: Boolean)
	 fun showCustomUi(show: Boolean)
	 fun setMoviePoster(result: (it: Drawable) -> Unit)
	 fun isInFullScreen(): Boolean
}

interface CinematicPlayerViews {
	 fun resizeMode(): Int
	 fun videoScalingMode(): Int

	 val loadingView: Int?
	 val controlLayout: Int?
	 val loaderSizeInP: Int
}
enum class PlayerStatus {
	 Playing, CantPlay, Error, JustMSG, InternalError
}

enum class CloseReason {
	 Swipe, BackButton, Casting
}
enum class PlayerType {
	 CHANNEL, MOVIE, EPISODE
}

data class NowPlaying(
	 val movieId: Long?,
	 val episodeId: Long?,
	 val type: PlayerType,
	 val poster: String,
	 val videoLink: String?,
	 val geners: String,
	 val title: String,
	 val runtime: Long,
	 val srtLink: String?
) {

	 fun nowPlayingId(): Long {
		  return when (type) {
			   PlayerType.CHANNEL -> 0L
			   PlayerType.MOVIE -> movieId ?: 0
			   PlayerType.EPISODE -> episodeId ?: 0
		  }
	 }
}