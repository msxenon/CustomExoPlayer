package com.appchief.msa

import android.content.Intent
import android.os.Bundle
import com.appchief.msa.Common.Companion.floatingPLayerFragmentNameTag
import com.appchief.msa.activities.GoogleServicesWarningActivity
import com.appchief.msa.awesomeplayer.R
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton
import com.appchief.msa.exoplayerawesome.NowPlaying
import com.appchief.msa.exoplayerawesome.PlayerType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivityFloatingNavigation() {

	val videos = listOf<String>(
		"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" //long video 9 mins,
		,
		"https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/360/Big_Buck_Bunny_360_10s_1MB.mp4" //10 sec
	)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (ExoFactorySingeleton.servicesNeedsToBeInstalled) {
			startActivity(Intent(this, GoogleServicesWarningActivity::class.java))
		}
		setContentView(R.layout.activity_main)

		video1?.setOnClickListener {


			navigateToPLayer(
				NowPlaying(
					82828,
					type = PlayerType.MOVIE,
					poster = "https://kaboompics.com/cache/b/2/8/8/3/b2883703308df69a2c024a1eacae859cbf227364.jpeg",
					videoLink = videos[0],
					title = "Big Buck Bunny",
					srtLink = TestVars.srt,
					runtime = 10000,
					geners = "",
					episode = null,
					vttLink = TestVars.testVtt
				)
			)
		}
		video2?.setOnClickListener {
			navigateToPLayer(
				NowPlaying(
					82828,
					type = PlayerType.MOVIE,
					poster = "https://kaboompics.com/cache/c/b/2/1/f/cb21f5aca64890d7d13ce9e8387c23c6883e71e9.jpeg",
					videoLink = videos[1],
					title = "Big Buck Bunny",
					srtLink = TestVars.srt,
					runtime = 10000,
					geners = "",
					episode = null,
					vttLink = TestVars.testVtt
				)
			)
		}
	}

	fun navigateToPLayer(nowPlaying: NowPlaying) {
		val x = supportFragmentManager.findFragmentById(R.id.you)
		x?.let {
			supportFragmentManager.beginTransaction().remove(x).runOnCommit {
				showFrag(nowPlaying)
			}.commitNow()
		} ?: kotlin.run {
			showFrag(nowPlaying)
		}
	}

	fun showFrag(nowPlaying: NowPlaying) {
		val m = PLayerFragment(nowPlaying)
		supportFragmentManager.beginTransaction()
			.replace(R.id.you, m, floatingPLayerFragmentNameTag)
			.commit()
	}
}
