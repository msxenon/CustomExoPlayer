package com.appchief.msa

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import com.appchief.msa.exoplayerawesome.DpToPx
import kotlinx.android.synthetic.main.appchief_floating_player.view.*

class MLExts @JvmOverloads constructor(
	 context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

	 fun setTopy(newMarginTop: Int) {
		  detailsView?.updateLayoutParams<ConstraintLayout.LayoutParams> {
			   this.topMargin = newMarginTop.DpToPx()
			   translationY
		  }
	 }
}
