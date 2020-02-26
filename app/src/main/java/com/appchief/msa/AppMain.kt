package com.appchief.msa

import android.app.Application
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton

class AppMain : Application() {
	 override fun onCreate() {
		  super.onCreate()
		  ExoFactorySingeleton.init(this)
	 }
}