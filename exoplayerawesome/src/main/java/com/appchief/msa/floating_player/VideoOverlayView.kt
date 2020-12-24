package com.appchief.msa.floating_player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
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
		  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			   this.focusable = View.FOCUSABLE
		  }
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
	internal var playerContainer: CinamaticExoPlayer? = null
	private var startX: Float? = null
	private var startY: Float? = null
	fun setPortraitVideoHight(height: Int) {
		if (!ExoFactorySingeleton.isTv)
			motionLayout?.getConstraintSet(R.id.start)?.let {
				it.constrainHeight(R.id.motionInteractView, height)
				motionLayout?.updateState()
			}
	}

	override fun onFinishInflate() {
		  super.onFinishInflate()
		  this.requestFocus()
		  if (motionLayout == null) {
			   motionLayout = (if (ExoFactorySingeleton.isTv) AppchiefTvPlayerBinding.inflate(
					LayoutInflater.from(context)
			   ).containerMotionLayout
			   else
					AppchiefFloatingPlayerBinding.inflate(
						 LayoutInflater.from(
							  context
						 )
					).containerMotionLayout)

			   addView(motionLayout)
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
		  if (motionLayout == null)
			   return false
		  val isInProgress = isInProgress()
		  val isInTarget = touchEventInsideTargetViewExceptTop(playerContainer!!, ev)
		  val touchingTarget = ev.touchEventInsideTargetView(playerContainer)
		  return if (isInProgress || isInTarget || touchingTarget) {
			   super.onInterceptTouchEvent(ev)
		  } else {
			   true
		  }
	 }



	 private fun touchEventInsideTargetViewExceptTop(v: View, ev: MotionEvent): Boolean {
		  return !isMinimized() && !isInFullScreen()
	 }

	 override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
		 try {
			 if (playerContainer != null && motionLayout != null) {
				 val consumed = playerContainer!!.dispatchTouchEvent(ev)
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
												  playerContainer!!.performClick()
											 }
											 if (doClickTransition()) {
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
	 return x
}