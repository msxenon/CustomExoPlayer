package com.appchief.msa

import android.os.Bundle
import android.util.Log
import com.appchief.msa.awesomeplayer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
	 companion object {
		  var link = "http://tv.supercellnetwork.com:1935/bein1/1/playlist.m3u8"
	 }
	 override fun isCastConnected(isConnected: Boolean) {
	 }

	 override fun isCastAvailable(isAvailable: Boolean) {
	 }

	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.activity_main)
		  button4?.setOnClickListener {
			   link = "http://tv.supercellnetwork.com:1935/bein1/1/playlist.m3u8"
			   removeIfExist()
		  }
		  button3?.setOnClickListener {
			   link = "http://tv.supercellnetwork.com:1935/nile3/mbc3.stream_360p/playlist.m3u8"
			   removeIfExist()
		  }
	 }

	 fun removeIfExist() {
		  val m = MainActivityFragment()
		  val x = supportFragmentManager.findFragmentById(R.id.you)
		  x?.let {
			   supportFragmentManager.beginTransaction().remove(x).commitNowAllowingStateLoss()
			   Log.e("main", "old frag removed")
		  }
		  supportFragmentManager.beginTransaction()
			   .add(R.id.you, m, "ff")
			   .commit()
	 }
}
