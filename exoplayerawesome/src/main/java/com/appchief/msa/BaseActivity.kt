package com.appchief.msa

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.cast.framework.*

interface CastListener {
	 fun isCastConnected(isConnected: Boolean)
	 fun isCastAvailable(isAvailable: Boolean)
}

abstract class CastApp : Application() {
	 init {
		  AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
	 }

	 val listeners = mutableListOf<CastListener>()
	 val mCastContext: CastContext by lazy { CastContext.getSharedInstance(this) }
	 private var mCastSession: CastSession? = null
	 override fun onCreate() {
		  super.onCreate()
		  try {
			   if (mCastSession == null) {
					mCastSession = CastContext.getSharedInstance(this).sessionManager
						 .currentCastSession
			   }
			   mCastContext.sessionManager.addSessionManagerListener(
					mSessionManagerListener, CastSession::class.java
			   )
			   mCastContext.addCastStateListener(castStateListener)
		  } catch (e: Exception) {
		  }
	 }

	 val mSessionManagerListener = object : SessionManagerListener<CastSession> {
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

	 fun isCastConnected(boolean: Boolean) {
		  listeners.map { it.isCastConnected(boolean) }
	 }
	 fun isCastConnected(): Boolean {
		  return (mCastSession != null && mCastSession!!.isConnected)
	 }



	 private val castStateListener =
		  CastStateListener { p0 -> listeners.map { it.isCastAvailable(p0 != CastState.NO_DEVICES_AVAILABLE) } }
}
