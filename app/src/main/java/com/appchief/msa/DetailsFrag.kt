package com.appchief.msa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.appchief.msa.awesomeplayer.R
import kotlinx.android.synthetic.main.details_view.*

class DetailsFrag : Fragment() {
	 override fun onCreateView(
		  inflater: LayoutInflater,
		  container: ViewGroup?,
		  savedInstanceState: Bundle?
	 ): View? {
		  return inflater.inflate(R.layout.details_view, container, false)
	 }

	 override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		  super.onViewCreated(view, savedInstanceState)
		  button2?.setOnClickListener {
			   MainActivity.movieName = "https"
			   MainActivity.link =
					"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
			   val m =
					activity?.supportFragmentManager?.findFragmentByTag("ff") as? MainActivityFragment
			   m!!.initPlayer()
		  }
		  button6?.setOnClickListener {
			   MainActivity.movieName = "https"
			   MainActivity.link =
					"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
			   val m =
					activity?.supportFragmentManager?.findFragmentByTag("ff") as? MainActivityFragment
			   m!!.initPlayer()
		  }
		  button7?.setOnClickListener {
			   MainActivity.movieName = "https"
			   MainActivity.link =
					"https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4"
			   val m =
					activity?.supportFragmentManager?.findFragmentByTag("ff") as? MainActivityFragment
			   m!!.initPlayer()
		  }
	 }
}