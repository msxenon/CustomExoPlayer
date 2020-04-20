package com.appchief.msa

import android.content.Context
import androidx.multidex.MultiDex
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton
import com.shakebugs.shake.Shake

class AppMain : CastApp() {
	 override fun attachBaseContext(base: Context?) {
		  super.attachBaseContext(base)
		  MultiDex.install(this)
	 }
	 override fun onCreate() {
		  super.onCreate()
		  Shake.start(this)
		  Shake.getReportConfiguration().isShowFloatingReportButton = false

		  ExoFactorySingeleton.init(this, true)
	 }

}