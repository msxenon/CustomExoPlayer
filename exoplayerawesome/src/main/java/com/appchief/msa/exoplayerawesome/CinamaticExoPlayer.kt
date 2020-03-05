package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.appchief.msa.exoplayerawesome.listeners.*
import com.appchief.msa.exoplayerawesome.viewcontroller.VideoControllerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.MimeTypes
import kotlin.math.abs

class CinamaticExoPlayer : PlayerView, PlaybackPreparer, PlayerControlView.VisibilityListener,
	 MediaPlayerControl {

	 private val taag = "CEP isFloating = ${R.id.playerView == this.id}"
	 var controllerViiablilityListener:PlayerControlView.VisibilityListener? = null
	 var cinematicPlayerViews: CinematicPlayerViews? = null
	 constructor(context: Context) : super(context)
	 constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	 constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
		  context,
		  attrs,
		  defStyleAttr
	 )

	 private var loadingV: View? = null
	 private fun loadingView(): View? {
		  return loadingV//playerUiFinalListener?.loadingView()
	 }

	 fun setController(title: String?) {
		  if (customController == null) {
			   customController = VideoControllerView(context!!)
			   Log.e(taag, "controller ${cinematicPlayerViews?.controlLayout != null}")
			   customController?.setAnchorView(this, title, cinematicPlayerViews?.controlLayout)

			   customController?.show()
		  }

		  try {
			   cinematicPlayerViews?.loadingView?.let {
					if (loadingV == null) {
						 loadingV = LayoutInflater.from(context).inflate(it, null)
						 val x = FrameLayout.LayoutParams(
							  cinematicPlayerViews!!.loaderSizeInP,
							  cinematicPlayerViews!!.loaderSizeInP
						 )
						 x.gravity = Gravity.CENTER
						 addView(loadingV, x)
						 loadingV?.visibility = View.GONE
					}
			   }
		  } catch (e: Exception) {
		  }
	 }

	 private fun controlController(visibility: Int) {
		  if (visibility == View.VISIBLE)
			   customController?.show()
		  else
			   customController?.hide()
	 }

	 //start
	 private var startAutoPlay: Boolean = true
	 private var forceReplay = true
	 private var nowPlaying: NowPlaying? = null
	 private var mediaSource: MediaSource? = null
	  var trackSelector: DefaultTrackSelector? = null
	 var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
	 var customController: VideoControllerView? = null
	 var playerUiFinalListener: CineamaticPlayerScreen? = null
	 var hasSettingsListener:SettingsListener? = null

	 override fun onDetachedFromWindow() {
		  try {
			   Log.e(taag, "onDetachedFromWindow ${this.id == R.id.playerView}")
			   savePlayData()
			   this.player?.playWhenReady = false
			   this.player?.stop(true)
			   this.player?.stop()
			   this.player?.playWhenReady = false
			   startAutoPlay = false
			   this.player?.release()
			   this.player?.release()
			   this.player = null
		  } catch (e: Exception) {
			   e.printStackTrace()
		  }
		  super.onDetachedFromWindow()
	 }

	 fun playLinkNSub(
		  videoLink: String?,
		  episodeId: Long?,
		  movieId: Long?,
		  playerType: PlayerType,
		  SrtLink: String?,
		  poster: String,
		  geners: String, title: String, runtime: Long
	 ) {
		  if (videoLink == null)
			   return
		  if (playerUiFinalListener == null)
			   throw Exception("playerUiFinalListener not setted")
		  savePlayData()
		  nowPlaying =
			   NowPlaying(movieId, episodeId, playerType, poster, videoLink, geners, title, runtime)
		  if (playerUiFinalListener?.isConnectedToCast() != true) {
			   initializePlayer(videoLink.encodeUrl(), SrtLink?.encodeUrl())
		  } else {
			   castCurrent()
		  }
	 }

	 fun setupPlayer() {
		  if (trackSelectorParameters == null) {
			   trackSelectorParameters = DefaultTrackSelector.ParametersBuilder(context)
					.setRendererDisabled(C.TRACK_TYPE_VIDEO, false).setPreferredTextLanguage("ar")
					.setPreferredTextLanguage("en").build()
			   this.setControllerVisibilityListener(this)
			   this.setErrorMessageProvider(PlayerErrorMessageProvider())

		  }
	 }

	 fun savePlayData() {
		  val cp = player?.currentPosition ?: 0
		  val ttl = player?.duration ?: 0

		  if (cp > 0)
			   nowPlaying?.let {
					playerUiFinalListener?.savePlayPosition(
						 nowPlaying,
						 cp,
						 ttl
					)
			   }
	 }
	 var hasSettings = false
	 private val plistener = object : Player.EventListener {
		  override fun onPlayerStateChanged(
			   playWhenReady: Boolean,
			   playbackState: Int
		  ) {
			   try {
					Log.e(
						 taag,
						 "onPlayerStateChanged ps$playbackState $playWhenReady ${loadingView() != null}"
					)
					loadingView()?.visibility = View.GONE
					if (playbackState == ExoPlayer.STATE_BUFFERING) {
						 loadingView()?.visibility = View.VISIBLE
						 this@CinamaticExoPlayer.hideController()
					} else if (playbackState == ExoPlayer.STATE_READY) {
						 forceReplay = false

						 checkHasSettings()
					} else if (playWhenReady && playbackState == ExoPlayer.STATE_ENDED) {
						 if (playWhenReady && forceReplay) {
							  start()
						 }
					}
					customController?.updateViews(playbackState == ExoPlayer.STATE_BUFFERING)
			   } catch (e: Exception) {
					e.printStackTrace()
			   }
		  }
	 }

	 fun setListeners() {
		  this.setPlaybackPreparer(this)
		  this.player?.addListener(plistener)
		  this.player?.addListener(
			   PlayerEventListener(
					context,
					this,
					playerUiFinalListener
			   )
		  )
		  controllerViiablilityListener =
			   PlayerControlView.VisibilityListener { visibility ->
					controlController(visibility)
			   }
	 }
	 fun applySettings(newPlayer: Player? = null) {
		  if (newPlayer == null) {
			   this.player?.playWhenReady = startAutoPlay
			   this.resizeMode =
					cinematicPlayerViews?.resizeMode() ?: AspectRatioFrameLayout.RESIZE_MODE_FIT
			   player?.videoComponent?.videoScalingMode =
					cinematicPlayerViews?.videoScalingMode() ?: C.VIDEO_SCALING_MODE_SCALE_TO_FIT
			   Log.e(
					"ApplySettings",
					"$ load${cinematicPlayerViews?.resizeMode() != null} ${cinematicPlayerViews?.videoScalingMode() != null}"
			   )

			   this.setShowBuffering(SHOW_BUFFERING_NEVER)
		  }
		  hasSettings = false
		  hasSettingsListener?.hasSettings(false)

		  setController(null)
		  Log.e("ApplySettings", "$ load${cinematicPlayerViews?.loadingView != null}")
		  Log.e("ApplySettings", "$ load2 ${cinematicPlayerViews?.controlLayout != null}")

	 }
	 private fun initializePlayer(url: String, srtLink: String?) {
		  mediaSource = null

		  try {
			   setupPlayer()
			   if (this.player == null) {
					val trackSelectionFactory: com.google.android.exoplayer2.trackselection.TrackSelection.Factory
					trackSelectionFactory = AdaptiveTrackSelection.Factory()

					trackSelector = DefaultTrackSelector(context, trackSelectionFactory)
					trackSelector?.parameters = trackSelectorParameters!!

					this.player = SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector!!)
						 .build()


					applySettings()


			   }
			   player?.playWhenReady = true


			   mediaSource =
					buildMediaSource(Uri.parse(url), srtLink)
			   val sp = getLastPos()
			   val haveStartPosition = sp > 0L
			   setListeners()
			   if (mediaSource != null) {
					(this.player as ExoPlayer).prepare(mediaSource!!, !haveStartPosition, false)
					this.player?.seekTo(sp)
			   }

		  } catch (e: Exception) {
			   playerUiFinalListener?.onMessageRecived(
					e.localizedMessage,
					ExoPlaybackException.TYPE_UNEXPECTED
			   )
			   e.printStackTrace()
		  }
	 }
	 private fun checkHasSettings() {
		 val m =  SettingsUtil.willHaveContent(trackSelector)
		  hasSettings = m
		  hasSettingsListener?.hasSettings(m)
		  Log.e("CEP", "settings $hasSettings ${hasSettingsListener != null}")
	 }

	 fun getLastPos(): Long {
		  var res = 0L
		  playerUiFinalListener?.getLastPosition(nowPlaying)?.let {
			   res = it
		  }
		  Log.e(taag, "getLastPos $nowPlaying $res")
		  return res
	 }
	 private fun isSreaming(): Boolean {
		  return nowPlaying?.type == PlayerType.CHANNEL
	 }

	 private fun buildMediaSource(uri: Uri, srtLink: String?): MediaSource? {
		  return if (!srtLink.isNullOrBlank()) {
			   addSubTitlesToMediaSource(
					ExoFactorySingeleton.getInstance().buildMediaSource(uri),
					srtLink.encodeUrl()
			   )
		  } else {
			   ExoFactorySingeleton.getInstance().buildMediaSource(uri)
		  }
	 }

	 private fun addSubTitlesToMediaSource(
		  mediaSource: MediaSource?,
		  subTitlesUrl: String
	 ): MediaSource {
		  val textFormat = Format.createTextSampleFormat(
			   null, MimeTypes.APPLICATION_SUBRIP,
			   null, Format.NO_VALUE, Format.NO_VALUE, "en", null, Format.OFFSET_SAMPLE_RELATIVE
		  )
		  val uri = Uri.parse(subTitlesUrl)
		  Log.e("subtitleURI", uri.toString() + " ")
		  val subtitleSource =
			   SingleSampleMediaSource.Factory(ExoFactorySingeleton.getInstance().buildDataSourceFactory())
					.createMediaSource(uri, textFormat, C.TIME_UNSET)
		  return MergingMediaSource(mediaSource, subtitleSource)
	 }

	 override fun preparePlayback() {
	 }

	 override fun onVisibilityChange(visibility: Int) {
		  Log.e("msdmdsmd", "$visibility $useController ${customController != null}")
		  controlController(visibility)
	 }

	 override fun start() {
		  Log.e(
			   "msdmdsmd start",
			   "${reachedEndOfVideo()}${player?.currentPosition} ${player?.duration} "
		  )

		  if (reachedEndOfVideo()) {
			   seekTo(0)
		  }
		  this.player?.playWhenReady = true
	 }

	 override fun pause() {
		  this.player?.playWhenReady = false
	 }

	 fun reachedEndOfVideo(): Boolean {
		  var res = false
		  this.player?.let {
			   val x = it.duration - it.currentPosition
			   res = abs(x) <= 500
			   Log.e(taag, "reachedEndOfVideo $x $res ${it.duration} ${it.currentPosition}")
		  }

		  return res
	 }
	 override fun onPauseSave() {
		  pause()
		  savePlayData()
	 }

	 override val duration: Long
		  get() = player?.duration ?: 0
	 override val currentPosition: Long
		  get() = player?.currentPosition ?: 0

	 override fun seekTo(pos: Long) {
		  player?.seekTo(pos)
	 }

	 override val isPlaying: Boolean
		  get() = player?.isPlaying == true
	 override val bufferPercentage: Int
		  get() = 0

	 override fun canPause(): Boolean {
		  return !isSreaming()
	 }

	 override fun canSeekBackward(): Boolean {
		  return !isSreaming()
	 }

	 override fun canSeekForward(): Boolean {
		  return !isSreaming()
	 }

	 override fun hasNext(): Boolean {
		  return false
	 }

	 override fun isFirstItem(): Boolean {
		  return true
	 }

	 override val canHaveFullScreen: Boolean
		  get() = playerUiFinalListener?.canMinimize() != false

	 //	 var isInFullScreenMode_: Boolean = false
//		  set(value) {
//			   field = value
//			   customController?.updateFullScreen()
//		  }
	 override fun toggleFullScreen() {
		  playerUiFinalListener?.setScreenOrentation(ExoIntent.isInFullScreen)
	 }

	 override fun canShowController(useController: Boolean) {
		  Log.e("canShowController", "$useController")
		  this.useController = useController
		  if (!useController)
			   controlController(View.GONE)
	 }

	 override fun minimizeAble(): Boolean {
		  return playerUiFinalListener?.canMinimize() == true && !ExoIntent.isInFullScreen
	 }

	 override fun minmize() {
		  playerUiFinalListener?.doMinimizePlayer()
	 }

	 fun init(finaluilistener: CineamaticPlayerScreen) {
		  playerUiFinalListener = finaluilistener
	 }

	 override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		  Log.e("CEP", "dispatchTouchEvent $childCount")
		  for (i in 0..childCount) {
			   getChildAt(i)?.let {
					val consumed = it.dispatchTouchEvent(ev)
					if (consumed) {
						 return consumed
					}
			   }
		  }
		  val x = super.dispatchTouchEvent(ev)
		  Log.e("CEP", "dispatchTouchEvent end ${ExoIntent.isInFullScreen} $x")

		  return x
	 }

	 override fun onTouchEvent(event: MotionEvent): Boolean {
		  if (ExoIntent.isInFullScreen) {
			   customController?.toggleShowHide()
		  }
		  return false
	 }

	 fun castCurrent() {
		  if (nowPlaying != null) {
			   CastUtil.castThis(
					context,
					nowPlaying?.title,
					nowPlaying?.videoLink,
					nowPlaying!!.geners,
					nowPlaying!!.poster,
					nowPlaying!!.runtime,
					isSreaming(),
					getLastPos()
			   )
			   playerUiFinalListener?.onDissmiss(CloseReason.Casting)
		  }
	 }
}

fun CinamaticExoPlayer.copyFrom(oldCinamaticExoPlayer: CinamaticExoPlayer) {
	 this.hasSettingsListener = oldCinamaticExoPlayer.hasSettingsListener
	 this.cinematicPlayerViews = oldCinamaticExoPlayer.cinematicPlayerViews
	 this.player = oldCinamaticExoPlayer.player
	 this.playerUiFinalListener = oldCinamaticExoPlayer.playerUiFinalListener
	 this.setController(null)
	 this.setListeners()
//	 this.loadingV = oldCinamaticExoPlayer.loadingV
}