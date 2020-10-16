package com.appchief.msa.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import com.appchief.msa.exoplayerawesome.R
import com.google.android.gms.common.GooglePlayServicesUtil
import kotlinx.android.synthetic.main.google_services_warning.*


class GoogleServicesWarningActivity : Activity() {
    private val servicesPackageName = "com.google.android.gms"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_services_warning)
        google_play?.isEnabled = isPlayStoreInstalled()
        alternative_store?.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://apkpure.com/google-play-store/com.android.vending/download?from=details")
            )
            startActivity(browserIntent)
        }
        skip?.setOnClickListener {
            this.finish()
        }
        google_play?.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$servicesPackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$servicesPackageName")
                    )
                )
            }
        }
    }

    private fun isPlayStoreInstalled(): Boolean {
        return try {
            packageManager
                .getPackageInfo(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}

