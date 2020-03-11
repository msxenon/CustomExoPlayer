package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.appchief.msa.CastApp
import com.appchief.msa.exoplayerawesome.ExoIntent.usedInistances
import com.appchief.msa.exoplayerawesome.listeners.*
import com.appchief.msa.exoplayerawesome.viewcontroller.VideoControllerView
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import kotlin.math.abs
import kotlin.math.min

class CinamaticExoPlayer : PlayerView, PlaybackPreparer, PlayerControlView.VisibilityListener,
	 MediaPlayerControl, LifecycleObserver {

	 fun attachObersver(lifecycleOwner: LifecycleOwner) {
		  lifecycleOwner.lifecycle.addObserver(this)
	 }

	 @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
	 fun onCreate() {
		  usedInistances += 1
		  Log.e(
			   "TAG",
			   "================================>>>> lifecycle owner ON_CREATE $usedInistances $taag"
		  )
	 }

	 @OnLifecycleEvent(Lifecycle.Event.ON_START)
	 fun start_() {
		  Log.e("TAG", "================================>>>> lifecycle owner STARTED $taag")
	 }

	 @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
	 fun onResumeLC() {
		  Log.e(
			   "TAG",
			   "================================>>>> lifecycle owner ON_RESUME "
		  )
//			onResume()
		  Log.e("TAG", "================================>>>> lifecycle ${player == null} ")
		  ExoIntent.getPlayerHere(this)
		  //  start()
		  Log.e("TAG2", "================================>>>> lifecycle ${player == null} ")
	 }

	 private var castExoPlayer: CastPlayer? = null
	 private var playerManager: PlayerManager? = null
	 fun initCast() {
//		  castExoPlayer = CastPlayer(this.getCastContext())
//		  castExoPlayer?.addListener(plistener)
//		  castExoPlayer?.setSessionAvailabilityListener(this)
		  playerManager = PlayerManager(object : PlayerManager.Listener {
			   override fun onUnsupportedTrack(trackType: Int) {
			   }

			   override fun onQueuePositionChanged(previousIndex: Int, newIndex: Int) {
			   }
		  }, this, customController!!, context, this.getCastContext(), trackSelector)
	 }
	 @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
	 fun stop() {
		  pause()
		  onPauseSave()
		  Log.e("TAG", "================================>>>> lifecycle owner STOPED  $taag")
	 }

	 @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
	 fun destroy() {
		  usedInistances -= 1
		  releasePlayer()
		  Log.e(
			   "TAG",
			   "================================>>>> lifecycle owner destroy --  sfcnull $usedInistances ${this.videoSurfaceView == null}"
		  )
	 }

	 fun releasePlayer() {
		  try {
			   if (player != null && usedInistances == 0) {
					player?.release()
					player = null
					removeAllViews()
					ExoIntent.reInit()
			   } else {
					pause()
			   }
		  } catch (e: Exception) {
			   e.printStackTrace()
		  }
	 }

	 private val isFloatingPlayer = R.id.playerView == id
	 private val taag = "CEP isFloating = ${isFloatingPlayer}"
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
	 fun loadingView(): View? {
		  return loadingV//playerUiFinalListener?.loadingView()
	 }

	 fun setController(title: String?) {
		  if (customController == null) {
			   customController = VideoControllerView(context!!)
			   Log.e(taag, "controller ${cinematicPlayerViews?.controlLayout != null}")
			   customController?.setAnchorView(this, title, cinematicPlayerViews?.controlLayout)
			   customController?.updateViews(null)
			   customController?.show()
		  } else {
			   customController?.updateViews(true)
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

	 var forceReplay = true
	 var nowPlaying: NowPlaying? = null
	 var mediaSource: MediaSource? = null

	 val trackSelector: DefaultTrackSelector by lazy {
		  val trackSelectionFactory: com.google.android.exoplayer2.trackselection.TrackSelection.Factory
		  trackSelectionFactory = AdaptiveTrackSelection.Factory()
		  val m = DefaultTrackSelector(context, trackSelectionFactory)
		  m.parameters = trackSelectorParameters
		  return@lazy m
	 }
	 val trackSelectorParameters: DefaultTrackSelector.Parameters by lazy {
		  val x = DefaultTrackSelector.ParametersBuilder(context)
			   .setRendererDisabled(C.TRACK_TYPE_VIDEO, false).setPreferredTextLanguage("ar")
			   .setPreferredTextLanguage("en").build()

		  return@lazy x
	 }
	 var customController: VideoControllerView? = null
	 var playerUiFinalListener: CineamaticPlayerScreen? = null
	 var hasSettingsListener:SettingsListener? = null



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
		  playerManager?.release()
		  nowPlaying =
			   NowPlaying(
					movieId, episodeId, playerType, poster,
					videoLink.encodeUrl(), geners, title, runtime, SrtLink?.encodeUrl()
			   )
		  // if (playerUiFinalListener?.isConnectedToCast() != true) {
			   initializePlayer()
//		  } else {
//			   castCurrent()
//		  }

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
		  }
	 }
	 val eventListener = PlayerEventListener(
		  context,
		  this,
		  playerUiFinalListener
	 )
	 fun setListeners() {
		  setControllerVisibilityListener(this)
		  setErrorMessageProvider(PlayerErrorMessageProvider())
		  setPlaybackPreparer(this)
		  player?.addListener(plistener)
		  player?.addListener(
			   eventListener
		  )
		  controllerViiablilityListener =
			   PlayerControlView.VisibilityListener { visibility ->
					controlController(visibility)
			   }
		  player?.videoComponent?.addVideoListener(object : VideoListener {
			   override fun onVideoSizeChanged(
					width: Int,
					height: Int,
					unappliedRotationDegrees: Int,
					pixelWidthHeightRatio: Float
			   ) {
					if (isFloatingPlayer) {
						 videoSize(height, width, true)
					}
			   }
		  })
	 }

	 private fun videoSize(height: Int, width: Int = 0, respectAspectRatio: Boolean = false) {
		  val screenHeightPx = Resources.getSystem().displayMetrics.heightPixels
		  val screenWPx = Resources.getSystem().displayMetrics.widthPixels
		  val aspectRatio = screenWPx.toFloat() / width.toFloat()

		  val actualHeight = if (respectAspectRatio && width > 0) {
			   (height * aspectRatio).toInt()
		  } else
			   height
		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			   TransitionManager.beginDelayedTransition(
					rootView as ViewGroup, TransitionSet()
						 .addTransition(ChangeBounds())
			   )
		  }
		  val m = layoutParams

		  Log.e(
			   "videoSizeCHanged",
			   " $width $height  $respectAspectRatio $aspectRatio $actualHeight $screenHeightPx , $screenWPx }"
		  )
		  m.height = min(screenHeightPx.div(2), actualHeight)
		  layoutParams = m
	 }

	 fun canAutoPlay(): Boolean {
		  return !ExoIntent.paused
	 }
	 fun applySettings(newPlayer: Player? = null) {
		  if (newPlayer == null) {
			   player?.playWhenReady = canAutoPlay()


			   setShowBuffering(SHOW_BUFFERING_NEVER)
		  }
		  resizeMode =
			   cinematicPlayerViews?.resizeMode() ?: AspectRatioFrameLayout.RESIZE_MODE_FIT
		  player?.videoComponent?.videoScalingMode =
			   cinematicPlayerViews?.videoScalingMode() ?: C.VIDEO_SCALING_MODE_SCALE_TO_FIT
		  Log.e(
			   "ApplySettings",
			   "$ VIDEO SIZEING ${cinematicPlayerViews?.resizeMode() != null} ${cinematicPlayerViews?.videoScalingMode() != null}"
		  )

		  setController(null)
		  Log.e("ApplySettings", "$ load${cinematicPlayerViews?.loadingView != null}")
		  Log.e("ApplySettings", "$ load2 ${cinematicPlayerViews?.controlLayout != null}")

	 }

	 val bMeter by lazy { DefaultBandwidthMeter.Builder(context).build() }
	 fun initializePlayer(): Boolean? {
		  mediaSource = null
		  if (nowPlaying?.videoLink == null)
			   return null
		  try {
			   ExoIntent.paused = false
			   player = null
			   player = SimpleExoPlayer.Builder(context).setBandwidthMeter(bMeter)
					.setTrackSelector(trackSelector)
					.build()


			   applySettings()


			   mediaSource =
					buildMediaSource(Uri.parse(nowPlaying?.videoLink), nowPlaying?.srtLink)

			   initCast()

			   setListeners()
			   val x = CastUtil.castThisMI(
					context,
					nowPlaying?.title,
					nowPlaying?.videoLink,
					nowPlaying!!.geners,
					nowPlaying!!.poster,
					nowPlaying!!.runtime,
					isSreaming(),
					getLastPos("pairinit")
			   )
			   playerManager?.addItem(mediaSource, x.second)
//			   if (mediaSource != null) {
//					(player as ExoPlayer).prepare(mediaSource!!, !haveStartPosition, false)
//					player?.seekTo(sp)
//			   }
			   return true
		  } catch (e: Exception) {
			   playerUiFinalListener?.onMessageRecived(
					e.localizedMessage,
					ExoPlaybackException.TYPE_UNEXPECTED
			   )
			   e.printStackTrace()
		  }
		  return null
	 }

	 fun checkHasSettings() {
		 val m =  SettingsUtil.willHaveContent(trackSelector)
		  hasSettings = m
		  hasSettingsListener?.hasSettings(m)
		  Log.e("CEP", "settings $hasSettings ${hasSettingsListener != null}")
	 }

	 fun getLastPos(tag: String): Long {
		  var res = 0L
		  playerUiFinalListener?.getLastPosition(nowPlaying)?.let {
			   res = it
		  }
		  Log.e(taag, "getLastPos $tag $nowPlaying $res")
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
		  Log.e("msdmdsmd vischange", "$visibility $useController ${customController != null}")
		  controlController(visibility)
	 }

	 override fun start() {
		  Log.e(
			   "msdmdsmd start",
			   "${reachedEndOfVideo()}${player?.currentPosition} ${player?.duration} "
		  )

		  if (reachedEndOfVideo()) {
			   seekTo(0)
		  } else
			   seekTo(player?.currentPosition ?: 0)
		  player?.playWhenReady = canAutoPlay()
	 }

	 override fun pause() {
		  if (!context.isCastConnected())
			   player?.playWhenReady = false
	 }

	 override fun onPause() {
	 }
	 fun reachedEndOfVideo(): Boolean {
		  var res = false
		  player?.let {
			   val x = it.duration - it.currentPosition
			   res = abs(x) <= 500
			   Log.e(taag, "reachedEndOfVideo $x $res ${it.duration} ${it.currentPosition}")
		  }

		  return res
	 }
	 override fun onPauseSave() {
		  savePlayData()
	 }

	 override val duration: Long
		  get() = player?.duration ?: 0
	 override val currentPosition: Long
		  get() = player?.currentPosition ?: 0

	 override fun seekTo(pos: Long) {
		  Log.e(taag, "seekto $pos")
		  player?.seekTo(pos)
	 }

	 override val isPlaying: Boolean
		  get() = player?.isPlaying == true
	 override val bufferPercentage: Int
		  get() = 0

	 override fun canPause(): Boolean {
		  return true
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

	 override fun canShowController(useController_: Boolean) {
		  Log.e("canShowController", "$useController_")
		  useController = useController_
		  if (!useController_)
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

	 //	 fun castCurrentMI(): MediaItem {
//		  return  CastUtil.castThisMI(
//			   context,
//			   nowPlaying?.title,
//			   nowPlaying?.videoLink,
//			   nowPlaying!!.geners,
//			   nowPlaying!!.poster,
//			   nowPlaying!!.runtime,
//			   isSreaming(),
//			   getLastPos()
//		  )
//
//	 }
	 fun castCurrent(): Array<MediaQueueItem> {
		  return CastUtil.castThis(
			   context,
			   nowPlaying?.title,
			   nowPlaying?.videoLink,
			   nowPlaying!!.geners,
			   nowPlaying!!.poster,
			   nowPlaying!!.runtime,
			   isSreaming(),
			   getLastPos("castcurrent")
		  )
	 }

	 fun minSize() {
		  videoSize(100.DpToPx())
	 }
}

fun Int.DpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

private fun CinamaticExoPlayer.getCastContext(): CastContext {
	 return (this.context.applicationContext as CastApp).mCastContext
}

private fun CinamaticExoPlayer.attachCastListener() {
	 // (this.context.applicationContext as CastApp).listeners.add(this)
}

private fun Context.isCastConnected(): Boolean {
	 return (this.applicationContext as CastApp).isCastConnected()
}
fun CinamaticExoPlayer.copyFrom(oldCinamaticExoPlayer: CinamaticExoPlayer) {
	 nowPlaying = oldCinamaticExoPlayer.nowPlaying
	 cinematicPlayerViews = oldCinamaticExoPlayer.cinematicPlayerViews
	 if (player == null)
		  player = oldCinamaticExoPlayer.player
	 playerUiFinalListener = oldCinamaticExoPlayer.playerUiFinalListener
	 setController(null)
//	 hasSettingsListener = oldCinamaticExoPlayer.hasSettingsListener
	 setListeners()
	 applySettings()
//	 loadingV = oldCinamaticExoPlayer.loadingV
}