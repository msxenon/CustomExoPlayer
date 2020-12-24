package com.appchief.msa.exoplayerawesome.listeners

interface CastListener {
    fun isCastConnected(isConnected: Boolean)
    fun isCastAvailable(isAvailable: Boolean)
}