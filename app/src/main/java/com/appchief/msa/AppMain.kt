package com.appchief.msa

import android.content.Context
import androidx.multidex.MultiDex
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton

class AppMain : CastApp() {
	 override fun attachBaseContext(base: Context?) {
		  super.attachBaseContext(base)
		  MultiDex.install(this)
	 }
	 override fun onCreate() {
		  super.onCreate()
		  ExoFactorySingeleton.init(this)
	 }

}