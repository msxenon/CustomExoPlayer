package com.appchief.msa.exoplayerawesome

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fullscreenplayer.*

class FullScreenPlayer : AppCompatActivity(), FullScreenActivity {


	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.fullscreenplayer)
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
		  playerViewFull.applySettings(playerViewFull.player)
	 }

	 override fun onDestroy() {
		  ExoIntent.savePlayer(playerViewFull, true)
		  super.onDestroy()
	 }

	 override fun ondissmiss() {
		  onBackPressed()
	 }
//	 @SuppressLint("MissingSuperCall")
//	 override fun onConfigurationChanged(newConfig: Configuration) {
//		 // super.onConfigurationChanged(newConfig)
//		  val display =
//			   (getSystemService(WINDOW_SERVICE) as? WindowManager?)?.defaultDisplay
//		  val orientation = display?.rotation
//		  when(orientation){
//			   3->fullScreenPlayer?.animate()?.rotation(-90f)?.start()
//			   else->fullScreenPlayer?.animate()?.rotation(90f)?.start()
//		  }
//		  oldOrentation = orientation?:1
//	 }
}