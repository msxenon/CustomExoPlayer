package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import java.io.File

object ExoFactorySingeleton {
	 private lateinit var value: ExoFactory
	 fun init(context: Context?) {
		  value = ExoFactory(context)
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

	 fun buildMediaSource(uri: Uri): MediaSource? {
		  Log.e("exoFactory", "buildMediaSource $uri")
		  try {//here put url
			   @C.ContentType val type = Util.inferContentType(uri, null)
			   return when (type) {
					C.TYPE_DASH -> DashMediaSource.Factory(buildDataSourceFactory()!!).createMediaSource(
						 uri
					)
					C.TYPE_SS -> SsMediaSource.Factory(buildDataSourceFactory()!!).createMediaSource(
						 uri
					)
					C.TYPE_HLS -> HlsMediaSource.Factory(buildDataSourceFactory()!!).createMediaSource(
						 uri
					)
					C.TYPE_OTHER -> ProgressiveMediaSource.Factory(buildDataSourceFactory()!!).createMediaSource(
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
	 fun buildDataSourceFactory(): DataSource.Factory? {
		  val upstreamFactory = DefaultDataSourceFactory(context, buildHttpDataSourceFactory())

		  return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache())
	 }

	 val twoMins = 2 * 60 * 1000
	 /** Returns a [HttpDataSource.Factory].  */
	 fun buildHttpDataSourceFactory(): HttpDataSource.Factory {
		  return DefaultHttpDataSourceFactory(userAgent, twoMins, twoMins, true)
	 }

	 @Synchronized
	 protected fun getDownloadCache(): com.google.android.exoplayer2.upstream.cache.Cache? {
		  if (downloadCache == null) {
			   val downloadContentDirectory =
					File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
			   downloadCache =
					SimpleCache(downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider())
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
		  return CacheDataSourceFactory(
			   cache,
			   upstreamFactory,
			   FileDataSource.Factory(),
			   /* eventListener= */ null,
			   CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null
		  )
	 }
}