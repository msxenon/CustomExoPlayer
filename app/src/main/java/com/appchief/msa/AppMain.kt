package com.appchief.msa

import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton

class AppMain : CastApp() {
	 override fun onCreate() {
		  super.onCreate()
		  ExoFactorySingeleton.init(this)
	 }

}