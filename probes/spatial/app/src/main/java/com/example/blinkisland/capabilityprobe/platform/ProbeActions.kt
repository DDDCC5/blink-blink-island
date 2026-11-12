package com.example.blinkisland.capabilityprobe.platform

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blinkisland.capabilityprobe.ProbeState
import com.pico.spatial.ui.platform.stub.SpatialLaunchActivity

object ProbeActions {
    private val handler = Handler(Looper.getMainLooper())
    private const val CHANNEL = "blink-capability-probe"
    fun requestProbePermissions(activity: android.app.Activity) {
        val permissions = arrayOf("com.picovr.permission.EYE_TRACKING", Manifest.permission.POST_NOTIFICATIONS)
            .filter { activity.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (permissions.isNotEmpty()) activity.requestPermissions(permissions.toTypedArray(), 41)
    }
    fun scheduleNotification(activity: android.app.Activity, highImportance: Boolean = false) {
        val manager = activity.getSystemService(NotificationManager::class.java)
        if (activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ProbeState.route.value = "Notification permission missing / 请先授权通知"
            return
        }
        val channelId = if (highImportance) "blink-capability-probe-high" else CHANNEL
        val importance = if (highImportance) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
        manager.createNotificationChannel(NotificationChannel(channelId, if (highImportance) "Blink probe HIGH test" else "Blink capability probe", importance))
        // Replace the pending test rather than stacking notices after repeated taps.
        handler.removeCallbacksAndMessages(null)
        fun target(route: String, code: Int): PendingIntent = PendingIntent.getActivity(
            activity, code, Intent(activity, LaunchActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra("probe_route", route), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        handler.postDelayed({
            try {
                manager.notify(if (highImportance) 702 else 701, Notification.Builder(activity, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(if (highImportance) "高重要性测试 / Blink HIGH probe" else "眨眨岛能力验证 / Blink probe")
                    .setContentText("Test only: tap to verify route. No wear reminder has been validated.")
                    .setContentIntent(target("start", 1)).setAutoCancel(true)
                    .addAction(Notification.Action.Builder(null, "Start / 开始", target("start", 1)).build())
                    .addAction(Notification.Action.Builder(null, "Later / 稍后", target("later", 2)).build())
                    .addAction(Notification.Action.Builder(null, "Today / 今日静默", target("today", 3)).build())
                    .build())
                Log.i(LaunchActivity.TAG, "notification_posted channel=$channelId importance=$importance; visible delivery and click need human observation")
            } catch (e: Exception) { Log.e(LaunchActivity.TAG, "notification_post_failed", e) }
        }, 15000)
        ProbeState.route.value = "${if (highImportance) "HIGH" else "DEFAULT"} notice scheduled in 15 s; switch apps. Process-local timer only."
    }
}
