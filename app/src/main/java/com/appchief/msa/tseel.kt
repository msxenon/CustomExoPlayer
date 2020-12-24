package com.appchief.msa

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckedTextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.forEachIndexed
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.appchief.msa.awesomeplayer.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector.SelectionOverride
import com.google.android.exoplayer2.trackselection.MappingTrackSelector.MappedTrackInfo
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider
import com.google.android.exoplayer2.ui.TrackSelectionView
import com.google.android.exoplayer2.util.Assertions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*

/**
 * Dialog to select tracks.
 */
class TrackSelectionDialog : DialogFragment() {

	 private val tabFragments: SparseArray<TrackSelectionViewFragment>
	 private val tabTrackTypes: ArrayList<Int>
	 private var titleId: Int = 0
	 private var onClickListener: DialogInterface.OnClickListener? = null
	 private var onDismissListener: DialogInterface.OnDismissListener? = null
	 private var frActivity: FragmentActivity? = null
	 private var fm: FragmentManager? = null

	 init {
		  tabFragments = SparseArray()
		  tabTrackTypes = ArrayList()
		  // Retain instance across activity re-creation to prevent losing access to init data.
		  retainInstance = true
	 }

	 private fun init(
		  titleId: Int,
		  mappedTrackInfo: MappedTrackInfo,
		  initialParameters: DefaultTrackSelector.Parameters?,
		  allowAdaptiveSelections: Boolean,
		  allowMultipleOverrides: Boolean,
		  onClickListener: DialogInterface.OnClickListener,
		  onDismissListener: DialogInterface.OnDismissListener
	 ) {
		  this.titleId = titleId
		  this.onClickListener = onClickListener
		  this.onDismissListener = onDismissListener
		  for (i in 0 until mappedTrackInfo.rendererCount) {
			   if (showTabForRenderer(mappedTrackInfo, i)) {
					val trackType = mappedTrackInfo.getRendererType(/* rendererIndex= */i)
					val trackGroupArray = mappedTrackInfo.getTrackGroups(i)
					val tabFragment = TrackSelectionViewFragment()
					tabFragment.init(
						 mappedTrackInfo,
						 /* rendererIndex= */ i,
						 initialParameters?.getRendererDisabled(/* rendererIndex= */i),
						 initialParameters?.getSelectionOverride(/* rendererIndex= */i,
							  trackGroupArray
						 ),
						 allowAdaptiveSelections,
						 allowMultipleOverrides
					)
					tabFragments.put(i, tabFragment)
					tabTrackTypes.add(trackType)
			   }
		  }
	 }

	 /**
	  * Returns whether a renderer is disabled.
	  *
	  * @param rendererIndex Renderer index.
	  * @return Whether the renderer is disabled.
	  */
	 fun getIsDisabled(rendererIndex: Int): Boolean {
		  val rendererView = tabFragments.get(rendererIndex)
		  return rendererView != null && rendererView.isDisabled
	 }

	 /**
	  * Returns the list of selected track selection overrides for the specified renderer. There will
	  * be at most one override for each track group.
	  *
	  * @param rendererIndex Renderer index.
	  * @return The list of track selection overrides for this renderer.
	  */
	 fun getOverrides(rendererIndex: Int): List<SelectionOverride> {
		  val rendererView = tabFragments.get(rendererIndex)
		  return rendererView?.overrides ?: emptyList()
	 }

	 override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		  // We need to own the view to let tab layout work correctly on all API levels. We can't use
		  // AlertDialog because it owns the view itself, so we use AppCompatDialog instead, themed using
		  // the AlertDialog theme overlay with force-enabled title.
		  val dialog = AppCompatDialog(frActivity)
		  dialog.setTitle(titleId)
		  return dialog
	 }

	 override fun onDismiss(dialog: DialogInterface) {
		  super.onDismiss(dialog)
		  onDismissListener!!.onDismiss(dialog)
	 }

	 private var viewPager: ViewPager2? = null
	 private var tabLayout: TabLayout? = null
	 override fun onViewCreated(view_: View, savedInstanceState: Bundle?) {
		  super.onViewCreated(view_, savedInstanceState)
		  viewPager?.offscreenPageLimit = tabFragments.size()


		  viewPager?.adapter = FragmentAdapter(frActivity!!)
		  if (viewPager != null && tabLayout != null)
			   TabLayoutMediator(tabLayout!!, viewPager!!) { tab, position ->
					tab.text = getTrackTypeString(resources, tabTrackTypes[position])
			   }.attach()

		  viewPager?.invalidate()
	 }

	 override fun onCreateView(
		  inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	 ): View? {
		  val dialogView = inflater.inflate(R.layout.tracksel, container, false)
		  tabLayout =
			   dialogView.findViewById<TabLayout>(R.id.track_selection_dialog_tab_layout)

		  viewPager = dialogView.findViewById<ViewPager2>(R.id.tvb)
		  val cancelButton =
			   dialogView.findViewById<Button>(R.id.track_selection_dialog_cancel_button)
		  val okButton = dialogView.findViewById<Button>(R.id.track_selection_dialog_ok_button)
		  okButton.text = "تطبيق التغييرات"
		  cancelButton.text = "إلغاء"


		  tabLayout!!.visibility = if (tabFragments.size() > 1) View.VISIBLE else View.GONE

		  cancelButton.setOnClickListener { view -> dismiss() }
		  okButton.setOnClickListener { view ->
			   onClickListener!!.onClick(dialog, DialogInterface.BUTTON_POSITIVE)
			   dismiss()
		  }
//		  dialog?.getWindow()?.requestFeature(Window.FEATURE_NO_TITLE);
		  return dialogView
	 }

	 /**
	  * Fragment to show a track seleciton in tab of the track selection dialog.
	  */
	 class TrackSelectionViewFragment : Fragment(), TrackSelectionView.TrackSelectionListener {

		  /* package */ internal var isDisabled: Boolean = false
		  /* package */ internal var overrides: List<SelectionOverride>? = null
		  private var mappedTrackInfo: MappedTrackInfo? = null
		  private var rendererIndex: Int = 0
		  private var allowAdaptiveSelections: Boolean = false
		  private var allowMultipleOverrides: Boolean = false

		  init {
			   // Retain instance across activity re-creation to prevent losing access to init data.
			   retainInstance = true
		  }

		  fun init(
			   mappedTrackInfo: MappedTrackInfo,
			   rendererIndex: Int,
			   initialIsDisabled: Boolean?,
			   initialOverride: SelectionOverride?,
			   allowAdaptiveSelections: Boolean,
			   allowMultipleOverrides: Boolean
		  ) {
			   this.mappedTrackInfo = mappedTrackInfo
			   this.rendererIndex = rendererIndex
			   this.isDisabled = initialIsDisabled ?: true
			   this.overrides = if (initialOverride == null)
					emptyList()
			   else
					listOf(initialOverride)
			   this.allowAdaptiveSelections = allowAdaptiveSelections
			   this.allowMultipleOverrides = allowMultipleOverrides
		  }

		  private var trackSelectionView: TrackSelectionView? = null
		  override fun onViewCreated(view_: View, savedInstanceState: Bundle?) {
			   super.onViewCreated(view_, savedInstanceState)
			   trackSelectionView?.forEachIndexed { index, view ->
					if (view is CheckedTextView) {
						 view.gravity = Gravity.RIGHT
					}
			   }
		  }

		  override fun onCreateView(
			   inflater: LayoutInflater,
			   container: ViewGroup?,
			   savedInstanceState: Bundle?
		  ): View? {
			   val rootView = inflater.inflate(
					R.layout.exo_track_selection_dialog_, container, /* attachToRoot= */ false
			   )

			   trackSelectionView =
					rootView.findViewById<TrackSelectionView>(R.id.exo_track_selection_view)
			   trackSelectionView?.setShowDisableOption(true)
			   trackSelectionView?.setTrackNameProvider(DefaultTrackNameProvider(activity!!.resources))
			   trackSelectionView?.setAllowMultipleOverrides(allowMultipleOverrides)
			   trackSelectionView?.setAllowAdaptiveSelections(allowAdaptiveSelections)
			   if (mappedTrackInfo != null && overrides != null) {
					trackSelectionView?.init(
						 mappedTrackInfo!!,
						 rendererIndex,
						 isDisabled,
						 overrides!!, /* listener= */
						 this
					)
			   }
			   return rootView
		  }

		  override fun onTrackSelectionChanged(
			   isDisabled: Boolean,
			   overrides: List<SelectionOverride>
		  ) {
			   this.isDisabled = isDisabled
			   this.overrides = overrides
		  }
	 }

	 private inner class FragmentAdapter(fragmentManager: FragmentActivity) :
		  FragmentStateAdapter(fragmentManager) {


		  override fun getItemCount(): Int {
			   return tabFragments.size()
		  }

		  override fun createFragment(position: Int): Fragment {
			   return tabFragments.valueAt(position)
		  }
	 }

	 companion object {

		  /**
		   * Returns whether a track selection dialog will have content to display if initialized with the
		   * specified [DefaultTrackSelector] in its current state.
		   */
		  fun willHaveContent(trackSelector: DefaultTrackSelector?): Boolean {
			   val mappedTrackInfo = trackSelector?.currentMappedTrackInfo
			   return mappedTrackInfo != null && willHaveContent(mappedTrackInfo)
		  }

		  /**
		   * Returns whether a track selection dialog will have content to display if initialized with the
		   * specified [MappedTrackInfo].
		   */
		  fun willHaveContent(mappedTrackInfo: MappedTrackInfo): Boolean {
			   for (i in 0 until mappedTrackInfo.rendererCount) {
					if (showTabForRenderer(mappedTrackInfo, i)) {
						 return true
					}
			   }
			   return false
		  }

		  /**
		   * Creates a dialog for a given [DefaultTrackSelector], whose parameters will be
		   * automatically updated when tracks are selected.
		   *
		   * @param trackSelector     The [DefaultTrackSelector].
		   * @param onDismissListener A [DialogInterface.OnDismissListener] to call when the dialog is
		   * dismissed.
		   */
		  fun createForTrackSelector(
			   fm: FragmentManager?,
			   activity: FragmentActivity?,
			   trackSelector: DefaultTrackSelector?,
			   onDismissListener: DialogInterface.OnDismissListener
		  ) {
			   val mappedTrackInfo = Assertions.checkNotNull(trackSelector?.currentMappedTrackInfo)
			   val trackSelectionDialog = TrackSelectionDialog()
			   trackSelectionDialog.frActivity = activity
			   trackSelectionDialog.fm = fm
			   val parameters = trackSelector?.parameters
			   trackSelectionDialog.init(
					/* titleId= */ R.string.app_name,
					mappedTrackInfo,
					/* initialParameters = */ parameters,
					/* allowAdaptiveSelections =*/ true,
					/* allowMultipleOverrides= */ false,
					/* onClickListener= */
					DialogInterface.OnClickListener { _, _ ->
						 val builder = parameters?.buildUpon()
						 for (i in 0 until mappedTrackInfo.rendererCount) {
							  builder
								   ?.clearSelectionOverrides(/* rendererIndex= */i)
								   ?.setRendererDisabled(
										/* rendererIndex= */ i,
										trackSelectionDialog.getIsDisabled(/* rendererIndex= */i)
								   )
							  val overrides =
								   trackSelectionDialog.getOverrides(/* rendererIndex= */i)
							  if (!overrides.isEmpty()) {
								   builder?.setSelectionOverride(
										/* rendererIndex= */ i,
										mappedTrackInfo.getTrackGroups(/* rendererIndex= */i),
										overrides[0]
								   )
							  }
						 }
						 builder?.let {
							  trackSelector.setParameters(it)
						 }
					},
					onDismissListener
			   )
			   fm?.let {
					trackSelectionDialog.show(fm, "settings_dialog")
			   }
		  }

		  /**
		   * Creates a dialog for given [MappedTrackInfo] and [DefaultTrackSelector.Parameters].
		   *
		   * @param titleId                 The resource id of the dialog title.
		   * @param mappedTrackInfo         The [MappedTrackInfo] to display.
		   * @param initialParameters       The [DefaultTrackSelector.Parameters] describing the initial
		   * track selection.
		   * @param allowAdaptiveSelections Whether adaptive selections (consisting of more than one track)
		   * can be made.
		   * @param allowMultipleOverrides  Whether tracks from multiple track groups can be selected.
		   * @param onClickListener         [DialogInterface.OnClickListener] called when tracks are selected.
		   * @param onDismissListener       [DialogInterface.OnDismissListener] called when the dialog is
		   * dismissed.
		   */
		  fun createForMappedTrackInfoAndParameters(
			   titleId: Int,
			   mappedTrackInfo: MappedTrackInfo,
			   initialParameters: DefaultTrackSelector.Parameters,
			   allowAdaptiveSelections: Boolean,
			   allowMultipleOverrides: Boolean,
			   onClickListener: DialogInterface.OnClickListener,
			   onDismissListener: DialogInterface.OnDismissListener
		  ): TrackSelectionDialog {
			   val trackSelectionDialog = TrackSelectionDialog()
			   trackSelectionDialog.init(
					titleId,
					mappedTrackInfo,
					initialParameters,
					allowAdaptiveSelections,
					allowMultipleOverrides,
					onClickListener,
					onDismissListener
			   )
			   return trackSelectionDialog
		  }

		  private fun showTabForRenderer(
			   mappedTrackInfo: MappedTrackInfo,
			   rendererIndex: Int
		  ): Boolean {
			   val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
			   if (trackGroupArray.length == 0) {
					return false
			   }
			   val trackType = mappedTrackInfo.getRendererType(rendererIndex)
			   return isSupportedTrackType(trackType)
		  }

		  private fun isSupportedTrackType(trackType: Int): Boolean {
			   when (trackType) {
					C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_AUDIO, C.TRACK_TYPE_TEXT -> return true
					else -> return false
			   }
		  }

		  private fun getTrackTypeString(resources: Resources, trackType: Int): String {
			   when (trackType) {
					C.TRACK_TYPE_VIDEO -> return "الفديو"
					C.TRACK_TYPE_AUDIO -> return "الصوت"
					C.TRACK_TYPE_TEXT -> return "النص"
					else -> throw IllegalArgumentException()
			   }
		  }
	 }
}
