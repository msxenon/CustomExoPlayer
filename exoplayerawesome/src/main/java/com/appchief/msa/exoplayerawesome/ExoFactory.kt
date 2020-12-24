package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
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
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.security.ProviderInstaller
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.net.ssl.SSLContext

object ExoFactorySingeleton {
	private lateinit var value: ExoFactory
	var servicesNeedsToBeInstalled = false
	var isTv = false

	fun init(context: Context, istv: Boolean = false) {
		GlobalScope.launch {
			withContext(Dispatchers.IO) {
				try {
					ProviderInstaller.installIfNeeded(context)
					val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
					sslContext.init(null, null, null)
					sslContext.createSSLEngine()
				} catch (e: Exception) {
					servicesNeedsToBeInstalled = true
					e.printStackTrace()
				}

			}
		}

		value = ExoFactory(context)
		try {
			isTv = istv || Util.isTv(context)
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	fun getInstance(): ExoFactory {
		return value
	}
}

class ExoFactory internal constructor(private val context: Context) {
	private val DOWNLOAD_CONTENT_DIRECTORY = "downloads"
	private lateinit var userAgent: String
	private var databaseProvider: DatabaseProvider? = null
	private var downloadDirectory: File? = null
	private var downloadCache: Cache? = null

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





	fun buildMediaSource(uri: Uri, noChache: Boolean): MediaSource? {
		try {//here put url
			@C.ContentType val type = Util.inferContentType(uri, null)
			Log.e("exoFactory", "buildMediaSource $uri $type")

			return when (type) {
				C.TYPE_DASH -> DashMediaSource.Factory(buildDataSourceFactory(noChache))
					.createMediaSource(
						MediaItem.fromUri(uri)
					)
				C.TYPE_SS -> SsMediaSource.Factory(buildDataSourceFactory(noChache))
					.createMediaSource(
						MediaItem.fromUri(uri)
					)
				C.TYPE_HLS -> {
					val m = HlsMediaSource.Factory(buildDataSourceFactory(noChache))
						.setAllowChunklessPreparation(true)
					m.createMediaSource(
						MediaItem.fromUri(uri)
					)
				}
				C.TYPE_OTHER -> {
					ProgressiveMediaSource.Factory(buildDataSourceFactory(noChache))
						.createMediaSource(
							MediaItem.fromUri(uri)
						)

				}
                   else -> throw Throwable("Unsupported type: $type")
               }

		  } catch (e: Exception) {
			   e.printStackTrace()
		  }
		  return null
	 }

	/** Returns a [DataSource.Factory].  */
	fun buildDataSourceFactory(noChache: Boolean): DataSource.Factory {
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
	protected fun getDownloadCache(noChache: Boolean): Cache {
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
		return downloadCache as Cache
	}

	private fun getDatabaseProvider(): DatabaseProvider {
		context.let {
			return ExoDatabaseProvider(context)
		}


	}

	private fun getDownloadDirectory(): File? {
		if (downloadDirectory == null) {
			downloadDirectory = context.getExternalFilesDir(null)
			if (downloadDirectory == null) {
				downloadDirectory = context.filesDir
			}
		}
		  return downloadDirectory
	 }

	protected fun buildReadOnlyCacheDataSource(
		upstreamFactory: DataSource.Factory,
		cache: Cache?
	): DataSource.Factory {
		val cacheFlags =
			CacheDataSource.FLAG_BLOCK_ON_CACHE
		return CacheDataSource.Factory().setUpstreamDataSourceFactory(upstreamFactory)
			.setCache(cache!!).setFlags(cacheFlags)
	}

	companion object {
		fun addSubTitlesToMediaSource(
			mediaSource: MediaSource?,
			subTitlesUrl: String,
			noChache: Boolean
		): MediaSource {
			val uri = Uri.parse(subTitlesUrl)
			Log.e("subtitleURI", uri.toString() + " ")
			val subtitleSource =
				SingleSampleMediaSource.Factory(
					ExoFactorySingeleton.getInstance().buildDataSourceFactory(
						noChache
					)
				)
					.createMediaSource(getSubtitle(uri), C.TIME_UNSET)

			return MergingMediaSource(mediaSource!!, subtitleSource)
		}

		fun getSubtitle(uri: Uri): MediaItem.Subtitle {
			return MediaItem.Subtitle(
				uri,
				getSubtitleMimeType(uri.path),
				"en",
				C.SELECTION_FLAG_DEFAULT
			)
		}

		fun getSubtitle(link: String?): MediaItem.Subtitle {
			return getSubtitle(Uri.parse(link?.encodeUrl()))
		}

		private fun getSubtitleMimeType(subtitle: String?): String {
			if (subtitle?.endsWith("srt") == true)
				return MimeTypes.APPLICATION_SUBRIP
			else
				return MimeTypes.TEXT_VTT
		}
	}
}