package com.appchief.msa.exoplayerawesome.viewcontroller

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.appchief.msa.exoplayerawesome.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.gms.cast.framework.CastButtonFactory
import kotlinx.android.synthetic.main.controllerui.view.*
import java.lang.StrictMath.max
import java.lang.ref.WeakReference
import java.util.*

/**
 * A view containing controls for a MediaPlayer. Typically contains the
 * buttons like "Play/Pause", "Rewind", "Fast Forward" and a progress
 * slider. It takes care of synchronizing the controls with the state
 * of the MediaPlayer.
 *
 *
 * The way to use this class is to instantiate it programatically.
 * The MediaController will create a default set of controls
 * and put them in a window floating above your application. Specifically,
 * the controls will float above the view specified with setAnchorView().
 * The window will disappear if left idle for three seconds and reappear
 * when the user touches the anchor view.
 *
 *
 * Functions like show() and hide() have no effect when MediaController
 * is created in an xml layout.
 *
 * MediaController will hide and
 * show the buttons according to these rules:
 *
 *  *  The "previous" and "next" buttons are hidden until setPrevNextListeners()
 * has been called
 *  *  The "previous" and "next" buttons are visible but disabled if
 * setPrevNextListeners() was called with null listeners
 *  *  The "rewind" and "fastforward" buttons are shown unless requested
 * otherwise by using the MediaController(Context, boolean) constructor
 * with the boolean set to false
 *
 */
enum class ControllerVisState {

	 Normal, Cast
}

abstract class VideoControllerView : FrameLayout {
	 var currentState = ControllerVisState.Normal
		  set(value) {
			   field = value
			   controlVis()
		  }

	 open fun onItemEndReached() {
	 }

	 var mPlayer: CinamaticExoPlayer? =
		  null
	 lateinit var player: Player
	 private var mContext: Context? = null
	 private var mAnchor: ViewGroup? = null
	 private var mRoot: View? = null
	 private var mProgress: SeekBar? = null
	 private var mEndTime: TextView? = null
	 private var mDownPlayer: ImageButton? = null
	 private var mCurrentTime: TextView? = null
	 private var mVideoSettings: ImageButton? = null
	 var lastShowingChange: Long = 0
	 var isShowing = false
		  private set(value) {
			   field = value
			   lastShowingChange = System.currentTimeMillis()
			   Log.e("VCV", "isShowing $value")
		  }
	 private var mDragging = false
	 private var mUseFastForward: Boolean = false
	 private var mFromXml = false
	 var mFormatBuilder: StringBuilder? = null
	 var mFormatter: Formatter? = null
	 private var fullscreenBtn: ImageButton? = null
	 private var mPauseButton: ImageButton? = null
	 private var mFfwdButton: ImageButton? = null
	 private var mRewButton: ImageButton? = null
	 private var mNext: ImageButton? = null
	 private var mPrev: ImageButton? = null
	 private val mHandler: Handler =
		  MessageHandler(this)

	 constructor(context: Context, attrs: AttributeSet?) : super(
		  context,
		  attrs
	 ) {
		  mRoot = null
		  mContext = context
		  mUseFastForward = true
		  mFromXml = true
		  Log.i(TAG, TAG)
	 }

	 constructor(
		  context: Context,
		  useFastForward: Boolean
	 ) : super(context) {
		  mContext = context
		  mUseFastForward = useFastForward
		  Log.i(TAG, TAG)
	 }

	 constructor(context: Context) : this(context, true) {
		  Log.i(TAG, TAG)
	 }


	 public override fun onFinishInflate() {
		  super.onFinishInflate()
		  if (mRoot != null) initControllerView(mRoot!!)
		  requestFocus()
	 }

	 fun setMediaPlayer(player: CinamaticExoPlayer?) {
		  mPlayer = player
		  this.player = player?.player!!
		  Log.e("VCV", "set buffer--")
//		  val x = Observable.interval(500, TimeUnit.MILLISECONDS)
//			   .subscribeOn(Schedulers.io())
//			   .observeOn(AndroidSchedulers.mainThread())
//			   .map { val estimatedBufferDuration= this.player.contentBufferedPosition.toFloat().div(this.player.duration.toFloat()).times(1000f).toInt()
//					Log.e("VCV","map buffer-- ${this.player.contentBufferedPosition}||$estimatedBufferDuration = ${this.player.bufferedPosition} ${this.player.duration}")
//
//					estimatedBufferDuration}
// 		   .filter { it > 0 }
// 			 .distinctUntilChanged()
//			   .subscribe {
//
//					Log.e("VCV","$it buffer   ${this.player.duration} ${this.player.bufferedPosition} ${this.player .totalBufferedDuration} ")
//					//val x = TimeBar
//					mProgress?.secondaryProgress = it
//			   }
	 }

	 private var moviePoster: ImageView? = null
	 private var imageCast: ImageView? = null

	 open fun controlVis() {
		  val vis =
			   (currentState == ControllerVisState.Normal && mPlayer?.canSeekForward() == true).controlVisibility()
		  mProgress?.visibility = vis
		  mFfwdButton?.visibility = vis
		  mRewButton?.visibility = vis
		  mCurrentTime?.visibility = vis
		  mEndTime?.visibility = vis
		  fullscreenBtn?.visibility = (mPlayer?.canHaveFullScreen).controlVisibility()
		  moviePoster?.visibility = (currentState == ControllerVisState.Cast).controlVisibility()
		  imageCast?.visibility = (currentState == ControllerVisState.Cast).controlVisibility()

		  if (currentState == ControllerVisState.Cast) {
			   mPlayer?.playerUiFinalListener?.setMoviePoster {
				   it?.let {
					   moviePoster?.setImageDrawable(it)
				   }
			   }
		  }

		  mNext?.visibility =
			   (mPlayer?.playerUiFinalListener?.hasNextItem() == true && currentState == ControllerVisState.Normal).controlVisibility()
		  mPrev?.visibility =
			   (mPlayer?.playerUiFinalListener?.hasPrevItem() == true && currentState == ControllerVisState.Normal).controlVisibility()
		  //  Log.e("VCV", "controlVis ${mPlayer?.canSeekBackward()} $vis $currentState")
		  setProgress()
		  updateDownBtn()
		  mPauseButton?.visibility =
			   (player.playbackState == ExoPlayer.STATE_BUFFERING).invertedControlVisibility()

		  mRoot?.exo_cast?.visibility =
			   (mPlayer?.playerUiFinalListener?.canUseCast() != false).controlVisibility()

	 }

	 private fun settingsVisiability(boolean: Boolean) {
		  val x =
			   (boolean || !isNotCastingMode())//(mPlayer?.hasSettings == true)
		  mVideoSettings?.visibility = x.controlVisibility()
		  mVideoSettings?.setVector(if (isNotCastingMode()) R.drawable.ic_settings_black_24dp else R.drawable.ic_settings_remote_black_24dp)
		  //  Log.e("VCV", " settingsVisiability $x")
	 }
	 /**
	  * Set the view that acts as the anchor for the control view.
	  * This can for example be a VideoView, or your Activity's main view.
	  * @param view The view to which to anchor the controller when it is visible.
	  */
	 fun setAnchorView(view: CinamaticExoPlayer, title: String?, controllerLayout: Int?) {
		  //  Log.e("VCV", "setAnchorView start $controllerLayout")
		  setMediaPlayer(view)
		  mAnchor = view
		  val frameParams = LayoutParams(
			   ViewGroup.LayoutParams.MATCH_PARENT,
			   ViewGroup.LayoutParams.MATCH_PARENT
		  )
		  removeAllViews()
		  moviePoster = ImageView(view.context)
		  moviePoster?.scaleType = ImageView.ScaleType.CENTER_CROP
		  view.addView(moviePoster, frameParams)
		  imageCast = ImageView(view.context)
		  imageCast?.scaleType = ImageView.ScaleType.CENTER_INSIDE
		  imageCast?.setVector(R.drawable.ic_cast_connected_black_24dp)
		  view.addView(imageCast, frameParams)
		  val v = makeControllerView(controllerLayout)
		  addView(v, frameParams)
		  title?.let {
			   v?.findViewById<TextView>(R.id.video_title)?.apply {
					text = title
					visibility = View.VISIBLE
			   }
		  }
		  //	  Log.e("VCV", "setAnchorView end")
	 }

	 /**
	  * Create the view that holds the widgets that control playback.
	  * Derived classes can override this to create their own.
	  * @return The controller view.
	  * @hide This doesn't work as advertised
	  */
	 internal fun makeControllerView(controllerLayout: Int?): View? {
         val inflate = LayoutInflater.from(mContext)

         mRoot = inflate.inflate(
             controllerLayout ?: com.appchief.msa.exoplayerawesome.R.layout.controllerui, null
         )
         initControllerView(mRoot!!)
         CastButtonFactory.setUpMediaRouteButton(context, mRoot!!.exo_cast)
         if (mRoot?.exo_cast == null)
             Log.e("makeControllerView", "Cast button is null")
         else
             Log.d("makeControllerView", "Cast button is good")

         controllerLayout?.let {
             mRoot?.let {
                 mPlayer?.playerUiFinalListener?.addtionalControllerButtonsInit(mRoot)
             }
         }
         return mRoot
     }

	 fun isNotCastingMode(): Boolean {
		  return currentState != ControllerVisState.Cast
	 }

	 open fun initControllerView(v: View) {
		  mPauseButton = v.findViewById(R.id.exo_play_pause) as? ImageButton
		  if (mPauseButton != null) {
			   mPauseButton?.requestFocus()
			   mPauseButton?.setOnClickListener(mPauseListener)
			   mPauseButton.applyFocusStates()
		  }

		  mFfwdButton = v.findViewById<View>(R.id.exo_ffwd) as? ImageButton
		  if (mFfwdButton != null) {
			   mFfwdButton?.setOnClickListener(mFfwdListener)
			   mFfwdButton?.applyFocusStates()
		  }
		  mRewButton = v.findViewById<View>(R.id.exo_rew) as? ImageButton
		  if (mRewButton != null) {
			   mRewButton?.setOnClickListener(mRewListener)
			   mRewButton.applyFocusStates()
		  }
		  mNext = v.findViewById(R.id.mNext) as? ImageButton
		  mNext?.setOnClickListener {
			   mPlayer?.playerUiFinalListener?.playNext()
		  }
		  mNext.applyFocusStates()

		  mPrev = v.findViewById(R.id.mPrev) as? ImageButton
		  mPrev?.setOnClickListener {
			   mPlayer?.playerUiFinalListener?.playPrev()
		  }
		  mPrev.applyFocusStates()
		  fullscreenBtn = v.findViewById(R.id.toggle_fullscreen)
//		  Log.e(
//			   "VCV",
//			   "hass full screen ${fullscreenBtn != null} && ${mPlayer?.canHaveFullScreen}"
//		  )
		  updateFullScreen()
		  mProgress =
			   v.findViewById<View>(R.id.exo_progress) as? SeekBar
		  if (mProgress != null)
			   if (mProgress is SeekBar) {
					val seeker = mProgress
					seeker?.setOnSeekBarChangeListener(mSeekListener)
					mProgress?.max = 1000
					if (ExoFactorySingeleton.isTv)
						 mProgress?.keyProgressIncrement = 1
					Log.e("VCV", "seekbar ${mProgress?.max} ${mProgress?.keyProgressIncrement}")
			   }

		  mEndTime = v.findViewById<View>(R.id.exo_duration) as? TextView
		  mCurrentTime = v.findViewById<View>(R.id.exo_position) as? TextView
		  mDownPlayer = v.findViewById(R.id.downPlayer)
		  mDownPlayer?.setOnClickListener { mPlayer?.minmize() }
		  mFormatBuilder = StringBuilder()
		  mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
		  mVideoSettings = v.findViewById(R.id.video_settings)
		  mVideoSettings?.setOnClickListener {
			   mPlayer?.playerUiFinalListener?.showSettings(currentState == ControllerVisState.Cast)
		  }
		  mPlayer?.hasSettingsListener = object : SettingsListener {
			   override fun hasSettings(has: Boolean) {
					settingsVisiability(has && externalSettingsCondition())
			   }
		  }
		  //  settingsVisiability()
	 }

	 open fun externalSettingsCondition(): Boolean {
		  return true
	 }

	 //	 override fun onTouchEvent(event: MotionEvent?): Boolean {
//		  Log.e("vcv","toucheevent")
//		  return super.onTouchEvent(event)
//	 }
//	 override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//		  return false
//	 }
	 private fun disableUnsupportedButtons() {
		  if (mPlayer == null) {
			   return
		  }
		  try {
			   if (mPauseButton != null && mPlayer?.canPause() != true) {
					mPauseButton?.isEnabled = false
			   }
			   if (mRewButton != null && mPlayer?.canSeekBackward() != true) {
					mRewButton?.isEnabled = false
			   }
			   mDownPlayer?.isEnabled = mPlayer?.minimizeAble() == true
			   if (mFfwdButton != null && mPlayer?.canSeekForward() != true) {
					mFfwdButton?.isEnabled = false
			   }
		  } catch (ex: IncompatibleClassChangeError) {
			   ex.printStackTrace()
			   // We were given an old version of the interface, that doesn't have
// the canPause/canSeekXYZ methods. This is OK, it just means we
// assume the media can be paused and seeked, and so we don't disable
// the buttons.
		  }
	 }

	 @JvmOverloads
	 fun show(timeout: Int = sDefaultTimeout, keep: Boolean = false) {
		  if (mPlayer?.useController != true)
			   return
		  if (!isShowing && mAnchor != null) {
			   setProgress()
			   if (mPauseButton != null) {
					mPauseButton?.requestFocus()
			   }
			   disableUnsupportedButtons()
			   val tlp = LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.BOTTOM
			   )
			   val lastIndex = mAnchor?.childCount ?: 0
			   mAnchor?.addView(this, lastIndex, tlp)
			   isShowing = true
		  }
		  updatePausePlay()
		  updateFullScreen()
		  // cause the progress bar to be updated even if mShowing
		  // was already true.  This happens, for example, if we're
		  // paused with the progress bar showing the user hits play.
		  if (!keep) {
			   mHandler.sendEmptyMessage(SHOW_PROGRESS)
			   val msg =
					mHandler.obtainMessage(FADE_OUT)
			   if (timeout != 0) {
					mHandler.removeMessages(FADE_OUT)
					mHandler.sendMessageDelayed(msg, timeout.toLong())
			   }
		  }
	 }

	 /**
	  * Remove the controller from the screen.
	  */
	 fun hide() {
		  if (mAnchor == null || mDragging || !isShowing) {
			   return
		  }
		  try {
			   mAnchor?.removeView(this)
			   mHandler.removeMessages(SHOW_PROGRESS)
		  } catch (ex: IllegalArgumentException) {
			   ex.printStackTrace()
			   Log.w("MediaController", "already removed")
		  }
		  isShowing = false
	 }

	 private fun stringForTime(timeMsO: Long): String {
		  val timeMs = max(0, timeMsO)
		  val totalSeconds = timeMs / 1000
		  val seconds = totalSeconds % 60
		  val minutes = totalSeconds / 60 % 60
		  val hours = totalSeconds / 3600

		  mFormatBuilder?.setLength(0)
		  val m = if (hours > 0) {
			   mFormatter?.format("%d:%02d:%02d", hours, minutes, seconds).toString()
		  } else {
			   mFormatter?.format("%02d:%02d", minutes, seconds).toString()
		  }
		  //  Log.e("VCV", "stringForTime $m $timeMs")
		  return m
	 }

	 private fun setProgress(): Long {
		  if (mPlayer == null || mDragging) {
			   return 0
		  }
		  val position = player.currentPosition
		  val duration = player.duration
		  if (mProgress != null) {
			   if (duration > 0) { // use long to avoid overflow
					val pos = 1000L * position / duration
					mProgress?.progress = pos.toInt()
			   }
		  }
		  mEndTime?.text = stringForTime(duration)
		  if (mCurrentTime != null) mCurrentTime?.text = stringForTime(position)
		  return position
	 }
////todo
//	 	 override fun onTouchEvent(event: MotionEvent): Boolean {
//		  show(sDefaultTimeout)
//		  return true
//	 }
//	 override fun onTrackballEvent(ev: MotionEvent): Boolean {
//		  show(sDefaultTimeout)
//		  return false
//	 }
//
	 private val mPauseListener =
		  OnClickListener {
			   doPauseResume()
			   show(sDefaultTimeout)
		  }
	 private val mFullscreenListener =
		  OnClickListener {
			   Log.e("VCV", "fulls clicklistener")
			   doToggleFullscreen()
			   show(sDefaultTimeout)
		  }

	 fun updatePausePlay() {
		  if (mRoot == null || mPauseButton == null || mPlayer == null) {
			   return
		  }
		  if (player.isPlaying == true) {
			   mPauseButton?.setImageResource(R.drawable.exo_controls_pause)
		  } else {
			   mPauseButton?.setImageResource(R.drawable.exo_controls_play)
		  }
	 }

	 fun updateFullScreen() {
		  if (mPlayer?.canHaveFullScreen == true) {
			   fullscreenBtn?.setOnClickListener(mFullscreenListener)
		  } else {
			   fullscreenBtn?.visibility = View.GONE
		  }
		  updateDownBtn()
	 }

	 open fun updateDownBtn() {
		  mDownPlayer?.visibility =
			   (mPlayer?.playerUiFinalListener?.isInFullScreen() != true && mPlayer?.minimizeAble() == true).controlVisibility(
					currentState == ControllerVisState.Normal
			   )
	 }

	 private fun doPauseResume() {
		  try {
			   if (mPlayer == null) {
					return
			   }
			   if (player.isPlaying == true) {
					ExoIntent.paused = true
					player.playWhenReady = false
			   } else {
					ExoIntent.paused = false
					player.playWhenReady = true
					if (player.playbackState == ExoPlayer.STATE_ENDED)
						 player.seekTo(0)
			   }
			   updatePausePlay()
		  } catch (e: Exception) {
			   e.printStackTrace()
		  }
	 }

	 private fun doToggleFullscreen() {
		  mPlayer?.toggleFullScreen()
	 }
	 private val mSeekListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
		  override fun onStartTrackingTouch(bar: SeekBar) {
			   show(3600000)
			   mDragging = true
		  }

		  override fun onProgressChanged(
			   bar: SeekBar,
			   progress: Int,
			   fromuser: Boolean
		  ) {
			   try {
					if (mPlayer == null) {
						 return
					}
					if (!fromuser) { // We're not interested in programmatically generated changes to
						 // the progress bar's position.
						 return
					}
					val duration = player.duration
					val newposition = duration * progress / 1000L
					player.seekTo(newposition)
					if (mCurrentTime != null) mCurrentTime?.text = stringForTime(newposition)
			   } catch (e: Exception) {
					e.printStackTrace()
			   }
		  }

		  override fun onStopTrackingTouch(bar: SeekBar) {
			   //   Log.e("VCV", "onStopTrackingTouch")
			   mDragging = false
			   setProgress()
			   updatePausePlay()
			   show(sDefaultTimeout)
			   // Ensure that progress is properly updated in the future,
// the call to show() does not guarantee this because it is a
// no-op if we are already showing.
			   // mHandler.sendEmptyMessage(SHOW_PROGRESS)
		  }
	 }

	 fun updateViews(isLoading: Boolean?) {
		  updatePausePlay()
		  controlVis()
	 }

	 fun toggleShowHide(): Boolean {
		  if (isShowing)
			   hide()
		  else
			   show()
		  return true
	 }

	 fun showLoading(b: Boolean) {
		  mPlayer?.loadingView()?.visibility = b.controlVisibility()
	 }

	 private val mRewListener =
		  OnClickListener {
			   if (player == null) {
					return@OnClickListener
			   }
			   var pos = player.currentPosition
			   pos -= 5000 // milliseconds
			   //   Log.e("taag", "seekto $pos")
			   player.seekTo(pos)
			   setProgress()
			   show(sDefaultTimeout)
		  }
	 private val mFfwdListener =
		  OnClickListener {
			   if (player == null) {
					return@OnClickListener
			   }
			   var pos = player.currentPosition
			   pos += 15000 // milliseconds
			   //   Log.e("taag", "seekto $pos")
			   player.seekTo(pos)
			   setProgress()
			   show(sDefaultTimeout)
		  }

		  private class MessageHandler(viewX: VideoControllerView) :
			   Handler(Looper.getMainLooper()) {

			   private val mView: WeakReference<VideoControllerView> = WeakReference(viewX)
			   override fun handleMessage(msgj: Message) {
					var msg = msgj
					val view = mView.get()
					if (view?.mPlayer == null) {
						 return
					}
					//  Log.e("MessageHandler", "" + msg.toString() + " dragging${view.mDragging}")
			   val pos: Long
			   when (msg.what) {
					FADE_OUT -> view.hide()
					SHOW_PROGRESS -> {
						 pos = view.setProgress()
						 if (!view.mDragging && view.isShowing && view.player.isPlaying == true) {
							  msg = obtainMessage(SHOW_PROGRESS)
							  sendMessageDelayed(msg, 1000 - (pos % 1000).toLong())
						 }
					}
			   }
		  }
	 }

	 companion object {
		  private const val TAG = "VideoControllerView"
		  private const val sDefaultTimeout = 2000
		  private const val FADE_OUT = 1
		  private const val SHOW_PROGRESS = 2
	 }

	 override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		  Log.e("VCV", " dispatchKeyEvent $event")
		  if (mPlayer == null) {
			   return true
		  }
		  val keyCode = event.keyCode
		  val uniqueDown = (event.repeatCount == 0
				  && event.action == KeyEvent.ACTION_DOWN)
		  if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_SPACE
		  ) {
			   if (uniqueDown) {
					doPauseResume()
					show(sDefaultTimeout)
					if (mPauseButton != null) {
						 mPauseButton!!.requestFocus()
					}
			   }
			   return true
		  } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
			   if (uniqueDown && !mPlayer!!.isPlaying) {
					mPlayer!!.start()
					updatePausePlay()
					show(sDefaultTimeout)
			   }
			   return true
		  } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
			   || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
		  ) {
			   if (uniqueDown && mPlayer!!.isPlaying) {
					mPlayer!!.pause()
					updatePausePlay()
					show(sDefaultTimeout)
			   }
			   return true
		  } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
		  ) { // don't show the controls for volume adjustment
			   return super.dispatchKeyEvent(event)
		  } else if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
			   if (uniqueDown) {
					hide()
			   }
			   return true
		  }
		  show(sDefaultTimeout)
		  return super.dispatchKeyEvent(event)
	 }

	 fun canGoBackInTV(): Boolean {
		  val ct = System.currentTimeMillis()
		  val x = System.currentTimeMillis() - lastShowingChange
		  Log.e("VCV", "$ct - $lastShowingChange = $x")
		  return !isShowing && (x) > 3000
	 }
}

fun View?.applyFocusStates() {
	 this?.setBackgroundResource(R.drawable.btn_controller_states)
}

private fun ImageView?.setVector(icSettingsRemoteBlack24dp: Int) {
	 if (this == null)
		  return
	 val x = VectorDrawableCompat.create(
		  this.context.resources, icSettingsRemoteBlack24dp,
		  this.context.theme
	 )
	 this.setImageDrawable(x)
}

fun Boolean?.controlVisibility(
	 anotherBool: Boolean = true,
	 currentState: ControllerVisState? = null
): Int {
	 return if (this == true && (anotherBool || currentState == null || currentState == ControllerVisState.Normal)) View.VISIBLE else View.GONE
}

fun Boolean?.invertedControlVisibility(normalBoolean: Boolean = false): Int {
	 return if (this == false || normalBoolean) View.VISIBLE else View.GONE
}