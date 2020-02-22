package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
 import com.appchief.msa.exoplayerawesome.listeners.*
import com.appchief.msa.exoplayerawesome.listeners.PlayerErrorMessageProvider
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.MimeTypes

class CinamaticExoPlayer : PlayerView, PlaybackPreparer, PlayerControlView.VisibilityListener,
	 MediaPlayerControl {

	 constructor(context: Context) : super(context)
	 constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		  initAttrs(context, attrs)
	 }

	 constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
		  context,
		  attrs,
		  defStyleAttr
	 ) {
		  initAttrs(context, attrs)
	 }

	 fun initAttrs(context: Context, attrs: AttributeSet?) {
		  if (attrs != null) {
			   val attributeArray = context.obtainStyledAttributes(
					attrs,
					R.styleable.CinamaticExoPlayer
			   )
			   val progressLayoutId =
					attributeArray.getResourceId(R.styleable.CinamaticExoPlayer_progressLayout, -1)
			   if (progressLayoutId > -1) {
					playProgressBar =
						 LayoutInflater.from(context).inflate(progressLayoutId, this, false)
					playProgressBar?.visibility = View.GONE
					addView(playProgressBar)
			   }
//			   val clid =
//					attributeArray.getResourceId(R.styleable.CinamaticExoPlayer_customController, -1)
//			   if (progressLayoutId > -1){
//					customController = LayoutInflater.from(context).inflate(clid,this,false)
//					addView(customController)
//
//			   }
			   attributeArray.recycle()
		  }
	 }

	 //start
	 private var startAutoPlay: Boolean = true
	 private var nowPlaying: NowPlaying? = null
	 private var mPlayer: ExoPlayer? = null
	 private var mediaSource: MediaSource? = null
	 internal var trackSelector: DefaultTrackSelector? = null
	 private var trackSelectorParameters: DefaultTrackSelector.Parameters? = null
	 private var lastSeenTrackGroupArray: TrackGroupArray? = null
	 private var playProgressBar: View? = null
	 var customController: View? = null
	 var playerUiFinalListener: PlayerUiFinalListener? = null

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
		  SrtLink: String?
	 ) {
		  if (videoLink == null)
			   return
		  if (playerUiFinalListener == null)
			   throw Exception("playerUiFinalListener not setted")
		  savePlayData()
		  nowPlaying = NowPlaying(movieId, episodeId, playerType)

		  initializePlayer(videoLink.encodeUrl(), SrtLink?.encodeUrl())
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
	 fun savePlayData(){
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
							  if (playbackState == ExoPlayer.STATE_BUFFERING) {
								   playProgressBar?.visibility = View.VISIBLE
								   this@CinamaticExoPlayer.hideController()
							  }else if (playbackState == ExoPlayer.STATE_READY){
								   playProgressBar?.visibility = View.GONE

								   val isEnded = player?.currentPosition?:0 > player?.duration?:-1
								   if (playWhenReady && playbackState == ExoPlayer.STATE_READY && isEnded ){
										player?.playWhenReady = false
										seekTo(0)
								   }
							  } else {
								   playProgressBar?.visibility = View.GONE
							  }
						 }
					})
			   }
			   player?.playWhenReady = true


			   mediaSource =
					buildMediaSource(Uri.parse(url), srtLink)
			   val sp = playerUiFinalListener?.getLastPosition(nowPlaying) ?: 0
			   val haveStartPosition = sp > 0L

			   if (mediaSource != null) {
					mPlayer?.prepare(mediaSource!!, !haveStartPosition, false)
					mPlayer?.seekTo(sp)
			   }
		  } catch (e: Exception) {
			   playerUiFinalListener?.onMessageRecived(e.localizedMessage, PlayerStatus.CantPlay)
			   e.printStackTrace()
		  }
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
		  Log.e("msdmdsmd", "$visibility")
		  customController?.visibility = visibility
	 }

	 override fun start() {
		  mPlayer?.playWhenReady = true
	 }

	 override fun pause() {
		  mPlayer?.playWhenReady = false
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

	 override val isFullScreen: Boolean
		  get() = true

	 override fun toggleFullScreen() {
	 }
}
