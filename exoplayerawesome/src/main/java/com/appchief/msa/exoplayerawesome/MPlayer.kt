package com.appchief.msa.exoplayerawesome

import android.content.Context
import android.os.Looper
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsCollector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.util.Clock

class MPlayer(
	 context: Context,
	 renderersFactory: RenderersFactory,
	 trackSelector: TrackSelector,
	 loadControl: LoadControl,
	 bandwidthMeter: BandwidthMeter,
	 analyticsCollector: AnalyticsCollector,
	 clock: Clock,
	 looper: Looper
) : SimpleExoPlayer(
	 context,
	 renderersFactory,
	 trackSelector,
	 loadControl,
	 bandwidthMeter,
	 analyticsCollector,
	 clock,
	 looper
)