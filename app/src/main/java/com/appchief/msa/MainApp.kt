package com.appchief.msa

import android.app.Application
import com.appchief.msa.exoplayerawesome.BuildConfig
import timber.log.Timber

class MainApp : Application() {
	 override fun onCreate() {
		  super.onCreate()

		  if (BuildConfig.DEBUG)
			   Timber.plant(LineNumberDebugTree())
	 }
}

class LineNumberDebugTree : Timber.DebugTree() {
	 override fun createStackElementTag(element: StackTraceElement): String? {
		  return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
	 }
}