package com.appchief.msa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appchief.msa.awesomeplayer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	 override fun onCreate(savedInstanceState: Bundle?) {
		  super.onCreate(savedInstanceState)
		  setContentView(R.layout.activity_main)
		  button3?.setOnClickListener {
			   supportFragmentManager.beginTransaction().replace(R.id.you, MainActivityFragment())
					.commit()
		  }
	 }
}
