package com.tvipper.gmcmonitor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Container layout
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // WebView setup
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            // WebView settings
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            settings.displayZoomControls = false

            // Tilføj brugeragent så PHP kan genkende appen
            val defaultUA = settings.userAgentString
            settings.userAgentString = "$defaultUA GMCApp"

            webViewClient = WebViewClient()
            loadUrl("https://gmc.tvipper.com/index.php")
        }

        layout.addView(webView)
        setContentView(layout)

        // Tilladelser og baggrundsarbejde
        requestNotificationPermissionIfNeeded()
        schedulePeriodicWorker()
    }

    override fun onResume() {
        super.onResume()

        // Genindlæs hvis DNS fejlede under opstart
        webView.evaluateJavascript(
            "(function() { return document.body.innerText.includes('ERR_NAME_NOT_RESOLVED'); })();"
        ) { result ->
            if (result == "true") {
                webView.reload()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    Log.d("MainActivity", "Notification permission already granted")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    Toast.makeText(this, "Notification permission is required for alerts.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun schedulePeriodicWorker() {
        val request = PeriodicWorkRequestBuilder<RadiationCheckWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "radiation_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        Log.d("MainActivity", "Scheduled periodic RadiationCheckWorker")
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("MainActivity", "Notification permission granted: $isGranted")
        if (!isGranted) {
            Toast.makeText(this, "Notifications won't be shown without permission.", Toast.LENGTH_SHORT).show()
        }
    }
}
