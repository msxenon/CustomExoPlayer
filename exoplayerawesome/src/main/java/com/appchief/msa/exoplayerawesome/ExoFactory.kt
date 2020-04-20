package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import java.io.File

object ExoFactorySingeleton {
	 private lateinit var value: ExoFactory
	 var isTv = false
	 fun init(context: Context?, istv: Boolean = false) {
		  value = ExoFactory(context)
		  try {
			   isTv = istv || Util.isTv(context!!)
		  } catch (e: Exception) {
		  }
	 }

	 fun getInstance(): ExoFactory {
		  return value
	 }
}

class ExoFactory internal constructor(private val context: Context?) {
	 private val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
	 private lateinit var userAgent: String
	 private var databaseProvider: DatabaseProvider? = null
	 private var downloadDirectory: File? = null
	 private var downloadCache: com.google.android.exoplayer2.upstream.cache.Cache? = null

	 init {
		  if (context != null)
			   userAgent = Util.getUserAgent(context, "ExoPlayerDemo")
	 }

	 fun buildMediaSource(uri: Uri, srtLink: String?, noChache: Boolean): MediaSource? {
		  return if (!srtLink.isNullOrBlank()) {
			   addSubTitlesToMediaSource(
					ExoFactorySingeleton.getInstance().buildMediaSource(uri, noChache),
					srtLink.encodeUrl(),
					noChache
			   )
		  } else {
			   ExoFactorySingeleton.getInstance().buildMediaSource(uri, noChache)
		  }
	 }

	 private fun addSubTitlesToMediaSource(
		  mediaSource: MediaSource?,
		  subTitlesUrl: String,
		  noChache: Boolean
	 ): MediaSource {
		  val textFormat = Format.createTextSampleFormat(
			   null, MimeTypes.APPLICATION_SUBRIP,
			   null, Format.NO_VALUE, Format.NO_VALUE, "en", null, Format.OFFSET_SAMPLE_RELATIVE
		  )
		  val uri = Uri.parse(subTitlesUrl)
		  Log.e("subtitleURI", uri.toString() + " ")
		  val subtitleSource =
			   SingleSampleMediaSource.Factory(
					ExoFactorySingeleton.getInstance().buildDataSourceFactory(
						 noChache
					)
			   )
					.createMediaSource(uri, textFormat, C.TIME_UNSET)

		  return MergingMediaSource(mediaSource, subtitleSource)
	 }

	 fun buildMediaSource(uri: Uri, noChache: Boolean): MediaSource? {
		  try {//here put url
			   @C.ContentType val type = Util.inferContentType(uri, null)
			   Log.e("exoFactory", "buildMediaSource $uri $type")

			   return when (type) {
					C.TYPE_DASH -> DashMediaSource.Factory(buildDataSourceFactory(noChache)!!).createMediaSource(
						 uri
					)
					C.TYPE_SS -> SsMediaSource.Factory(buildDataSourceFactory(noChache)!!).createMediaSource(
						 uri
					)
					C.TYPE_HLS -> {
						 val m = HlsMediaSource.Factory(buildDataSourceFactory(noChache)!!)
							  .setAllowChunklessPreparation(true)
						 m.createMediaSource(
						 uri
					)
					}
					C.TYPE_OTHER -> ProgressiveMediaSource.Factory(buildDataSourceFactory(noChache)!!).createMediaSource(
						 uri
					)
					else -> throw Throwable("Unsupported type: $type")
			   }

		  } catch (e: Exception) {
			   e.printStackTrace()
		  }
		  return null
	 }

	 /** Returns a [DataSource.Factory].  */
	 fun buildDataSourceFactory(noChache: Boolean): DataSource.Factory? {
		  val upstreamFactory = DefaultDataSourceFactory(context, buildHttpDataSourceFactory())
		  return if (noChache) {
			   upstreamFactory
		  } else
			   buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache(noChache))
	 }

	 val twoMins = 8000
	 /** Returns a [HttpDataSource.Factory].  */
	 fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
		  return DefaultHttpDataSourceFactory(userAgent, twoMins, twoMins, true)
	 }

	 val sizeCache = 100 * 1024 * 1024
	 @Synchronized
	 protected fun getDownloadCache(noChache: Boolean): com.google.android.exoplayer2.upstream.cache.Cache? {
		  if (downloadCache == null) {
			   val downloadContentDirectory =
					File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
			   downloadCache =
					SimpleCache(
						 downloadContentDirectory,
						 LeastRecentlyUsedCacheEvictor(sizeCache.toLong()),
						 getDatabaseProvider()
					)
		  }
		  return downloadCache
	 }

	 private fun getDatabaseProvider(): DatabaseProvider? {
		  context?.let {
			   return ExoDatabaseProvider(context)
		  }


		  return null
	 }

	 private fun getDownloadDirectory(): File? {
		  if (downloadDirectory == null) {
			   downloadDirectory = context?.getExternalFilesDir(null)
			   if (downloadDirectory == null) {
					downloadDirectory = context?.filesDir
			   }
		  }
		  return downloadDirectory
	 }

	 protected fun buildReadOnlyCacheDataSource(
		  upstreamFactory: DataSource.Factory,
		  cache: com.google.android.exoplayer2.upstream.cache.Cache?
	 ): CacheDataSourceFactory? {
		  val cacheFlags =
			   CacheDataSource.FLAG_BLOCK_ON_CACHE

		  return CacheDataSourceFactory(
			   cache,
			   upstreamFactory, cacheFlags
//			   FileDataSource.Factory(),
//			   /* eventListener= */ null,
//			   CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
		  )
	 }
}