package com.appchief.msa

import android.os.Bundle
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity
import java.util.*

class ExpandedControlsActivity : ExpandedControllerActivity() {
	 override fun onCreate(p0: Bundle?) {
		  super.onCreate(p0)
		  val configuration = resources.configuration
		  configuration.setLayoutDirection(Locale("en"))
		  configuration.setLocale(Locale("en"))
		  createConfigurationContext(configuration)
	 }
}