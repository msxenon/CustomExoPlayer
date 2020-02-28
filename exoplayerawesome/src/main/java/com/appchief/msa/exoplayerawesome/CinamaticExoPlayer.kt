package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.appchief.msa.exoplayerawesome.listeners.*
import com.appchief.msa.exoplayerawesome.viewcontroller.VideoControllerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.MimeTypes
import kotlin.math.abs

open class CinamaticExoPlayer : PlayerView, PlaybackPreparer, PlayerControlView.VisibilityListener,
	 MediaPlayerControl {
	 var controllerViiablilityListener:PlayerControlView.VisibilityListener? = null

	 constructor(context: Context) : super(context)
	 constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	 constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
		  context,
		  attrs,
		  defStyleAttr
	 )

	 private fun loadingView(): View? {
		  return playerUiFinalListener?.loadingView()
	 }

	 private fun setController(title: String?) {
		  customController = VideoControllerView(context!!)
		  customController?.setAnchorView(this, title, playerUiFinalListener?.ControllerLayout())

		  customController?.show()

		  controllerViiablilityListener =
			   PlayerControlView.VisibilityListener { visibility ->
					controlController(visibility)
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
	 private var nowPlaying: NowPlaying? = null
	 private var mPlayer: ExoPlayer? = null
	 private var mediaSource: MediaSource? = null
	  var trackSelector: DefaultTrackSelector? = null
	 private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
	 private var lastSeenTrackGroupArray: TrackGroupArray? = null
	 var customController: VideoControllerView? = null
	 var playerUiFinalListener: CineamaticPlayerScreen? = null
	 var hasSettingsListener:SettingsListener? = null

	 override fun onDetachedFromWindow() {
		  savePlayData()
		  this.player?.playWhenReady = false
		  this.player?.stop(true)
		  mPlayer?.stop()
		  mPlayer?.playWhenReady = false
		  startAutoPlay = false
		  this.player?.release()
		  player?.release()
		  player = null
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

	 private fun setupPlayer() {
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

	 private fun initializePlayer(url: String, srtLink: String?) {
		  mediaSource = null

		  try {
			   setupPlayer()
			   if (mPlayer == null) {
					val trackSelectionFactory: com.google.android.exoplayer2.trackselection.TrackSelection.Factory
					trackSelectionFactory = AdaptiveTrackSelection.Factory()

					trackSelector = DefaultTrackSelector(context, trackSelectionFactory)
					trackSelector?.parameters = trackSelectorParameters!!
					lastSeenTrackGroupArray = null

					mPlayer = SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector!!)
						 .build()



					mPlayer?.addListener(PlayerEventListener(context, this, playerUiFinalListener))
					mPlayer?.playWhenReady = startAutoPlay
					this.player = mPlayer
					this.setPlaybackPreparer(this)
					this.player?.addListener(object : Player.EventListener {
						 override fun onPlayerStateChanged(
							  playWhenReady: Boolean,
							  playbackState: Int
						 ) {
							  customController?.updateViews()
 							  if (playbackState == ExoPlayer.STATE_BUFFERING) {
								   loadingView()?.visibility = View.VISIBLE
								   this@CinamaticExoPlayer.hideController()
							  }else if (playbackState == ExoPlayer.STATE_READY){
								   loadingView()?.visibility = View.GONE
								   val isEnded =
										player?.currentPosition ?: 0 >= player?.duration ?: -1
								   if (playWhenReady && playbackState == ExoPlayer.STATE_READY && isEnded ){
										player?.playWhenReady = false
										seekTo(0)
								   }
								   if (playbackState == ExoPlayer.STATE_READY)
										checkHasSettings()

							  } else {
								   loadingView()?.visibility = View.GONE
							  }
						 }
					})
					this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
					this.setShowBuffering(SHOW_BUFFERING_NEVER)

			   }
			   player?.playWhenReady = true


			   mediaSource =
					buildMediaSource(Uri.parse(url), srtLink)
			   val sp = getLastPos(nowPlaying)
			   val haveStartPosition = sp > 0L

			   if (mediaSource != null) {
					mPlayer?.prepare(mediaSource!!, !haveStartPosition, false)
					mPlayer?.seekTo(sp)
			   }
			   hasSettings = false
			   hasSettingsListener?.hasSettings(false)
			   setController(null)
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
	 }

	 fun getLastPos(nowPlaying: NowPlaying?): Long {
		  var res = 0L
		  playerUiFinalListener?.getLastPosition(nowPlaying)?.let {
			   if (nowPlaying!!.runtime - it >= 5000) {
					res = it
					if (reachedEndOfVideo())
						 res = 0
			   }
		  }
		  Log.e("lastpos", "$nowPlaying $res")
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
		  mPlayer?.playWhenReady = true
	 }

	 override fun pause() {
		  mPlayer?.playWhenReady = false
	 }

	 fun reachedEndOfVideo(): Boolean {
		  var res = false
		  player?.let {
			   val x = it.duration - it.currentPosition
			   res = abs(x) <= 1000
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
	 var isInFullScreenMode: Boolean = false
		  internal set(value) {
			   field = value
			   customController?.updateFullScreen()
		  }
	 override fun toggleFullScreen() {
		  playerUiFinalListener?.setScreenOrentation(isInFullScreenMode)
	 }

	 override fun canShowController(useController: Boolean) {
		  Log.e("canShowController", "$useController")
		  this.useController = useController
		  if (!useController)
			   controlController(View.GONE)
	 }

	 override fun minimizeAble(): Boolean {
		  return playerUiFinalListener?.canMinimize() == true
	 }

	 override fun minmize() {
		  playerUiFinalListener?.doMinimizePlayer()
	 }

	 fun init(finaluilistener: CineamaticPlayerScreen) {
		  playerUiFinalListener = finaluilistener
	 }

	 override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		  Log.e("CEP", "dispatchTouchEvent")
		  for (i in 0..childCount) {
			   getChildAt(i)?.let {
					val consumed = it.dispatchTouchEvent(ev)
					if (consumed) {
						 return consumed
					}
			   }
		  }

		  return super.dispatchTouchEvent(ev)
	 }

	 override fun onTouchEvent(event: MotionEvent): Boolean {
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
					getLastPos(nowPlaying)
			   )
			   playerUiFinalListener?.onDissmiss(CloseReason.Casting)
		  }
	 }
}
