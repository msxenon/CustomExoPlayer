package com.appchief.msa

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import com.appchief.msa.Common.Companion.floatingPLayerFragmentNameTag
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton
import com.appchief.msa.floating_player.FloatingPLayerFragment


open class BaseActivityFloatingNavigation : AppCompatActivity() {
	 open fun getPlayerFragment(): FloatingPLayerFragment? {
		 return supportFragmentManager.findFragmentByTag(floatingPLayerFragmentNameTag) as? FloatingPLayerFragment
	 }

	 @SuppressLint("SourceLockedOrientationActivity")
	 fun canGoBack(): Boolean {
		  if (ExoFactorySingeleton.isTv)
			   return true
		  val orientation = this.resources.configuration.orientation
		  if (orientation != Configuration.ORIENTATION_PORTRAIT) {
			   // code for landscape mode
			   requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
			   return false
		  }
		  val pc = getPlayerFragment()

		  pc?.takeIf { pc.isVisible }?.let {
			   val canGoBack = it.canGoBack()
			   if (!canGoBack) {
					pc.doMinimizePlayer()
					return false
			   }
		  }

		  return goUpFragments()
	 }

	 open fun goUpFragments(): Boolean {
		  return true
	 }

	 override fun onBackPressed() {
		  if (canGoBack())
			   super.onBackPressed()
	 }
}