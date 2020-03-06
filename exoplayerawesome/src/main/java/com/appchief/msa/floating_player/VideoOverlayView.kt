package com.appchief.msa.floating_player

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import com.appchief.msa.exoplayerawesome.CinamaticExoPlayer
import com.appchief.msa.exoplayerawesome.databinding.AppchiefFloatingPlayerBinding
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

// https://medium.com/vrt-digital-studio/picture-in-picture-video-overlay-with-motionlayout-a9404663b9e7
class VideoOverlayView @JvmOverloads constructor(
	 context: Context,
	 attrs: AttributeSet? = null,
	 defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	 var motionLayout: MotionLayout? = null
	 var player: CinamaticExoPlayer? = null
	 var playerContainer: AspectRatioFrameLayout? = null
	 private var startX: Float? = null
	 private var startY: Float? = null
	 //	 override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
//		  super.onLayout(changed, left, top, right, bottom)
//		  if (changed){
//			   player =
//					motionLayout?.findViewById(com.appchief.msa.exoplayerawesome.R.id.playerView)
//		  }
//	 }
	 override fun onFinishInflate() {
		  super.onFinishInflate()
		  //LayoutInflater.from(context).inflate(R.layout.layout_detail, this, false) as MotionLayout
		  if (motionLayout == null || player == null) {
			   removeAllViews()
			   motionLayout = AppchiefFloatingPlayerBinding.inflate(LayoutInflater.from(context))
					.containerMotionLayout
			   addView(motionLayout)
			   player =
					motionLayout?.findViewById(com.appchief.msa.exoplayerawesome.R.id.playerView)
			   playerContainer =
					motionLayout?.findViewById(com.appchief.msa.exoplayerawesome.R.id.motionInteractView)
			   //   player = playerView//motionLayout?.findViewById(R.id.playerView)
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
		  val isInTarget = touchEventInsideTargetViewExceptTop(player!!, ev)
		  val touchingTarget = touchEventInsideTargetView(playerContainer!!, ev)
		  Log.e("VVO", "onInterceptTouchEvent 2 $isInProgress  $isInTarget $touchingTarget")
		  return if (isInProgress || isInTarget || touchingTarget) {
			   super.onInterceptTouchEvent(ev)
		  } else {
			   true
		  }
	 }

	 private fun touchEventInsideTargetView(v: View, ev: MotionEvent): Boolean {
		  var x = false
		  if (ev.x > v.left && ev.x < v.right) {
			   if (ev.y > v.top && ev.y < v.bottom) {
					x = true
			   }
		  }
		  Log.e(
			   "VOV",
			   "target touched$x ex${ev.x} ey${ev.y} ${v.left} ${v.top} ${v.right} ${v.bottom}"
		  )

		  return x
	 }

	 private fun touchEventInsideTargetViewExceptTop(v: View, ev: MotionEvent): Boolean {
//		  var x = false
//		  if (ev.x > v.left && ev.x < v.right) {
//			   if (ev.y > v.top &&ev.y < v.bottom && !isMinimized()) {
//					x =  true
//			   }
//		  }
		  Log.e("VOV", "top $x ${ev.x} ${ev.y} ${v.left} ${v.top} ${v.right} ${v.bottom}")
		  return !isMinimized()
	 }

	 override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//		  Log.e("VVO", "dispatchTouchEvent ${motionLayout == null} ")
		  if (player != null && motionLayout != null) {
			   val consumed = player!!.dispatchTouchEvent(ev)
//			   Log.e("VVO", "dispatchTouchEvent c1 ${consumed}  ")
			   if (consumed) {
					return consumed
			   }
			   if (touchEventInsideTargetView(player!!, ev)) {
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
										if (motionLayout!!.currentState == motionLayout!!.startState || motionLayout!!.currentState == com.appchief.msa.exoplayerawesome.R.id.fullScreen) {
											 Log.e("VVO", "dispatchTouchEvent preform pte ")
											 player!!.performClick()
										}
										if (doClickTransition()) {
											 Log.e("VVO", "dispatchTouchEvent c2 doclick ")
											 return true
										}
								   }
							  }
						 }
					}
			   }
		  }

		  return super.dispatchTouchEvent(ev)
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
		  return motionLayout?.currentState == com.appchief.msa.exoplayerawesome.R.id.end
	 }
	 fun minimize() {
		  motionLayout?.transitionToEnd()
	 }
}