package com.appchief.msa.exoplayerawesome

 import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
object SettingsUtil {
	 /**
	  * Returns whether a track selection dialog will have content to display if initialized with the
	  * specified [DefaultTrackSelector] in its current state.
	  */
	 fun willHaveContent(trackSelector: DefaultTrackSelector?): Boolean {
		  val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
		  return mappedTrackInfo != null && willHaveContent(mappedTrackInfo)
	 }

	 /**
	  * Returns whether a track selection dialog will have content to display if initialized with the
	  * specified [MappedTrackInfo].
	  */
	 fun willHaveContent(mappedTrackInfo: MappedTrackInfo): Boolean {
		  for (i in 0 until mappedTrackInfo.rendererCount) {
			   if (showTabForRenderer(mappedTrackInfo, i)) {
					return true
			   }
		  }
		  return false
	 }

	 private fun showTabForRenderer(
		  mappedTrackInfo: MappedTrackInfo,
		  rendererIndex: Int
	 ): Boolean {
		  val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
		  if (trackGroupArray.length == 0) {
			   return false
		  }
		  val trackType = mappedTrackInfo.getRendererType(rendererIndex)
		  return isSupportedTrackType(trackType)
	 }

	 private fun isSupportedTrackType(trackType: Int): Boolean {
		  when (trackType) {
			   C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT -> return true
			   else -> return false
		  }
	 }


}

interface SettingsListener{
	 fun hasSettings(has:Boolean)
}