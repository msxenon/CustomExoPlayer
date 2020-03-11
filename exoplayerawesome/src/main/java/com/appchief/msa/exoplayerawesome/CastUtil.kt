package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.android.exoplayer2.ext.cast.MediaItem
import com.google.android.gms.cast.*
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.images.WebImage

object CastUtil {
	 private val PRELOAD_TIME_S = 20
	 fun getMediaArray(mediaInfo: MediaInfo): Array<MediaQueueItem> {
		  val queueItem = MediaQueueItem.Builder(mediaInfo).setAutoplay(
			   true
		  ).setPreloadTime(PRELOAD_TIME_S.toDouble()).build()
		  return arrayOf(queueItem)
	 }

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

		  remoteMediaClient.queueLoad(
			   getMediaArray(mediaInfo), 0,
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
		  val englishSubtitle = MediaTrack.Builder(
			   1 /* ID */,
			   MediaTrack.TYPE_TEXT
		  )
			   .setName("English Subtitle")
			   .setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
			   .setContentId(subtitle)
			   /* language is required for subtitle type but optional otherwise */
			   .setLanguage("en-US")
			   .build()
		  return MediaInfo.Builder(videoUrl)
			   .setMediaTracks(listOf(englishSubtitle))
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
	 ): Array<MediaQueueItem> {
		  val m = loadRemoteMedia(
			   if (isChannel) MediaInfo.STREAM_TYPE_LIVE else MediaInfo.STREAM_TYPE_BUFFERED,
			   videoUrl,
			   title,
			   geners ?: "",
			   poster,
			   runtime
		  )
		  // sendToConnectedTV(context, m, position ?: 0L)
		  return getMediaArray(m)
	 }

	 fun castThisMI(
		  context: Context,
		  title: String?,
		  videoUrl: String?,
		  geners: String?,
		  poster: String,
		  runtime: Long?,
		  isChannel: Boolean,
		  position: Long?
	 ): Pair<MediaItem, Array<MediaQueueItem>> {
		  val m = loadRemoteMedia(
			   if (isChannel) MediaInfo.STREAM_TYPE_LIVE else MediaInfo.STREAM_TYPE_BUFFERED,
			   videoUrl,
			   title,
			   geners ?: "",
			   poster,
			   runtime
		  )
		  val i = MediaItem.Builder().setUri(videoUrl!!).setMimeType(getMimeType(url = videoUrl))
			   .setTitle(title ?: "X").build()

		  return Pair(i, getMediaArray(m))
	 }
	 fun getMimeType(url: String): String {
		  var type: String = "videos/mp4"
		  val extension = MimeTypeMap.getFileExtensionFromUrl(url)
		  if (extension != null) {
			   if (extension.contains("m3u"))
					type = "application/vnd.apple.mpegurl"
			   else
					try {
						 type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: type
					} catch (e: Exception) {
						 e.printStackTrace()
					}
		  }
		  Log.e("mimeType", "type $type $extension")
		  return type
	 }
}