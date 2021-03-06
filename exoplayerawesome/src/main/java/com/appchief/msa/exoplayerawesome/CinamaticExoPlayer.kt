package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.appchief.msa.CastApp
import com.appchief.msa.exoplayerawesome.BuildConfig.DEBUG
import com.appchief.msa.exoplayerawesome.ExoIntent.Tag
import com.appchief.msa.exoplayerawesome.ExoIntent.usedInistances
import com.appchief.msa.exoplayerawesome.listeners.*
import com.appchief.msa.exoplayerawesome.viewcontroller.VideoControllerView
import com.appchief.msa.floating_player.VideoOverlayView
import com.appchief.msa.floating_player.touchEventInsideTargetView
import com.appchief.msa.youtube.PlayerDoubleTapListener
import com.appchief.msa.youtube.SeekListener
import com.appchief.msa.youtube.YouTubeOverlay
import com.appchief.msa.youtube.YouTubeOverlay.Companion.TAG
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.C.ROLE_FLAG_TRICK_PLAY
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.video.VideoListener
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import kotlinx.android.synthetic.main.appchief_floating_player.view.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

class CinamaticExoPlayer : PlayerView, PlaybackPreparer, PlayerControlView.VisibilityListener,
    MediaPlayerControl, LifecycleObserver {

    fun attachObersver(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    var isForeground = true

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        usedInistances += 1
        Log.e(
            "TAG",
            "================================>>>> lifecycle owner ON_CREATE $usedInistances $taag"
        )
        mDetector = GestureDetectorCompat(context, DoubleTapGestureListener())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start_() {
        Log.e("TAG", "================================>>>> lifecycle owner STARTED $taag")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResumeLC() {
        isForeground = true
        Log.e(
            "TAG",
            "================================>>>> lifecycle owner ON_RESUME  "
        )
//			onResume()
        Log.e("TAG", "================================>>>> lifecycle ${player == null} ")
        //  ExoIntent.getPlayerHere(this)
        lastPos_ = 0L
        getLastPos("resume")
        if (playerManager?.isConnected() != true)
            start()




        Log.e("TAG2", "================================>>>> lifecycle ${player == null} ")
    }

    private var playerManager: PlayerManager? = null
    fun initCast() {
        //		  castExoPlayer = CastPlayer(this.getCastContext())
//		  castExoPlayer?.addListener(plistener)
//		  castExoPlayer?.setSessionAvailabilityListener(this)
        if (playerManager == null) {
            playerManager = PlayerManager(context, object : PlayerManager.Listener {
                override fun onUnsupportedTrack(trackType: Int) {

                }

                override fun onQueuePositionChanged(previousIndex: Int, newIndex: Int) {
                }
            }, { this }, { customController }, this.getCastContext(), { trackSelector })
            playerManager?.update(true)
        } else
            playerManager?.update()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stop() {
        isForeground = false

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
            playerManager?.release()
            removeAllViews()
            ExoIntent.reInit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val isFloatingPlayer = R.id.motionInteractView == id
    private val taag = "CEP isFloating = ${isFloatingPlayer}"
    var controllerViiablilityListener: PlayerControlView.VisibilityListener? = null
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
        //  if (customController == null) {
//			   customController = VideoControllerView(context)
        Log.e(taag, "controller ${cinematicPlayerViews?.controlLayout != null}")
        customController?.setAnchorView(this, title, cinematicPlayerViews?.controlLayout)
        customController?.updateViews(null)
        customController?.show()
//		  } else {
//			   customController?.updateViews(true)
//		  }
        try {
            cinematicPlayerViews?.loadingView?.let {
                if (loadingV == null) {
                    loadingV = LayoutInflater.from(context).inflate(it, null)
                    val x = LayoutParams(
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

    var nowPlaying: NowPlaying? = null
    var mediaSource: MediaSource? = null
    val trackSelector: DefaultTrackSelector by lazy {
        val trackSelectionFactory: com.google.android.exoplayer2.trackselection.TrackSelection.Factory
        trackSelectionFactory = AdaptiveTrackSelection.Factory()
        val m = DefaultTrackSelector(context, trackSelectionFactory)

        m.parameters = trackSelectorParameters
        return@lazy m
    }
    val trackSelectorParameters by lazy {
        val x = DefaultTrackSelector.ParametersBuilder(context)
            .setExceedRendererCapabilitiesIfNecessary(true)
            .setExceedVideoConstraintsIfNecessary(true)
            .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
            .setSelectUndeterminedTextLanguage(true)
            .setPreferredTextLanguage("en")
            .setPreferredTextRoleFlags(ROLE_FLAG_TRICK_PLAY)
            .build()

        return@lazy x
    }
    var customController: VideoControllerView? = null
    var playerUiFinalListener: CineamaticPlayerScreen? = null
    var hasSettingsListener: SettingsListener? = null


    fun playLinkNSub(
        videoLink: String?,
        episode: Any?,
        movieId: Long?,
        playerType: PlayerType,
        SrtLink: String?,
        poster: String,
        geners: String, title: String, runtime: Long,
        vttLink: String?
    ) {
        if (videoLink == null)
            return
        if (playerUiFinalListener == null)
            throw Exception("playerUiFinalListener not setted")
        savePlayData()
        nowPlaying =
            NowPlaying(
                movieId, episode, playerType, poster,
                videoLink.encodeUrl(), geners, title, runtime, SrtLink?.encodeUrl(),
                vttLink = vttLink
            )
        lastPos_ = getLastPos("init", true)

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

    val eventListener = PlayerEventListener(
        context,
        this,
        playerUiFinalListener
    )

    fun setListeners() {
        setControllerVisibilityListener(this)
        setErrorMessageProvider(PlayerErrorMessageProvider(context))
        setPlaybackPreparer(this)
        player?.addListener(
            eventListener
        )
        controllerViiablilityListener =
            PlayerControlView.VisibilityListener { _ ->
                //	controlController(visibility)
            }
        player?.videoComponent?.addVideoListener(object : VideoListener {
            override fun onVideoSizeChanged(
                width: Int,
                height: Int,
                unappliedRotationDegrees: Int,
                pixelWidthHeightRatio: Float
            ) {
                Log.e("sizeChanged", " $unappliedRotationDegrees $pixelWidthHeightRatio")
                true.videoSize(height, ((width * pixelWidthHeightRatio).roundToInt()))

            }
        })
    }

    private fun Boolean.videoSize(height: Int, width: Int = 0) {
        val screenHeightPx = Resources.getSystem().displayMetrics.heightPixels
        val screenWPx = Resources.getSystem().displayMetrics.widthPixels
        val aspectRatio = screenWPx.toFloat() / width.toFloat()

        val actualHeight = if (this && width > 0) {
            (height * aspectRatio).toInt()
        } else
            height
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (rootView as? ViewGroup != null) {
                try {
                    TransitionManager.beginDelayedTransition(
                        rootView as? ViewGroup, TransitionSet()
                            .addTransition(ChangeBounds())
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        Log.e(
            "videoSizeCHanged",
            " $width $height  ${this} $aspectRatio $actualHeight $screenHeightPx , $screenWPx }"
        )
        realHeight = min(screenHeightPx.div(2), actualHeight)
        applyHeight(realHeight)
    }

    private var realHeight = 200.DpToPx()
    private fun applyHeight(height: Int) {
        //val m = layoutParams

        if (playerUiFinalListener?.isInFullScreen() == true) {
//			   m.height = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            findParentMotionLayout(height)
        }
        //  Log.e("applyHeight", "$realHeight && ${m.height} ${200.DpToPx()}")
        // layoutParams = m
    }

    var videoOverlayView: VideoOverlayView? = null
    private fun findParentMotionLayout(height: Int) {
        videoOverlayView?.setPortraitVideoHight(height)
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
            cinematicPlayerViews?.videoScalingMode() ?: Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT
        Log.e(
            "ApplySettings",
            "$ VIDEO SIZEING ${cinematicPlayerViews?.resizeMode() != null} ${cinematicPlayerViews?.videoScalingMode() != null}"
        )

        setController(if (ExoFactorySingeleton.isTv) nowPlaying?.title else null)
        Log.e("ApplySettings", "$ load${cinematicPlayerViews?.loadingView != null}")
        Log.e("ApplySettings", "$ load2 ${cinematicPlayerViews?.controlLayout != null}")
    }

    fun initializePlayer(): Boolean? {
        mediaSource = null
        if (nowPlaying?.videoLink == null)
            return null
        try {
            ExoIntent.paused = false



            initCast()


            applySettings()


            mediaSource =
                buildMediaSource(
                    Uri.parse(nowPlaying?.videoLink),
                    nowPlaying?.srtLink,
                    noCache = isSreaming()
                )


            setListeners()
            val x = CastUtil.castThisMI(
                context,
                nowPlaying?.title,
                nowPlaying?.videoLink,
                nowPlaying!!.geners,
                nowPlaying!!.poster,
                nowPlaying!!.runtime,
                isSreaming(),
                subtitleLink = nowPlaying!!.vttLink,
                position = getLastPos("pairinit")
            )
            playerManager?.addItem(mediaSource, mutableListOf(x.first))
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

    //	 override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//		  Log.e("keydown","$keyCode")
//		  return super.onKeyDown(keyCode, event)
//	 }
    fun checkHasSettings() {
        val m = SettingsUtil.willHaveContent(trackSelector)
        hasSettings = m
        hasSettingsListener?.hasSettings(m)
        Log.e("CEP", "settings $hasSettings ${hasSettingsListener != null}")
    }

    private var lastPos_ = 0L
    fun getLastPos(tag: String, force: Boolean = false): Long {
        if ((lastPos_ == 0L || force) && nowPlaying?.type != PlayerType.CHANNEL)
            playerUiFinalListener?.getLastPosition(nowPlaying)?.let {
                lastPos_ = it
            }
        Log.e(taag, "getLastPos $tag $nowPlaying $lastPos_")
        return lastPos_
    }

    private fun isSreaming(): Boolean {
        return nowPlaying?.type == PlayerType.CHANNEL
    }

    private fun buildMediaSource(uri: Uri, srtLink: String?, noCache: Boolean): MediaSource? {
        return if (!srtLink.isNullOrBlank()) {
            ExoFactory.addSubTitlesToMediaSource(
                ExoFactorySingeleton.getInstance().buildMediaSource(uri, noCache),
                srtLink.encodeUrl(),
                noCache
            )
        } else {
            ExoFactorySingeleton.getInstance().buildMediaSource(uri, noCache)
        }
    }



    override fun preparePlayback() {
    }

    override fun onVisibilityChange(visibility: Int) {
        Log.e("msdmdsmd vischange", "$visibility $useController ${customController != null}")
        //controlController(visibility)
    }

    override fun start() {
        Log.e(
            "msdmdsmd start",
            "${reachedEndOfVideo()}${playerManager?.exoPlayer?.currentPosition} ${playerManager?.exoPlayer?.duration} "
        )

        if (reachedEndOfVideo()) {
            seekTo(0)
        } else
            seekTo(lastPos_)

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


    override val canHaveFullScreen: Boolean
        get() = playerUiFinalListener?.canMinimize() != false


    override fun toggleFullScreen() {
        playerUiFinalListener?.setScreenOrentation()
    }

    override fun canShowController(useController_: Boolean) {
        Log.e("canShowController", "$useController_")
        useController = useController_
        if (!useController_)
            customController?.hide()
    }

    override fun minimizeAble(): Boolean {
        return playerUiFinalListener?.canMinimize() == true && playerUiFinalListener?.isInFullScreen() != true
    }

    override fun minmize() {
        playerUiFinalListener?.doMinimizePlayer()
    }

    fun init(finaluilistener: CineamaticPlayerScreen) {
        playerUiFinalListener = finaluilistener
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        //  Log.e("CEP", "dispatchTouchEvent $childCount ")
        if (customController?.isShowing == true) {//|| playerUiFinalListener?.isInFullScreen() == true) {
            for (i in 0..childCount) {
                getChildAt(i)?.let {
                    val consumed = it.dispatchTouchEvent(ev)
                    if (consumed) {
                        return consumed
                    }
                }
            }
            // val x = super.dispatchTouchEvent(ev)
            //   Log.e("CEP", "dispatchTouchEvent end   $x")
//			   return x
        }
        return if (ev?.touchEventInsideTargetView(this) == true)
            super.dispatchTouchEvent(ev) //
        else
            false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (doubleTapActivated) {
            mDetector?.onTouchEvent(event)
            // Do not trigger original behavior when double tapping
            // otherwise the controller would show/hide - it would flack
        }

        return false
    }

    internal var isDoubleTap = false
    private var doubleTapActivated = true
    private var mDetector: GestureDetectorCompat? = null
    var controls: PlayerDoubleTapListener? = null

    // Variable to save current state
    private val mHandler = Handler()
    private val mRunnable = Runnable {
        Log.d(TAG, "Runnable called")
        isDoubleTap = false
        controls?.onDoubleTapFinished()
    }
    var doubleTapDelay: Long = 650

    /**
     * Sets whether the PlayerView recognizes double tap gestures or not
     */
    private fun activateDoubleTap(active: Boolean) {
        this.doubleTapActivated = active
    }

    fun setDoubleTapListener(layout: PlayerDoubleTapListener?) {
        if (layout != null) {
            controls = layout
        }
    }

    /**
     * Resets the timeout to keep in double tap mode.
     *
     * Called once in [PlayerDoubleTapListener.onDoubleTapStarted] Needs to be called
     * from outside if the double tap is customized / overridden to detect ongoing taps
     */
    fun keepInDoubleTapMode() {
        isDoubleTap = true
        mHandler.removeCallbacks(mRunnable)
        mHandler.postDelayed(mRunnable, doubleTapDelay)
    }

    /**
     * Cancels double tap mode instantly by calling [PlayerDoubleTapListener.onDoubleTapFinished]
     */
    fun cancelInDoubleTapMode() {
        mHandler.removeCallbacks(mRunnable)
        isDoubleTap = false
        controls?.onDoubleTapFinished()
    }


    private fun ffwdRewd() {
        Log.e(Tag, "View Double Tapped")
    }

    fun setDoubleTapActivated() {
        rootView.ytController?.apply {
            animationDuration = 800
            fastForwardRewindDuration = 10000
            seekListener = object : SeekListener {
                override fun onVideoStartReached() {
                    pause()
                    playerUiFinalListener?.onMessageRecived(
                        context.getString(R.string.video_start_reached),
                        -1
                    )
                }

                override fun onVideoEndReached() {
                    playerUiFinalListener?.onMessageRecived(
                        context.getString(R.string.video_end_reached),
                        -1
                    )
                }
            }
            performListener = object : YouTubeOverlay.PerformListener {
                override fun onAnimationStart() {
                    // Do UI changes when double tapping / animation starts including showing the overlay
//						 playerView?.useController = false
                    rootView.ytController?.visibility = View.VISIBLE
                }

                override fun onAnimationEnd() {
                    // Do UI changes when double tap animation ends including hiding the overlay
                    rootView.ytController?.visibility = View.GONE
                    // if (!player?.playWhenReady!!) this@CinamaticExoPlayer.showController()
                }
            }
        }

        this.activateDoubleTap(true)
        this.setDoubleTapListener(rootView.ytController)
    }

    fun castCurrent(): Array<MediaQueueItem> {
        return CastUtil.castThis(
            context,
            nowPlaying?.title,
            nowPlaying?.videoLink,
            nowPlaying!!.geners,
            nowPlaying!!.poster,
            nowPlaying!!.runtime,
            isSreaming(),
            getLastPos("castcurrent"),
            subtitleLink = nowPlaying!!.vttLink
        )
    }

    fun fullSize() {
        applyHeight(ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun minSize() {
        applyHeight(realHeight)
    }

    var isPlayerReady: Boolean = false

    /**
     * Gesture Listener for double tapping
     *
     * For more information which methods are called in certain situations look for
     * [GestureDetectorCompat.onTouchEvent], especially for ACTION_DOWN and ACTION_UP
     */
    inner class DoubleTapGestureListener : GestureDetector.SimpleOnGestureListener() {

        fun canDoubleTab(): Boolean {
            return videoOverlayView?.isMinimized() != true && !isSreaming() && isPlayerReady
        }

        override fun onDown(e: MotionEvent): Boolean {
            // Used to override the other methods
            if (isDoubleTap && canDoubleTab()) {
                controls?.onDoubleTapProgressDown(e.x, e.y)
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isDoubleTap && canDoubleTab()) {
                if (DEBUG) Log.d(TAG, "onSingleTapUp: isDoubleTap = true")
                controls?.onDoubleTapProgressUp(e.x, e.y)
            }
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            // Ignore this event if double tapping is still active
            // Return true needed because this method is also called if you tap e.g. three times
            // in a row, therefore the controller would appear since the original behavior is
            // to hide and show on single tap
            if (isDoubleTap) return true
            if (DEBUG) Log.d(TAG, "onSingleTapConfirmed: isDoubleTap = false")
            customController?.toggleShowHide()
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // First tap (ACTION_DOWN) of both taps
            if (DEBUG) Log.d(TAG, "onDoubleTap")
            if (!isDoubleTap) {
                isDoubleTap = true
                keepInDoubleTapMode()
                if (canDoubleTab())
                    controls?.onDoubleTapStarted(e.x, e.y)
            }
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            // Second tap (ACTION_UP) of both taps
            if (e.actionMasked == MotionEvent.ACTION_UP && isDoubleTap) {
                if (DEBUG) Log.d(TAG, "onDoubleTapEvent, ACTION_UP")
                if (playerManager?.isConnected() != true && canDoubleTab()) {
                    customController?.hide()
                    controls?.onDoubleTapProgressUp(e.x, e.y)
                }
                return true
            }
            return super.onDoubleTapEvent(e)
        }
    }
}

fun Int.DpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

private fun CinamaticExoPlayer.getCastContext(): CastContext? {
    return (this.context.applicationContext as? CastApp)?.mCastContext
}


private fun Context.isCastConnected(): Boolean {
    return (this.applicationContext as? CastApp)?.isCastConnected() == true
}
