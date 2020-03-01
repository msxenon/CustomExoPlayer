package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage

object CastUtil {
	 private val PRELOAD_TIME_S = 20
	 private fun sendToConnectedTV(context: Context, mediaInfo: MediaInfo, position: Long) {
		  val castSession = CastContext.getSharedInstance(context).sessionManager.currentCastSession
		  if (castSession == null || !castSession.isConnected) {
			   return
		  }
		  val remoteMediaClient = castSession.remoteMediaClient
		  if (remoteMediaClient == null) {
			   Log.w("TAG", "showQueuePopup(): null RemoteMediaClient")
			   return
		  }
		  val queueItem = MediaQueueItem.Builder(mediaInfo).setAutoplay(
			   true
		  ).setPreloadTime(PRELOAD_TIME_S.toDouble()).build()
		  val newItemArray = arrayOf(queueItem)
		  remoteMediaClient.queueLoad(
			   newItemArray, 0,
			   MediaStatus.REPEAT_MODE_REPEAT_OFF, position, null
		  )
	 }

	 private fun buildMediaInfo4Movie(
		  streamType: Int,
		  videoUrl: String,
		  title: String,
		  subtitle: String,
		  poster: String,
		  duration: Long
	 ): MediaInfo {
		  val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC)
		  movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subtitle)
		  movieMetadata.putString(MediaMetadata.KEY_TITLE, title)

		  movieMetadata.addImage(WebImage(Uri.parse(poster)))
		  val sd = duration * 60 * 1000
		  return MediaInfo.Builder(videoUrl)
//			   .setMediaTracks(listOf(subtitleMT))
			   .setStreamType(streamType)
			   .setContentType(getMimeType(videoUrl))
			   .setMetadata(movieMetadata)
			   .setStreamDuration(sd)
			   .build()
	 }

	 private fun loadRemoteMedia(
		  streamType: Int,
		  videoUrl: String?,
		  title: String?,
		  subtitle: String?,
		  poster: String?,
		  duration: Long?,
		  position: Long = 0L
	 ): MediaInfo {
		  val m = buildMediaInfo4Movie(
			   streamType,
			   videoUrl ?: "",
			   title ?: "",
			   subtitle ?: "",
			   poster ?: "",
			   duration ?: 0
		  )
		  return m
	 }

	 fun castThis(
		  context: Context,
		  title: String?,
		  videoUrl: String?,
		  geners: String?,
		  poster: String,
		  runtime: Long?,
		  isChannel: Boolean,
		  position: Long?
	 ) {
		  val m = loadRemoteMedia(
			   if (isChannel) MediaInfo.STREAM_TYPE_LIVE else MediaInfo.STREAM_TYPE_BUFFERED,
			   videoUrl,
			   title,
			   geners ?: "",
			   poster,
			   runtime
		  )
		  sendToConnectedTV(context, m, position ?: 0L)
	 }

	 fun getMimeType(url: String): String {
		  var type: String = "videos/mp4"
		  val extension = MimeTypeMap.getFileExtensionFromUrl(url)
		  if (extension != null) {
			   try {
					type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: type
			   } catch (e: Exception) {
					e.printStackTrace()
			   }
		  }
		  return type
	 }
}