package com.tvipper.gmcmonitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

class RadiationCheckWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("RadiationWorker", "Running doWork()")

        return try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://gmc.tvipper.com/api/latest.php")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody.isNullOrBlank()) {
                Log.e("RadiationWorker", "Network call failed or empty response")
                return Result.failure()
            }

            val json = JSONObject(responseBody)
            val data = json.getJSONObject("data")
            val usv = data.getDouble("uSV")
            val timestamp = json.getString("timestamp")

            Log.d("RadiationWorker", "Radiation level: $usv µSv/h at $timestamp")

            if (usv >= 2.0) {
                sendNotification(usv, timestamp)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("RadiationWorker", "Exception occurred", e)
            Result.retry()
        }
    }

    private fun sendNotification(usv: Double, timestamp: String) {
        val channelId = "radiation_alerts"
        val title = "High Radiation Detected!"
        val message = String.format(Locale.US, "%.2f µSv/h at %s", usv, timestamp)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Radiation Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when high radiation is detected"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        if (ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("RadiationWorker", "Posting notification: $message")
            notificationManager.notify(1001, builder.build())
        } else {
            Log.w("RadiationWorker", "Notification permission not granted")
        }
    }
}
