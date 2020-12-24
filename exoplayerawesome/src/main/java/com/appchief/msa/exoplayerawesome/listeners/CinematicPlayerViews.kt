package com.appchief.msa.exoplayerawesome.listeners

interface CinematicPlayerViews {
    fun resizeMode(): Int
    fun videoScalingMode(): Int
    val loadingView: Int?
    val controlLayout: Int?
    val loaderSizeInP: Int
}



