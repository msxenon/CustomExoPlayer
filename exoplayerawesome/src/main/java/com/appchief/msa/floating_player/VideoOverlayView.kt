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
import com.appchief.msa.exoplayerawesome.R
import com.appchief.msa.exoplayerawesome.databinding.AppchiefFloatingPlayerBinding

// https://medium.com/vrt-digital-studio/picture-in-picture-video-overlay-with-motionlayout-a9404663b9e7
class VideoOverlayView @JvmOverloads constructor(
	 context: Context,
	 attrs: AttributeSet? = null,
	 defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

	 var motionLayout: MotionLayout? = null
	 // var player: CinamaticExoPlayer? = null
	 var playerContainer: CinamaticExoPlayer? = null
	 private var startX: Float? = null
	 private var startY: Float? = null
	 fun setPortraitVideoHight(height: Int) {
		  motionLayout?.getConstraintSet(R.id.start)?.let {
			   it.constrainHeight(R.id.motionInteractView, height)
			   motionLayout?.updateState()
		  }
	 }
	 override fun onFinishInflate() {
		  super.onFinishInflate()
		  //LayoutInflater.from(context).inflate(R.layout.layout_detail, this, false) as MotionLayout
		  if (motionLayout == null) {
			   //   (rootView as ViewGroup) .removeAllViews()
			   motionLayout = AppchiefFloatingPlayerBinding.inflate(LayoutInflater.from(context))
					.containerMotionLayout


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
		  Log.e(
			   "VOV",
			   "top ${isMinimized()}   kj $x ${ev.x} ${ev.y} ${v.left} ${v.top} ${v.right} ${v.bottom}"
		  )
		  return !isMinimized() && !isInFullScreen()
	 }

	 override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//		  Log.e("VVO", "dispatchTouchEvent ${motionLayout == null} ")
		  if (playerContainer != null && motionLayout != null) {
			   val consumed = playerContainer!!.dispatchTouchEvent(ev)
//			   Log.e("VVO", "dispatchTouchEvent c1 ${consumed}  ")
			   if (consumed) {
					return consumed
			   }
			   if (touchEventInsideTargetView(playerContainer!!, ev)) {
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
											 Log.e("VVO", "dispatchTouchEvent preform pte ")
											 playerContainer!!.performClick()
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

	 fun isInFullScreen(): Boolean {
		  return playerContainer?.playerUiFinalListener?.isInFullScreen() == true
	 }
	 fun minimize() {
		  motionLayout?.transitionToEnd()
	 }
}