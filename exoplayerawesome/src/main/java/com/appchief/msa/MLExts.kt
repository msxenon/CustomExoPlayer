package com.appchief.msa

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.Toast
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

fun Context.showTopToast(string: String) {
	 val toast = Toast.makeText(
		  this,
		  string, Toast.LENGTH_LONG
	 )
	 toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 20.DpToPx())
	 toast.show()
}