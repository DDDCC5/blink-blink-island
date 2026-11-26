package com.example.blinkisland.capabilityprobe

import kotlinx.coroutines.flow.MutableStateFlow

/** Diagnostic counters only. No raw gaze, pose, account data or completed training is persisted. */
object ProbeState {
    var requestEyePermission:(()->Unit)?=null
    var startEyeExperiment:(()->Boolean)?=null
    val experiment = MutableStateFlow("idle")
    val auditRequested = MutableStateFlow(false)
    val route = MutableStateFlow("No notification received / 尚未点击通知")
    val proximity = MutableStateFlow("Proximity untested; proximity is NOT verified wear state")
}
