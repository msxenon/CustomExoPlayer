package com.appchief.msa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.appchief.msa.activities.GoogleServicesWarningActivity
import com.appchief.msa.awesomeplayer.R
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : BaseActivityFloatingNavigation() {
	companion object {
		var link = ""
		var poster = ""
		var movieName = ""
		var isChannel = true
	}

	val videos = listOf<String>(
		"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" //long video 9 mins,
		,
		"https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4" //10 sec
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		Timber.plant(Timber.DebugTree())
		super.onCreate(savedInstanceState)
		if (ExoFactorySingeleton.servicesNeedsToBeInstalled) {
			startActivity(Intent(this, GoogleServicesWarningActivity::class.java))
		}
		setContentView(R.layout.activity_main)

		button4?.setOnClickListener {
			isChannel = false
			link = videos[0]
			movieName = "Tiktok"
             poster =
				 "https://kaboompics.com/cache/b/2/8/8/3/b2883703308df69a2c024a1eacae859cbf227364.jpeg"
			 removeIfExist()
		 }
		 google_play?.setOnClickListener {
			 isChannel = false
			 link = videos[0]
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
		  if (true) {
			   startActivity(Intent(this, TVPlayer::class.java))
			   return
		  }
		  val m = MainActivityFragment()
		  supportFragmentManager.beginTransaction()
			   .replace(R.id.you, m, "ff")
			   .commit()
	 }
}
