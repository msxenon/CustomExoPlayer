package com.appchief.msa.exoplayerawesome.viewcontroller

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.appchief.msa.exoplayerawesome.CinamaticExoPlayer
import com.appchief.msa.exoplayerawesome.R
import com.appchief.msa.exoplayerawesome.listeners.MediaPlayerControl
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max

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
class VideoControllerView : FrameLayout {

	 private var mPlayer: MediaPlayerControl? =
		  null
	 private var mContext: Context
	 private var mAnchor: ViewGroup? = null
	 private var mRoot: View? = null
	 private var mProgress: SeekBar? = null
	 private var mEndTime: TextView? = null
	 private var mDownPlayer: ImageButton? = null
	 private var mCurrentTime: TextView? = null
	 var isShowing = false
		  private set
	 private var mDragging = false
	 private var mUseFastForward: Boolean
	 private var mFromXml = false
	 private var mListenersSet = false
	 private var mNextListener: OnClickListener? = null
	 private var mPrevListener: OnClickListener? = null
	 var mFormatBuilder: StringBuilder? = null
	 var mFormatter: Formatter? = null
	 private var mPauseButton: ImageButton? = null
	 private var mFfwdButton: ImageButton? = null
	 private var mRewButton: ImageButton? = null
	 private var mNextButton: ImageButton? = null
	 private var mPrevButton: ImageButton? = null
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
	 }

	 fun setMediaPlayer(player: MediaPlayerControl?) {
		  mPlayer = player
	 }

	 private fun controlVis() {
		  Log.e("VCV", "${mPlayer?.canSeekBackward()}")
		  if (mPlayer?.canSeekForward() != true) {
			   mProgress?.visibility = View.GONE
			   mEndTime?.visibility = View.GONE
			   mFfwdButton?.visibility = View.GONE
			   mRewButton?.visibility = View.GONE
			   mCurrentTime?.visibility = View.GONE
			   mPauseButton?.visibility = View.GONE
			   return
		  }
		  if (mPlayer?.hasNext() != true) {
			   mNextButton?.visibility = View.GONE
		  }
		  if (mPlayer?.isFirstItem() != false) {
			   mPrevButton?.visibility = View.GONE
		  }
		  setProgress()
		  mDownPlayer?.visibility = mPlayer?.minimizeAble().controlVisibility()
	 }

	 /**
	  * Set the view that acts as the anchor for the control view.
	  * This can for example be a VideoView, or your Activity's main view.
	  * @param view The view to which to anchor the controller when it is visible.
	  */
	 fun setAnchorView(view: CinamaticExoPlayer, title: String?, controllerLayout: Int?) {
		  mAnchor = view
		  val frameParams = LayoutParams(
			   ViewGroup.LayoutParams.MATCH_PARENT,
			   ViewGroup.LayoutParams.MATCH_PARENT
		  )
		  removeAllViews()
		  val v = makeControllerView(controllerLayout)
		  addView(v, frameParams)
		  title?.let {
			   v?.findViewById<TextView>(R.id.video_title)?.apply {
					text = title
					visibility = View.VISIBLE
			   }
		  }
		  setMediaPlayer(view)
	 }

	 /**
	  * Create the view that holds the widgets that control playback.
	  * Derived classes can override this to create their own.
	  * @return The controller view.
	  * @hide This doesn't work as advertised
	  */
	 protected fun makeControllerView(controllerLayout: Int?): View? {
		  val inflate = LayoutInflater.from(mContext)

		  mRoot = inflate.inflate(controllerLayout ?: R.layout.controllerui, null)

		  initControllerView(mRoot!!)
		  return mRoot
	 }

	 private fun initControllerView(v: View) {
		  mPauseButton = v.findViewById(R.id.exo_play_pause) as ImageButton
		  if (mPauseButton != null) {
			   mPauseButton!!.requestFocus()
			   mPauseButton!!.setOnClickListener(mPauseListener)
		  }
		  mNextButton = v.findViewById(R.id.exo_next)
		  mPrevButton = v.findViewById(R.id.exo_prev)
		  mFfwdButton = v.findViewById<View>(R.id.exo_ffwd) as ImageButton
		  if (mFfwdButton != null) {
			   mFfwdButton!!.setOnClickListener(mFfwdListener)
			   if (!mFromXml) {
					mFfwdButton!!.visibility = if (mUseFastForward) View.VISIBLE else View.GONE
			   }
		  }
		  mRewButton = v.findViewById<View>(R.id.exo_rew) as ImageButton
		  if (mRewButton != null) {
			   mRewButton!!.setOnClickListener(mRewListener)
			   if (!mFromXml) {
					mRewButton!!.visibility = if (mUseFastForward) View.VISIBLE else View.GONE
			   }
		  }


		  mProgress =
			   v.findViewById<View>(R.id.exo_progress) as SeekBar
		  if (mProgress != null) {
			   if (mProgress is SeekBar) {
					val seeker = mProgress as SeekBar
					seeker.setOnSeekBarChangeListener(mSeekListener)
			   }
			   mProgress!!.max = 1000
		  }
		  mEndTime = v.findViewById<View>(R.id.exo_duration) as TextView
		  mCurrentTime = v.findViewById<View>(R.id.exo_position) as TextView
		  mDownPlayer = v.findViewById(R.id.downPlayer)
		  mDownPlayer?.setOnClickListener { mPlayer?.minmize() }
		  mFormatBuilder = StringBuilder()
		  mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
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
			   if (mPauseButton != null && !mPlayer!!.canPause()) {
					mPauseButton!!.isEnabled = false
			   }
			   if (mRewButton != null && !mPlayer!!.canSeekBackward()) {
					mRewButton!!.isEnabled = false
			   }
			   mDownPlayer?.isEnabled = mPlayer!!.minimizeAble()
			   if (mFfwdButton != null && !mPlayer!!.canSeekForward()) {
					mFfwdButton!!.isEnabled = false
			   }
		  } catch (ex: IncompatibleClassChangeError) { // We were given an old version of the interface, that doesn't have
// the canPause/canSeekXYZ methods. This is OK, it just means we
// assume the media can be paused and seeked, and so we don't disable
// the buttons.
		  }
	 }

	 @JvmOverloads
	 fun show(timeout: Int = sDefaultTimeout) {
		  Log.e("VCV", "showcalled")
		  if (!isShowing && mAnchor != null) {
			   setProgress()
			   if (mPauseButton != null) {
					mPauseButton!!.requestFocus()
			   }
			   disableUnsupportedButtons()
			   val tlp = LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT,
					Gravity.BOTTOM
			   )
			   mAnchor!!.addView(this, tlp)
			   isShowing = true
		  }
		  updatePausePlay()
		  updateFullScreen()
		  // cause the progress bar to be updated even if mShowing
		  // was already true.  This happens, for example, if we're
		  // paused with the progress bar showing the user hits play.
		  mHandler.sendEmptyMessage(SHOW_PROGRESS)
		  val msg =
			   mHandler.obtainMessage(FADE_OUT)
		  if (timeout != 0) {
			   mHandler.removeMessages(FADE_OUT)
			   mHandler.sendMessageDelayed(msg, timeout.toLong())
		  }
	 }

	 /**
	  * Remove the controller from the screen.
	  */
	 fun hide() {
		  if (mAnchor == null || mDragging) {
			   return
		  }
		  try {
			   mAnchor!!.removeView(this)
			   mHandler.removeMessages(SHOW_PROGRESS)
		  } catch (ex: IllegalArgumentException) {
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

		  mFormatBuilder!!.setLength(0)
		  val m = if (hours > 0) {
			   mFormatter!!.format("%d:%02d:%02d", hours, minutes, seconds).toString()
		  } else {
			   mFormatter!!.format("%02d:%02d", minutes, seconds).toString()
		  }
		  Log.e("VCV", "stringForTime $m $timeMs")
		  return m
	 }

	 private fun setProgress(): Long {
		  if (mPlayer == null || mDragging) {
			   return 0
		  }
		  val position = mPlayer!!.currentPosition
		  val duration = mPlayer!!.duration
		  if (mProgress != null) {
			   if (duration > 0) { // use long to avoid overflow
					val pos = 1000L * position / duration
					mProgress!!.progress = pos.toInt()
			   }
			   val percent = mPlayer?.bufferPercentage
			   mProgress?.secondaryProgress = percent ?: 0 * 10
		  }
		  mEndTime?.text = stringForTime(duration)
		  if (mCurrentTime != null) mCurrentTime!!.text = stringForTime(position)
		  return position
	 }

	 //	 override fun onTouchEvent(event: MotionEvent): Boolean {
//		  show(sDefaultTimeout)
//		  return true
//	 }
	 override fun onTrackballEvent(ev: MotionEvent): Boolean {
		  show(sDefaultTimeout)
		  return false
	 }

	 override fun dispatchKeyEvent(event: KeyEvent): Boolean {
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

	 private val mPauseListener =
		  OnClickListener {
			   doPauseResume()
			   show(sDefaultTimeout)
		  }
	 private val mFullscreenListener =
		  OnClickListener {
			   doToggleFullscreen()
			   show(sDefaultTimeout)
		  }

	 fun updatePausePlay() {
		  if (mRoot == null || mPauseButton == null || mPlayer == null) {
			   return
		  }
		  if (mPlayer!!.isPlaying) {
			   mPauseButton!!.setImageResource(R.drawable.exo_controls_pause)
		  } else {
			   mPauseButton!!.setImageResource(R.drawable.exo_icon_play)
		  }
	 }

	 fun updateFullScreen() {
	 }

	 private fun doPauseResume() {
		  if (mPlayer == null) {
			   return
		  }
		  if (mPlayer!!.isPlaying) {
			   mPlayer!!.pause()
		  } else {
			   mPlayer!!.start()
		  }
		  updatePausePlay()
	 }

	 private fun doToggleFullscreen() {
		  if (mPlayer == null) {
			   return
		  }
		  mPlayer!!.toggleFullScreen()
	 }

	 // There are two scenarios that can trigger the seekbar listener to trigger:
//
// The first is the user using the touchpad to adjust the posititon of the
// seekbar's thumb. In this case onStartTrackingTouch is called followed by
// a number of onProgressChanged notifications, concluded by onStopTrackingTouch.
// We're setting the field "mDragging" to true for the duration of the dragging
// session to avoid jumps in the position in case of ongoing playback.
//
// The second scenario involves the user operating the scroll ball, in this
// case there WON'T BE onStartTrackingTouch/onStopTrackingTouch notifications,
// we will simply apply the updated position without suspending regular updates.
	 private val mSeekListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
		  override fun onStartTrackingTouch(bar: SeekBar) {
			   show(3600000)
			   mDragging = true
			   // By removing these pending progress messages we make sure
// that a) we won't update the progress while the user adjusts
// the seekbar and b) once the user is done dragging the thumb
// we will post one of these messages to the queue again and
// this ensures that there will be exactly one message queued up.
			   //  mHandler.removeMessages(SHOW_PROGRESS)
		  }

		  override fun onProgressChanged(
			   bar: SeekBar,
			   progress: Int,
			   fromuser: Boolean
		  ) {
			   if (mPlayer == null) {
					return
			   }
			   if (!fromuser) { // We're not interested in programmatically generated changes to
// the progress bar's position.
					return
			   }
			   val duration = mPlayer!!.duration
			   val newposition = duration * progress / 1000L
			   mPlayer!!.seekTo(newposition)
			   if (mCurrentTime != null) mCurrentTime!!.text = stringForTime(newposition)
		  }

		  override fun onStopTrackingTouch(bar: SeekBar) {
			   Log.e("VCV", "onStopTrackingTouch")
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

	 override fun setEnabled(enabled: Boolean) {
		  if (mPauseButton != null) {
			   mPauseButton!!.isEnabled = enabled
		  }
		  if (mFfwdButton != null) {
			   mFfwdButton!!.isEnabled = enabled
		  }
		  if (mRewButton != null) {
			   mRewButton!!.isEnabled = enabled
		  }

		  if (mProgress != null) {
			   mProgress!!.isEnabled = enabled
		  }
		  disableUnsupportedButtons()
		  super.setEnabled(enabled)
	 }

	 fun updateViews() {
		  updatePausePlay()
		  updateFullScreen()
		  controlVis()
	 }

	 private val mRewListener =
		  OnClickListener {
			   if (mPlayer == null) {
					return@OnClickListener
			   }
			   var pos = mPlayer!!.currentPosition
			   pos -= 5000 // milliseconds
			   mPlayer!!.seekTo(pos)
			   setProgress()
			   show(sDefaultTimeout)
		  }
	 private val mFfwdListener =
		  OnClickListener {
			   if (mPlayer == null) {
					return@OnClickListener
			   }
			   var pos = mPlayer!!.currentPosition
			   pos += 15000 // milliseconds
			   mPlayer!!.seekTo(pos)
			   setProgress()
			   show(sDefaultTimeout)
		  }

	 private class MessageHandler internal constructor(view: VideoControllerView) :
		  Handler() {

		  private val mView: WeakReference<VideoControllerView>
		  override fun handleMessage(msgj: Message) {
			   var msg = msgj
			   val view = mView.get()
			   if (view == null || view.mPlayer == null) {
					return
			   }
			   Log.e("MessageHandler", "" + msg.toString() + " dragging${view.mDragging}")
			   val pos: Long
			   when (msg.what) {
					FADE_OUT -> view.hide()
					SHOW_PROGRESS -> {
						 pos = view.setProgress()
						 if (!view.mDragging && view.isShowing && view.mPlayer!!.isPlaying) {
							  msg = obtainMessage(SHOW_PROGRESS)
							  sendMessageDelayed(msg, 1000 - (pos % 1000).toLong())
						 }
					}
			   }
		  }

		  init {
			   mView = WeakReference(view)
		  }
	 }

	 companion object {
		  private const val TAG = "VideoControllerView"
		  private const val sDefaultTimeout = 2000
		  private const val FADE_OUT = 1
		  private const val SHOW_PROGRESS = 2
	 }
}

private fun Boolean?.controlVisibility(): Int {
	 return if (this == true) View.VISIBLE else View.GONE
}
