package com.appchief.msa

import android.content.Context
import android.util.Log
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

         Log.e("tag", "App Started")
         Shake.start(this)
         Shake.getReportConfiguration().isShowFloatingReportButton = false

         ExoFactorySingeleton.init(this, istv = false)
     }

}