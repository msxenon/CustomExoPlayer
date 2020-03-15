package com.appchief.msa

import android.os.Bundle
import android.util.Log
import com.appchief.msa.awesomeplayer.R
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : BaseActivityFloatingNavigation() {
	 companion object {
		  var link = ""
		  var poster = ""
		  var movieName = ""
		  var isChannel = true
	 }


	 override fun onCreate(savedInstanceState: Bundle?) {
		  Timber.plant(Timber.DebugTree())
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.activity_main)
		  Timber.e("kkmkmk ")
		  button4?.setOnClickListener {
			   isChannel = false
			   link =
					"http://appchief.net/bigBunny.mp4"//"https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"//"http://tv.supercellnetwork.com:1935/bein1/1/playlist.m3u8"
			   movieName = "Tiktok"
			   poster =
					"https://kaboompics.com/cache/b/2/8/8/3/b2883703308df69a2c024a1eacae859cbf227364.jpeg"
			   removeIfExist()
		  }
		  button3?.setOnClickListener {
			   isChannel = true
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
