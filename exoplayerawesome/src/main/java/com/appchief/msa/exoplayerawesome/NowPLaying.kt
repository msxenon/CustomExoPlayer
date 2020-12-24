package com.appchief.msa.exoplayerawesome

data class NowPlaying(
    val movieId: Long?,
    val episode: Any?,
    val type: PlayerType,
    val poster: String,
    val videoLink: String?,
    val geners: String,
    val title: String,
    val runtime: Long,
    val srtLink: String?
)