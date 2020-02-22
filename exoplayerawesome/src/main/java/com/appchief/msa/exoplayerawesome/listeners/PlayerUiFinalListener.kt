package com.appchief.msa.exoplayerawesome.listeners

interface PlayerUiFinalListener {
	 fun onMessageRecived(msg: String?, state: PlayerStatus)
	 fun getLastPosition(modelId: NowPlaying?): Long
	 fun savePlayPosition(nowWasPlaying: NowPlaying?, position: Long, duration: Long)
}

enum class PlayerStatus {
	 Playing, CantPlay, Error, JustMSG
}

enum class PlayerType {
	 CHANNEL, MOVIE, EPISODE
}

data class NowPlaying(val movieId: Long?, val episodeId: Long?, val type: PlayerType)