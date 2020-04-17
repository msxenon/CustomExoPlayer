package com.appchief.msa.floating_player

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import com.appchief.msa.exoplayerawesome.CinamaticExoPlayer
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton
import com.appchief.msa.exoplayerawesome.R
import com.appchief.msa.exoplayerawesome.databinding.AppchiefFloatingPlayerBinding
import com.appchief.msa.exoplayerawesome.databinding.AppchiefTvPlayerBinding

// https://medium.com/vrt-digital-studio/picture-in-picture-video-overlay-with-motionlayout-a9404663b9e7
class VideoOverlayView @JvmOverloads constructor(
	 context: Context,
	 attrs: AttributeSet? = null,
	 defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	 init {
		  //Set whether this view can receive the focus
		  this.focusable = View.FOCUSABLE
		  //When a view is focusable, it may not want to take focus when in touch mode.
		  //For example, a button would like focus when the user is navigating via a D-pad
		  //so that the user can click on it, but once the user starts touching the screen,
		  //the button shouldn't take focus
		  this.isFocusableInTouchMode = true
	 }

	 override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		  playerContainer?.customController?.dispatchKeyEvent(event)

		  return super.dispatchKeyEvent(event)
	 }

	 var motionLayout: MotionLayout? = null

	 // var player: CinamaticExoPlayer? = null
	 var playerContainer: CinamaticExoPlayer? = null
	 private var startX: Float? = null
	 private var startY: Float? = null
	 fun setPortraitVideoHight(height: Int) {
		  Log.e("setPortraitVideoHightA", "$height")
		  motionLayout?.getConstraintSet(R.id.start)?.let {
			   it.constrainHeight(R.id.motionInteractView, height)
			   motionLayout?.updateState()
			   Log.e("setPortraitVideoHightB", "$height")

		  }
	 }
	 override fun onFinishInflate() {
		  super.onFinishInflate()
		  this.requestFocus()
		  //LayoutInflater.from(context).inflate(R.layout.layout_detail, this, false) as MotionLayout
		  if (motionLayout == null) {
			   //   (rootView as ViewGroup) .removeAllViews()
			   Log.e("VOV", "istv ${ExoFactorySingeleton.isTv}")
			   motionLayout = (if (ExoFactorySingeleton.isTv) AppchiefTvPlayerBinding.inflate(
					LayoutInflater.from(context)
			   ).containerMotionLayout else AppchiefFloatingPlayerBinding.inflate(
					LayoutInflater.from(
						 context
					)
			   ).containerMotionLayout)



			   addView(motionLayout)
//			   player =
//					motionLayout?.findViewById(com.appchief.msa.exoplayerawesome.R.id.motionInteractView)
			   playerContainer =
					motionLayout?.findViewById(com.appchief.msa.exoplayerawesome.R.id.motionInteractView)
			   playerContainer?.videoOverlayView = this
		  }
	 }

	 fun isInProgress(): Boolean {
		  try {
			   return (motionLayout!!.progress > 0.0f && motionLayout!!.progress < 1.0f)
		  } catch (e: Exception) {
			   e.printStackTrace()
			   return false
		  }
	 }
	 override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
		  Log.e("VVO", "onInterceptTouchEvent ${motionLayout == null} ")
		  if (motionLayout == null)
			   return false
		  val isInProgress = isInProgress()
		  val isInTarget = touchEventInsideTargetViewExceptTop(playerContainer!!, ev)
		  val touchingTarget = ev.touchEventInsideTargetView(playerContainer)
		  Log.e("VVO", "onInterceptTouchEvent 2 $isInProgress  $isInTarget $touchingTarget")
		  return if (isInProgress || isInTarget || touchingTarget) {
			   super.onInterceptTouchEvent(ev)
		  } else {
			   true
		  }
	 }



	 private fun touchEventInsideTargetViewExceptTop(v: View, ev: MotionEvent): Boolean {
//		  var x = false
//		  if (ev.x > v.left && ev.x < v.right) {
//			   if (ev.y > v.top &&ev.y < v.bottom && !isMinimized()) {
//					x =  true
//			   }
//		  }
		  Log.e(
			   "VOV",
			   "top ${isMinimized()}   kj $x ${ev.x} ${ev.y} ${v.left} ${v.top} ${v.right} ${v.bottom}"
		  )
		  return !isMinimized() && !isInFullScreen()
	 }

	 override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
		  try {//		  Log.e("VVO", "dispatchTouchEvent ${motionLayout == null} ")
			   if (playerContainer != null && motionLayout != null) {
					val consumed = playerContainer!!.dispatchTouchEvent(ev)
					//			   Log.e("VVO", "dispatchTouchEvent c1 ${consumed}  ")
					if (consumed) {
						 return consumed
					}
					if (ev.touchEventInsideTargetView(playerContainer)) {
						 when (ev.action) {
							  MotionEvent.ACTION_DOWN -> {
								   startX = ev.x
								   startY = ev.y
							  }
							  MotionEvent.ACTION_UP -> {
								   if (startX != null && startY != null) {
										val endX = ev.x
										val endY = ev.y
										if (isClick(startX!!, endX, startY!!, endY)) {
											 if (motionLayout!!.currentState == motionLayout!!.startState) {
												  //	 Log.e("VVO", "dispatchTouchEvent preform pte ")
												  playerContainer!!.performClick()
											 }
											 if (doClickTransition()) {
												  //	 Log.e("VVO", "dispatchTouchEvent c2 doclick ")
												  return true
											 }
										}
								   }
							  }
						 }
					}
			   }

			   return super.dispatchTouchEvent(ev)
		  } catch (e: Exception) {
			   return false
		  }
	 }

	 private fun doClickTransition(): Boolean {
		  var isClickHandled = false
//		  if (motionLayout != null) {
//			   if (motionLayout!!.progress < 0.05F) {
//					//            motionLayout.transitionToEnd()
////            isClickHandled = true
//			   } else if (motionLayout!!.progress > 0.95F) {
//					//motionLayout!!.transitionToStart()
//					isClickHandled = true
//			   }
//		  }
		  return isClickHandled
	 }

	 private fun isClick(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
		  val differenceX = Math.abs(startX - endX)
		  val differenceY = Math.abs(startY - endY)
		  return !/* =5 */(differenceX > 200 || differenceY > 200)
	 }

	 @SuppressLint("ClickableViewAccessibility")
	 override fun onTouchEvent(event: MotionEvent?): Boolean {
		  return false
	 }

	 fun isMinimized(): Boolean {
		  Log.e(
			   "mmm",
			   "${com.appchief.msa.exoplayerawesome.R.id.end} ${motionLayout?.currentState}"
		  )
		  return motionLayout?.currentState == com.appchief.msa.exoplayerawesome.R.id.end || motionLayout?.currentState == -1
	 }

	 fun isInFullScreen(): Boolean {
		  return playerContainer?.playerUiFinalListener?.isInFullScreen() == true
	 }
	 fun minimize() {
		  motionLayout?.transitionToState(com.appchief.msa.exoplayerawesome.R.id.end)
	 }
}

fun MotionEvent.touchEventInsideTargetView(v: View?): Boolean {
	 var x = false
	 if (v == null)
		  return x
	 if (this.x > v.left && this.x < v.right) {
		  if (this.y > v.top && this.y < v.bottom) {
			   x = true
		  }
	 }
//	 Log.e(
//		  "EXT",
//		  "target touched$x ex${ev.x} ey${ev.y} ${v.left} ${v.top} ${v.right} ${v.bottom}"
//	 )
	 return x
}