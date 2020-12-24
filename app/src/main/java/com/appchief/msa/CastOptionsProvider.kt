package com.appchief.msa

import android.content.Context
import androidx.core.net.toUri
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.*
import com.google.android.gms.common.images.WebImage
import java.util.*

class CastOptionsProvider : OptionsProvider {
	 override fun getCastOptions(context: Context): CastOptions {
		  val notificationOptions = NotificationOptions.Builder()
			   .setActions(
					Arrays.asList(
						 MediaIntentReceiver.ACTION_SKIP_NEXT,
						 MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
						 MediaIntentReceiver.ACTION_STOP_CASTING
					), intArrayOf(1, 2)
			   )
			   .setTargetActivityClassName(ExpandedControlsActivity::class.java.name)
			   .build()
		  val mediaOptions = CastMediaOptions.Builder()
			   .setImagePicker(ImagePickerImpl())
			   .setNotificationOptions(notificationOptions)
			   .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java.name)
			   .build()
		  val v =
			   LaunchOptions.Builder()//.setLocale(Locale("en"))
					.build()
		  return CastOptions.Builder()
			   .setLaunchOptions(v)
			   .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
			   .setCastMediaOptions(mediaOptions)
			   .build()
	 }

	 override fun getAdditionalSessionProviders(context: Context): List<SessionProvider>? {
		  return null
	 }

	 private class ImagePickerImpl : ImagePicker() {
		  override fun onPickImage(p0: MediaMetadata?, p1: ImageHints): WebImage {
			   return super.onPickImage(p0, p1)
		  }
		  override fun onPickImage(
			   mediaMetadata: MediaMetadata,
			   type: Int
		  ): WebImage {

			   if (!mediaMetadata.hasImages()) {
					return WebImage("".toUri())
			   }
			   val images = mediaMetadata.images

			   return if (images.size == 1) {
					images[0]
			   } else {
					if (type == IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
						 images[0]
					} else {
						 images[0]
					}
			   }
		  }
	 }
}