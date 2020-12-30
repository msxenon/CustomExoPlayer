package com.appchief.msa

import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.appchief.msa.exoplayerawesome.ExoFactorySingeleton
import com.appchief.msa.exoplayerawesome.NowPlaying
import com.appchief.msa.exoplayerawesome.viewcontroller.VideoControllerView
import com.appchief.msa.floating_player.FloatingPLayerFragment
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.material.snackbar.Snackbar


class PLayerFragment(val externalVideoData: NowPlaying) : FloatingPLayerFragment() {
    private var snackBar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //set data CinematicOnce
        getPlayer()?.cinematicPlayerViews = CinematicOnce()

        //viewcontroller can be customized here
        getPlayer()?.customController =
            object : VideoControllerView(context!!) {
//                override fun onItemEndReached() {
//                    super.onItemEndReached()
//                }
//
//                override fun onFinishInflate() {
//                    super.onFinishInflate()
//                }
//
//                override fun controlVis() {
//                    super.controlVis()
//                }
//
//                override fun initControllerView(v: View) {
//                    super.initControllerView(v)
//                }
//
//                override fun externalSettingsCondition(): Boolean {
//                    return super.externalSettingsCondition()
//                }
//
//                override fun updateDownBtn() {
//                    super.updateDownBtn()
//                }
//
//                override fun dispatchKeyEvent(event: KeyEvent): Boolean {
//                    return super.dispatchKeyEvent(event)
//                }
            }
        super.onViewCreated(view, savedInstanceState)
        initPlayer()
        setDetails()
        //subtitle styling
        getPlayer()?.subtitleView?.setStyle(
            CaptionStyleCompat(
                Color.WHITE,
                Color.BLACK,
                Color.TRANSPARENT,
                CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                Color.TRANSPARENT,
                null
            )
        )
        //subtitle typeFace
//        getPlayer()?.getSubtitleView()?.setFixedTextSize(TypedValue.COMPLEX_UNIT_PX, 80f)
    }

    //Fragment Shows below player
    private fun setDetails() {
        childFragmentManager.beginTransaction()
            .replace(com.appchief.msa.exoplayerawesome.R.id.detailsView, DetailsFrag())
            .commitAllowingStateLoss()
    }

    override fun onMessageRecived(msg: String?, state: Int) {
        msg?.takeIf { view != null && state >= 0 }?.let {
            snackBar = Snackbar.make(view!!, msg, Snackbar.LENGTH_INDEFINITE)
            snackBar?.setAction("Try Again") {
                snackBar?.dismiss()
                initPlayer()
            }
            snackBar?.show()
        } ?: kotlin.run {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun initPlayer(res: String?) {
        getPlayer()?.playLinkNSub(
            externalVideoData.videoLink,
            null,
            null,
            externalVideoData.type,
            TestVars.srt,
            externalVideoData.poster,
            externalVideoData.geners,
            externalVideoData.title,
            externalVideoData.runtime,
            externalVideoData.vttLink
        )
        //add this line if you want to support double taps to forward / rewind
        getPlayer()?.setDoubleTapActivated()
    }

    override fun onDestroy() {
        snackBar?.dismiss()
        super.onDestroy()
    }

    //Use your local db to return last position for this NowPLaying item
    override fun getLastPosition(modelId: NowPlaying?): Long {
        return 0
    }

    //Use your local db to save last position for this NowPlaying item
    override fun savePlayPosition(nowWasPlaying: NowPlaying?, position: Long, duration: Long) {
    }

    //controlls if player can be floated / minimized
    override fun canMinimize(): Boolean {
        return !ExoFactorySingeleton.isTv
    }

    override fun hasPrevItem(): Boolean {
        return false
    }

    override fun hasNextItem(): Boolean {
        return true
    }

    //play next item binding.videoOverlayView.playerContainer?.playLinkNSub(....
    override fun playNext() {
    }

    //play Prev. item binding.videoOverlayView.playerContainer?.playLinkNSub(....
    override fun playPrev() {
    }

    //when Settings icon clicked inside the player
    override fun showSettings(forCasting: Boolean) {
        if (!forCasting) {
            TrackSelectionDialog.createForTrackSelector(childFragmentManager,
                activity,
                getPlayer()!!.trackSelector,
                DialogInterface.OnDismissListener { })
        } else {
            activity?.startActivity(Intent(activity, ExpandedControlsActivity::class.java))
        }
    }

    //sets poster blurred on player bg if its in Casting mode (ChromeCast)
    override fun setMoviePoster(result: (it: Drawable?) -> Unit) {
        val myIcon =
            getDrawable(context!!.resources, com.appchief.msa.awesomeplayer.R.drawable.castbg, null)
        result(myIcon)
    }

    fun getDrawable(res: Resources, id: Int, theme: Theme?): Drawable? {
        val version = Build.VERSION.SDK_INT
        return if (version >= 21) {
            ResourcesCompat.getDrawable(res, id, theme)
        } else {
            res.getDrawable(id)
        }
    }

    override fun addtionalControllerButtonsInit(view: View?) {
    }
}
