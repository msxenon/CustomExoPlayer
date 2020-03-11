/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import com.appchief.msa.exoplayerawesome.viewcontroller.ControllerVisState
import com.appchief.msa.exoplayerawesome.viewcontroller.VideoControllerView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.cast.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import java.util.*

/** Manages players and an internal media queue for the demo app.  */ /* package */
internal class PlayerManager(
	 private val listener: Listener,
	 private val localPlayerView: CinamaticExoPlayer,
	 private val castControlView: VideoControllerView,
	 context: Context?,
	 castContext: CastContext?,
	 trackSelectors: DefaultTrackSelector
) : Player.EventListener, SessionAvailabilityListener {
	 //  public MediaSource mediaSource;
	 /** Listener for events.  */
	 internal interface Listener {

		  /** Called when the currently played item of the media queue changes.  */
		  fun onQueuePositionChanged(previousIndex: Int, newIndex: Int)

		  /**
		   * Called when a track of type `trackType` is not supported by the player.
		   *
		   * @param trackType One of the [C]`.TRACK_TYPE_*` constants.
		   */
		  fun onUnsupportedTrack(trackType: Int)
	 }

	 private val trackSelector: DefaultTrackSelector
	 private val exoPlayer: SimpleExoPlayer
	 private val castPlayer: CastPlayer
	 private val mediaQueue: ArrayList<MediaItem>
	 private val concatenatingMediaSource: ConcatenatingMediaSource
	 private val mediaItemConverter: MediaItemConverter
//	 private var lastSeenTrackGroupArray: TrackGroupArray? = null
	 /** Returns the index of the currently played item.  */
	 var currentItemIndex: Int
		  private set
	 private var currentPlayer: Player? = null
	 // Queue manipulation methods.
	 /**
	  * Plays a specified queue item in the current player.
	  *
	  * @param itemIndex The index of the item to play.
	  */
//	 fun selectQueueItem(itemIndex: Int) {
//		  setCurrentItem(itemIndex, C.TIME_UNSET, true)
//	 }

	 //  public void addItem(MediaItem item) {
//    mediaQueue.add(item);
//   concatenatingMediaSource.addMediaSource(mediaSource);
//    if (currentPlayer == castPlayer) {
//      castPlayer.addItems(mediaItemConverter.toMediaQueueItem(item));
//    }
//  }
	 val tag = "PlayerManager"
	 fun addItem(
		  item: MediaSource?,
		  castCurrent: Array<MediaQueueItem>
	 ) {
//			mediaQueue.clear()
//		  mediaQueue.add(castCurrent)
		  concatenatingMediaSource.clear()
		  concatenatingMediaSource.addMediaSource(item)
		  if (currentPlayer === castPlayer) {
			   val pos = localPlayerView.getLastPos("addItem")
			   Log.e(tag, "$pos addItem")
			   castPlayer.loadItems(
					castCurrent, 0, pos,
					REPEAT_MODE_ALL
			   )
		  }
	 }

	 /** Returns the size of the media queue.  */
	 val mediaQueueSize: Int
		  get() = mediaQueue.size

	 /**
	  * Returns the item at the given index in the media queue.
	  *
	  * @param position The index of the item.
	  * @return The item at the given index in the media queue.
	  */
	 fun getItem(position: Int): MediaItem {
		  return mediaQueue[position]
	 }

	 /**
	  * Removes the item at the given index from the media queue.
	  *
	  * @param item The item to remove.
	  * @return Whether the removal was successful.
	  */
	 fun removeItem(item: MediaItem?): Boolean {
		  val itemIndex = mediaQueue.indexOf(item)
		  if (itemIndex == -1) {
			   return false
		  }
		  concatenatingMediaSource.removeMediaSource(itemIndex)
		  if (currentPlayer === castPlayer) {
			   if (castPlayer.playbackState != Player.STATE_IDLE) {
					val castTimeline = castPlayer.currentTimeline
					if (castTimeline.periodCount <= itemIndex) {
						 return false
					}
					castPlayer.removeItem(
						 castTimeline.getPeriod(
							  itemIndex,
							  Timeline.Period()
						 ).id as Int
					)
			   }
		  }
		  mediaQueue.removeAt(itemIndex)
		  if (itemIndex == currentItemIndex && itemIndex == mediaQueue.size) {
			   maybeSetCurrentItemAndNotify(C.INDEX_UNSET)
		  } else if (itemIndex < currentItemIndex) {
			   maybeSetCurrentItemAndNotify(currentItemIndex - 1)
		  }
		  return true
	 }

	 /**
	  * Dispatches a given [KeyEvent] to the corresponding view of the current player.
	  *
	  * @param event The [KeyEvent].
	  * @return Whether the event was handled by the target view.
	  */
	 fun dispatchKeyEvent(event: KeyEvent?): Boolean {
		  return if (currentPlayer === exoPlayer) {
			   localPlayerView.dispatchKeyEvent(event!!)
		  } else  /* currentPlayer == castPlayer */ {
			   castControlView.dispatchKeyEvent(event!!)
		  }
	 }

	 /** Releases the manager and the players that it holds.  */
	 fun release() {
		  currentItemIndex = C.INDEX_UNSET
		  mediaQueue.clear()
		  concatenatingMediaSource.clear()
		  castPlayer.setSessionAvailabilityListener(null)
		  castPlayer.release()
		  localPlayerView.player = null
		  exoPlayer.release()
	 }

	 // Player.EventListener implementation.
	 override fun onPlayerStateChanged(playWhenReady: Boolean, @Player.State playbackState: Int) {
		  updateCurrentItemIndex()
	 }

	 override fun onPositionDiscontinuity(@DiscontinuityReason reason: Int) {
		  updateCurrentItemIndex()
	 }

	 override fun onTimelineChanged(timeline: Timeline, @TimelineChangeReason reason: Int) {
		  updateCurrentItemIndex()
	 }

	 override fun onTracksChanged(
		  trackGroups: TrackGroupArray,
		  trackSelections: TrackSelectionArray
	 ) {
//		  if (currentPlayer === exoPlayer && trackGroups !== lastSeenTrackGroupArray) {
//			   val mappedTrackInfo = trackSelector.currentMappedTrackInfo
//			   if (mappedTrackInfo != null) {
//					if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
//						 == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS
//					) {
//						 listener.onUnsupportedTrack(C.TRACK_TYPE_VIDEO)
//					}
//					if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
//						 == MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS
//					) {
//						 listener.onUnsupportedTrack(C.TRACK_TYPE_AUDIO)
//					}
//			   }
//			   lastSeenTrackGroupArray = trackGroups
//		  }
	 }

	 // CastPlayer.SessionAvailabilityListener implementation.
	 override fun onCastSessionAvailable() {
		  setCurrentPlayer(castPlayer)
	 }

	 override fun onCastSessionUnavailable() {
		  setCurrentPlayer(exoPlayer)
	 }

	 // Internal methods.
	 private fun updateCurrentItemIndex() {
		  val playbackState = currentPlayer!!.playbackState
		  maybeSetCurrentItemAndNotify(
			   if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) currentPlayer!!.currentWindowIndex else C.INDEX_UNSET
		  )
	 }

	 private fun setCurrentPlayer(currentPlayer: Player) {
		  if (this.currentPlayer === currentPlayer) {
			   return
		  }
		  // View management.
		  if (currentPlayer === exoPlayer) {
			   castControlView.player = exoPlayer
			   localPlayerView.player = exoPlayer
			   castControlView.hide()
			   castControlView.currentState = ControllerVisState.Normal
			   //  localPlayerView.rootView.findViewById<View>(R.id.overlayImage).visibility = View.GONE
		  } else  /* currentPlayer == castPlayer */ { //localPlayerView.setVisibility(View.GONE);
			   castControlView.currentState = ControllerVisState.Cast
			   castControlView.player = castPlayer
			   localPlayerView.player = castPlayer
			   castControlView.show()
//			   localPlayerView.rootView.findViewById<View>(R.id.overlayImage).visibility =
//					View.VISIBLE
//			   localPlayerView.rootView.findViewById<View>(R.id.overlayImage)
//					.setBackgroundResource(R.drawable.cast_bg)
		  }
		  // Player state management.
		  var playbackPositionMs = localPlayerView.getLastPos("setcurrent")
		  Log.e(tag, "$playbackPositionMs setcurrentplayerFirst")
		  var windowIndex = C.INDEX_UNSET
		  var playWhenReady = false
		  val previousPlayer = this.currentPlayer
		  if (previousPlayer != null) { // Save state from the previous player.
			   val playbackState = previousPlayer.playbackState
			   if (playbackState != Player.STATE_ENDED) {
					playbackPositionMs = previousPlayer.currentPosition
					playWhenReady = previousPlayer.playWhenReady
					windowIndex = previousPlayer.currentWindowIndex

					if (windowIndex != currentItemIndex) {
						 playbackPositionMs = C.TIME_UNSET
						 windowIndex = currentItemIndex
					}
			   }
			   previousPlayer.stop(true)
		  }
		  this.currentPlayer = currentPlayer
		  // Media queue management.
		  if (currentPlayer === exoPlayer) {
			   val haveStartPosition = playbackPositionMs > 0L
			   exoPlayer.prepare(concatenatingMediaSource, false, false)
			   exoPlayer.seekTo(exoPlayer.currentWindowIndex, playbackPositionMs)
			   Log.e(
					tag,
					"seekto $playbackPositionMs setcurrentplayerexoo $haveStartPosition $windowIndex cwi${exoPlayer.currentWindowIndex}"
			   )

		  }
		  // Playback transition.
		  if (windowIndex != C.INDEX_UNSET) {
			   setCurrentItem(windowIndex, playbackPositionMs, playWhenReady)
		  }
		  Log.e(tag, "$playbackPositionMs setcurrentplayerSecond")

	 }

	 /**
	  * Starts playback of the item at the given position.
	  *
	  * @param itemIndex The index of the item to play.
	  * @param positionMs The position at which playback should start.
	  * @param playWhenReady Whether the player should proceed when ready to do so.
	  */
	 private fun setCurrentItem(
		  itemIndex: Int,
		  positionMs: Long,
		  playWhenReady: Boolean
	 ) {
		  maybeSetCurrentItemAndNotify(itemIndex)
		  if (currentPlayer === castPlayer && castPlayer.currentTimeline.isEmpty) { //      MediaQueueItem[] items = new MediaQueueItem[mediaQueue.size()];
//      for (int i = 0; i < items.length; i++) {
//        items[i] = mediaItemConverter.toMediaQueueItem(mediaQueue.get(i));
//      }
			   castPlayer.loadItems(
					localPlayerView.castCurrent(),
					0,
					positionMs,
					Player.REPEAT_MODE_ALL
			   )
		  } else {
			   currentPlayer?.seekTo(itemIndex, positionMs)
			   currentPlayer?.playWhenReady = playWhenReady
			   Log.e(tag, "$itemIndex $positionMs currentItem")
		  }
	 }

	 private fun maybeSetCurrentItemAndNotify(currentItemIndex: Int) {
		  if (this.currentItemIndex != currentItemIndex) {
			   val oldIndex = this.currentItemIndex
			   this.currentItemIndex = currentItemIndex
			   listener.onQueuePositionChanged(oldIndex, currentItemIndex)
		  }
	 }

	 companion object {
		  private const val USER_AGENT = "ExoCastDemoPlayer"
		  /**
		   * Appends `item` to the media queue.
		   *
		   * @param item The [MediaItem] to append.
		   */
		  const val MIME_TYPE_HLS = MimeTypes.APPLICATION_M3U8
	 }

	 /**
	  * Creates a new manager for [SimpleExoPlayer] and [CastPlayer].
	  *
	  * @param listener A [Listener] for queue position changes.
	  * @param localPlayerView The [PlayerView] for local playback.
	  * @param castControlView The [PlayerControlView] to control remote playback.
	  * @param context A [Context].
	  * @param castContext The [CastContext].
	  */
	 init {
		  mediaQueue = ArrayList()
		  currentItemIndex = C.INDEX_UNSET
		  concatenatingMediaSource = ConcatenatingMediaSource()
		  mediaItemConverter = DefaultMediaItemConverter()
		  trackSelector = trackSelectors//DefaultTrackSelector(context!!)
		  exoPlayer =
			   localPlayerView.player as SimpleExoPlayer//SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector).build()
		  exoPlayer.addListener(localPlayerView.eventListener)
		  exoPlayer.addListener(this)
		  castPlayer = CastPlayer(castContext!!)
		  castPlayer.addListener(localPlayerView.eventListener)
		  castPlayer.addListener(this)
		  castPlayer.setSessionAvailabilityListener(this)

		  setCurrentPlayer(if (castPlayer.isCastSessionAvailable) castPlayer else exoPlayer)
	 }
}