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
import com.appchief.msa.exoplayerawesome.viewcontroller.ControllerVisState
import com.appchief.msa.exoplayerawesome.viewcontroller.VideoControllerView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.cast.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import java.util.*

/** Manages players and an internal media queue for the demo app.  */ /* package */
internal class PlayerManager(
	 private val context: Context,
	 private val listener: Listener,
	 private val localPlayerView: () -> CinamaticExoPlayer,
	 private val castControlView: () -> VideoControllerView?,
	 private val castContext: CastContext?,
	 private val trackSelectors: () -> DefaultTrackSelector
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

	 private lateinit var trackSelector: DefaultTrackSelector
	 lateinit var exoPlayer: SimpleExoPlayer
	 private var castPlayer: CastPlayer? = null
	 private lateinit var mediaQueue: ArrayList<MediaItem>
	 private var concatenatingMediaSource: MediaSource? = null
	 private lateinit var mediaItemConverter: MediaItemConverter
//	 private var lastSeenTrackGroupArray: TrackGroupArray? = null
	 /** Returns the index of the currently played item.  */
	 var currentItemIndex: Int = -1
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
		  //concatenatingMediaSource.clear()
		  //  concatenatingMediaSource.clear()

		  concatenatingMediaSource = item!!
		  val pos = localPlayerView().getLastPos("addItem")
		  if (currentPlayer == castPlayer) {
			   castPlayer?.loadItems(
					castCurrent, 0, pos,
					REPEAT_MODE_ALL
			   )
		  } else {
			   // setCurrentItem(concatenatingMediaSource.size-1,pos,true)
//todo			   this.currentPlayer?.stop()
			   setCurrentPlayer(exoPlayer, pos, true)
		  }
		  Log.e(tag, " addItem")

	 }




	 /** Releases the manager and the players that it holds.  */
	 fun release() {
		  currentItemIndex = C.INDEX_UNSET
		  exoPlayer.stop()
		  mediaQueue.clear()
//		  concatenatingMediaSource.clear()
		  castPlayer?.setSessionAvailabilityListener(null)
		  castPlayer?.release()
		  localPlayerView().player = null
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
		  if (localPlayerView().playerUiFinalListener?.canUseCast() != false)
			   setCurrentPlayer(castPlayer, skip = false)

	 }

	 override fun onCastSessionUnavailable() {
		  setCurrentPlayer(exoPlayer, skip = false)
	 }

	 // Internal methods.
	 private fun updateCurrentItemIndex() {
		  val playbackState = currentPlayer?.playbackState
		  maybeSetCurrentItemAndNotify(
			   if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) currentPlayer?.currentWindowIndex
					?: 0 else C.INDEX_UNSET
		  )
	 }

	 private fun setCurrentPlayer(currentPlayer: Player?, pos: Long? = null, skip: Boolean) {
		  if (currentPlayer == null) {
			   return
		  }
		  // View management.
		  if (currentPlayer == exoPlayer) {
			   castControlView()?.player = exoPlayer
			   localPlayerView().player = exoPlayer
			   castControlView()?.hide()
			   castControlView()?.currentState = ControllerVisState.Normal
		  } else if (castPlayer != null) /* currentPlayer == castPlayer */ {
			   castControlView()?.currentState = ControllerVisState.Cast
			   castControlView()?.player = castPlayer!!
			   localPlayerView().player = castPlayer
			   castControlView()?.show()
			   castPlayer?.loadItems(
					localPlayerView().castCurrent(),
					0,
					pos ?: 0,
					Player.REPEAT_MODE_ALL
			   )
			   localPlayerView().playerUiFinalListener?.forcePortrait()
		  }

		  // Player state management.
		  prepare(currentPlayer, pos)
	 }

	 private fun prepare(currentPlayer: Player, lastpos: Long? = null) {
		  if (concatenatingMediaSource == null)
			   return
		  var playbackPositionMs = lastpos ?: 0L
		  Log.e(
			   tag,
			   "$playbackPositionMs ${currentPlayer.isPlaying} ${this.currentPlayer?.isPlaying}setcurrentplayerFirst"
		  )
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
		  lastpos?.let {
			   playbackPositionMs = it
		  }
		  // Media queue management.
		  if (currentPlayer == exoPlayer) {
			   val haveStartPosition = playbackPositionMs > 0L
			   exoPlayer.seekTo(playbackPositionMs)
			   exoPlayer.prepare(concatenatingMediaSource!!, !haveStartPosition, false)
			   var prevIndex = exoPlayer.previousWindowIndex
			   if (prevIndex < 0)
					prevIndex = 0
			   Log.e(
					tag,
					"seekto $playbackPositionMs setcurrentplayerexoo $haveStartPosition $windowIndex ${prevIndex} cwi ${exoPlayer.currentWindowIndex}"
			   )

		  }
		  // Playback transition.
		  if (windowIndex != C.INDEX_UNSET) {
			   setCurrentItem(windowIndex, playbackPositionMs, playWhenReady)
		  }
		  Log.e(tag, "$playbackPositionMs ${exoPlayer.currentPosition}  setcurrentplayerSecond")
		  this.currentPlayer = currentPlayer
		  exoPlayer.playWhenReady = localPlayerView().isForeground
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
		  if (currentPlayer == castPlayer && castPlayer?.currentTimeline?.isEmpty == true) { //      MediaQueueItem[] items = new MediaQueueItem[mediaQueue.size()];
//      for (int i = 0; i < items.length; i++) {
//        items[i] = mediaItemConverter.toMediaQueueItem(mediaQueue.get(i));
//      }
			   Log.e(tag, "currentItem")
			   castPlayer?.loadItems(
					localPlayerView().castCurrent(),
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
	 var isInCastContext = false
	 val bMeter by lazy { DefaultBandwidthMeter.Builder(context).build() }

	 fun update(freshUpdate: Boolean = false) {
		  if (castContext?.castState == CastState.CONNECTED && freshUpdate)
			   castContext.sessionManager.endCurrentSession(true)
		  if (freshUpdate) {
			   trackSelector = trackSelectors()//DefaultTrackSelector(context!!)
			   val sexo = SimpleExoPlayer.Builder(context).setBandwidthMeter(bMeter)
					.setTrackSelector(trackSelector)
					.build()
			   sexo.setHandleAudioBecomingNoisy(true)

			   localPlayerView().player = sexo
			   exoPlayer =
					localPlayerView().player as SimpleExoPlayer//SimpleExoPlayer.Builder(context).setTrackSelector(trackSelector).build()
			   exoPlayer.addListener(localPlayerView().eventListener)
			   exoPlayer.addListener(this)

			   mediaQueue = ArrayList()

			   currentItemIndex = C.INDEX_UNSET
//		  concatenatingMediaSource = ConcatenatingMediaSource()
			   mediaItemConverter = DefaultMediaItemConverter()
			   if (castContext != null) {
					castPlayer = CastPlayer(castContext)
					castPlayer?.addListener(localPlayerView().eventListener)
					castPlayer?.addListener(this)
					castPlayer?.setSessionAvailabilityListener(this)
			   }
			   castContext?.addCastStateListener {
					if (!isInCastContext)
						 isInCastContext = it == 3 || it == 4
					if (isInCastContext && localPlayerView().isForeground && localPlayerView().playerUiFinalListener?.canUseCast() != false)
						 localPlayerView().playerUiFinalListener?.onMessageRecived(
							  localizeCastState(it),
							  -1
						 )
			   }

			   setCurrentPlayer(
					if (castPlayer?.isCastSessionAvailable == true) castPlayer else exoPlayer,
					skip = false
			   )
		  }
	 }
	 fun localizeCastState(state: Int): String {
		  return when (state) {
			   1 -> localPlayerView().context.getString(R.string.no_cast_device_available)
			   2 -> localPlayerView().context.getString(R.string.cast_not_connected)
			   3 -> localPlayerView().context.getString(R.string.connecting)
			   4 -> localPlayerView().context.getString(R.string.cast_connected)
			   else -> String.format(Locale.ROOT, "UNKNOWN_STATE(%d)", state)
		  }
	 }

	 fun isConnected(): Boolean {
		  return castContext?.castState == CastState.CONNECTED
	 }
}
