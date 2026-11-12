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

class LaunchActivity : SpatialLaunchActivity(), SensorEventListener {
    private val handler = Handler(Looper.getMainLooper())
    private var sensors: SensorManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordRoute(intent)
        sensors = getSystemService(SensorManager::class.java)
        val sensor = sensors?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        ProbeState.proximity.value = if (sensor == null) "No public Android proximity sensor; wear remains UNVERIFIED" else "Proximity sensor present; wear semantics UNVERIFIED"
        sensor?.let { sensors?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        Log.i(TAG, "lifecycle=create sdk=6.1.9; no training outcome is generated")
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recordRoute(intent)
    }
    private fun recordRoute(intent: Intent?) {
        val route = intent?.getStringExtra("probe_route") ?: return
        if (route !in setOf("start", "later", "today")) return
        ProbeState.route.value = "Notification route received: $route"
        Log.i(TAG, "notification_route=$route")
    }
    override fun onSensorChanged(event: SensorEvent) {
        // A proximity reading must never be treated as an authoritative wear event.
        ProbeState.proximity.value = "Proximity diagnostic=${event.values.firstOrNull()}; wear UNVERIFIED"
        Log.i(TAG, "proximity_event; wear_semantics=unverified")
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun onStop() { Log.i(TAG, "lifecycle=stop"); super.onStop() }
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        sensors?.unregisterListener(this)
        super.onDestroy()
    }
    companion object { const val TAG = "BlinkCapabilityProbe"; private const val CHANNEL = "blink-capability-probe" }
}
