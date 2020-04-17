package com.appchief.msa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appchief.msa.awesomeplayer.R

class TVPlayer : AppCompatActivity() {
	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.lpayerortv)
		  val m = MainActivityFragment()
		  supportFragmentManager.beginTransaction()
			   .replace(R.id.playercontainertv, m, "ff")
			   .commit()
	 }
}