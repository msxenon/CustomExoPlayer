package com.appchief.msa

import android.os.Bundle
import com.appchief.msa.awesomeplayer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {
	 override fun isCastConnected(isConnected: Boolean) {
	 }

	 override fun isCastAvailable(isAvailable: Boolean) {
	 }

	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.activity_main)
		  button3?.setOnClickListener {
			   supportFragmentManager.beginTransaction().replace(R.id.you, MainActivityFragment())
					.commit()
		  }
	 }
}
