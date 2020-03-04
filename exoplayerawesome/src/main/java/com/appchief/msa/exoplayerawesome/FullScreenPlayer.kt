package com.appchief.msa.exoplayerawesome

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fullscreenplayer.*

class FullScreenPlayer : AppCompatActivity(), FullScreenActivity {


	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.fullscreenplayer)
//		  val m = supportFragmentManager?.findFragmentByTag("ff")
//		val x = ( m as? FloatingPLayerFragment)?.binding?.videoOverlayView?.player
//		  Log.e("ff","${m != null} ${x != null}")


	 }

	 override fun onPause() {
		  ExoIntent.onPause(playerViewFull)
		  super.onPause()
	 }

	 override fun onResume() {
		  super.onResume()
		  window?.decorView?.apply {
			   // Hide both the navigation bar and the status bar.
			   // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
			   // a general rule, you should design your app to hide the status bar whenever you
			   // hide the navigation bar.
			   systemUiVisibility =
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		  }
		  ExoIntent.getPlayerHere(playerViewFull, this)
//		  playerViewFull.applySettings(playerViewFull.player)
	 }

	 override fun ondissmiss() {
		  onBackPressed()
	 }

	 override fun onBackPressed() {
		  ExoIntent.savePlayer(playerViewFull, true)
		  this.finish()
	 }
}

fun View?.removeSelf() {
	 this ?: return
	 val parent = parent as? ViewGroup ?: return
	 parent.removeView(this)
}