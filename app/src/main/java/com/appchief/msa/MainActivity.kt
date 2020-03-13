package com.appchief.msa

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.appchief.msa.awesomeplayer.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {
	 companion object {
		  var link = ""
		  var poster = ""
		  var movieName = ""
	 }

	 private fun isGooglePlayServicesAvailable(): Boolean {
		  val googleApiAvailability = GoogleApiAvailability.getInstance()
		  val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
		  if (resultCode != ConnectionResult.SUCCESS) {
			   val dialog: Dialog? = googleApiAvailability.getErrorDialog(this, resultCode, 0)
			   if (dialog != null) {
					dialog.show()
			   }
			   return false
		  }
		  return true
	 }



	 override fun onCreate(savedInstanceState: Bundle?) {
		  Timber.plant(Timber.DebugTree())
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.activity_main)
		  Timber.e("kkmkmk ")
		  isGooglePlayServicesAvailable()
		  button4?.setOnClickListener {
			   link =
					"http://appchief.net/bigBunny.mp4"//"https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"//"http://tv.supercellnetwork.com:1935/bein1/1/playlist.m3u8"
			   movieName = "Tiktok"
			   poster =
					"https://kaboompics.com/cache/b/2/8/8/3/b2883703308df69a2c024a1eacae859cbf227364.jpeg"
			   removeIfExist()
		  }
		  button3?.setOnClickListener {
			   link =
					"http://tv.supercellnetwork.com:1935/bein1/1/playlist.m3u8"//"http://cinema.supercellnetwork.com:8081/vod/f1a21737-0a5c-4dec-8bca-7bd4b431cb26/NxSxsu0xvAzvrqo/,NxSxsu0xvAzvrqo_1080.mp4,NxSxsu0xvAzvrqo_720.mp4,NxSxsu0xvAzvrqo_480.mp4,NxSxsu0xvAzvrqo.srt,.urlset/master.m3u8"//
			   movieName = "TigBunny"
			   poster =
					"https://kaboompics.com/cache/c/b/2/1/f/cb21f5aca64890d7d13ce9e8387c23c6883e71e9.jpeg"
			   removeIfExist()
		  }
	 }

	 fun removeIfExist() {

		  val x = supportFragmentManager.findFragmentById(R.id.you)
		  x?.let {
			   supportFragmentManager.beginTransaction().remove(x).runOnCommit {
					showFrag()
			   }.commitNow()
			   Log.e("main", "old frag removed")
		  } ?: kotlin.run {
			   showFrag()
		  }
	 }

	 fun showFrag() {
		  val m = MainActivityFragment()
		  supportFragmentManager.beginTransaction()
			   .replace(R.id.you, m, "ff")
			   .commit()
	 }
}
