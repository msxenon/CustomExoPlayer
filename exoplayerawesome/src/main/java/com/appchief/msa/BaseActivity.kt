package com.appchief.msa

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.cast.framework.*

abstract class BaseActivity : AppCompatActivity() {
	 init {
		  AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
	 }

	 abstract fun isCastConnected(isConnected: Boolean)

	 abstract fun isCastAvailable(isAvailable: Boolean)
	 private val mCastContext: CastContext by lazy { CastContext.getSharedInstance(this) }
	 private var mCastSession: CastSession? = null
	 private val mSessionManagerListener = object : SessionManagerListener<CastSession> {
		  override fun onSessionStarted(p0: CastSession?, p1: String?) {
			   Log.e("BaseActivity", "onSessionStarted  ")
			   isCastConnected(true)

			   mCastSession = p0
		  }

		  override fun onSessionResumeFailed(p0: CastSession?, p1: Int) {
			   Log.e("BaseActivity", "onSessionResumeFailed  ")
		  }

		  override fun onSessionSuspended(p0: CastSession?, p1: Int) {
			   Log.e("BaseActivity", "onSessionSuspended  ")
		  }

		  override fun onSessionEnded(session: CastSession?, p1: Int) {
			   Log.e("BaseActivity", "onSessionEnded  ")
			   isCastConnected(false)

			   if (session === mCastSession) {
					mCastSession = null
			   }
		  }

		  override fun onSessionResumed(p0: CastSession?, p1: Boolean) {
			   Log.e("BaseActivity", "onSessionResumed  ")
			   isCastConnected(true)
			   mCastSession = p0
		  }

		  override fun onSessionStarting(p0: CastSession?) {
			   Log.e("BaseActivity", "onSessionStarting  ")
		  }

		  override fun onSessionResuming(p0: CastSession?, p1: String?) {
			   Log.e("BaseActivity", "onSessionResuming  ")
		  }

		  override fun onSessionEnding(p0: CastSession?) {
			   Log.e("BaseActivity", "onSessionEnding  ")
		  }

		  override fun onSessionStartFailed(p0: CastSession?, p1: Int) {
			   Log.e("BaseActivity", "onSessionStartFailed  ")
		  }
	 }

	 fun isCastConnected(): Boolean {
		  return (mCastSession != null && mCastSession!!.isConnected)
	 }

	 @SuppressLint("SourceLockedOrientationActivity")
	 override fun onBackPressed() {
		  val orientation = this.resources.configuration.orientation
		  if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			   // code for portrait mode
			   super.onBackPressed()
		  } else {
			   // code for landscape mode
			   requestedOrientation =
					ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		  }
	 }

	 private val castStateListener =
		  CastStateListener { p0 -> isCastAvailable(p0 != CastState.NO_DEVICES_AVAILABLE) }
}