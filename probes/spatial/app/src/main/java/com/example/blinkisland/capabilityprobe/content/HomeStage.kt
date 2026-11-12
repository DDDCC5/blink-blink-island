package com.example.blinkisland.capabilityprobe.content

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.blinkisland.capabilityprobe.ProbeState
import com.example.blinkisland.capabilityprobe.platform.LaunchActivity
import com.example.blinkisland.capabilityprobe.platform.ProbeActions
import com.pico.spatial.core.ecs.AttachmentPanelComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.tracking.eye.EyeTrackingProvider
import com.pico.spatial.tracking.hmd.HMDTrackingProvider
import com.pico.spatial.tracking.hand.HandTrackingProvider
import com.pico.spatial.tracking.controller.ControllerTrackingProvider
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.pico.spatial.ui.foundation.content.SpatialView
import com.pico.spatial.ui.foundation.content.attachmentPanelComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

private fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

@Composable
fun HomeStage() {
    val activity = LocalContext.current.activity()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var requested by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Tracking stopped / 尚未启动追踪") }
    var presses by remember { mutableIntStateOf(0) }
    val route by ProbeState.route.collectAsState()
    val proximity by ProbeState.proximity.collectAsState()
    val eye = remember { EyeTrackingProvider() }
    val head = remember { HMDTrackingProvider() }
    val hand = remember { HandTrackingProvider() }
    val controller = remember { ControllerTrackingProvider() }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> active = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(requested, active) {
        if (!requested || !active) return@LaunchedEffect
        if (activity?.checkSelfPermission("com.picovr.permission.EYE_TRACKING") != PackageManager.PERMISSION_GRANTED) {
            status = "Eye permission absent; tracking not started / 请先授权眼动"
            return@LaunchedEffect
        }
        try {
            val starts = listOf(eye.start(), head.start(), hand.start(), controller.start())
            Log.i(LaunchActivity.TAG, "provider_start_results=$starts")
            var eyeTicks = 0; var headTicks = 0; var handTicks = 0; var controllerTicks = 0
            // Count real Flow emissions; never access internal timestamp properties.
            launch { eye.dataFlow.collect { eyeTicks++ } }
            launch { head.dataFlow.collect { headTicks++ } }
            launch { hand.dataFlow.collect { handTicks++ } }
            launch { controller.dataFlow.collect { controllerTicks++ } }
            while (true) {
                status = "Eye: ${eye.supportState}/${eye.state} ticks=$eyeTicks\nHead: ${head.supportState}/${head.state} ticks=$headTicks\nHand: ${hand.supportState}/${hand.state} ticks=$handTicks\nController: ${controller.supportState}/${controller.state} ticks=$controllerTicks"
                Log.i(LaunchActivity.TAG, status.replace('\n', ';'))
                delay(1000)
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            status = "Probe error: ${e.javaClass.simpleName}; see local logcat"
            Log.e(LaunchActivity.TAG, "provider_probe_failed", e)
        } finally { eye.stop(); head.stop(); hand.stop(); controller.stop() }
    }
    val panel = attachmentPanelComponent {
        PicoTheme {
            Column(Modifier.width(1000.dp).padding(28.dp)) {
                Text("眨眨岛 · 能力探针 / Blink capability probe")
                Text("SDK 6.1.9 · Full Space · Diagnostics only; NOT training")
                Text(status)
                Text("Eyelid openness and wear-time API: UNVERIFIED. No inferred closure or success.")
                Text(proximity)
                Text(route)
                Button(onClick = { activity?.let { ProbeActions.requestProbePermissions(it) } }) { Text("1. Request eye + notice permissions / 请求权限") }
                Button(onClick = { requested = !requested }) { Text(if (requested) "Stop tracking / 停止追踪" else "2. Start tracking / 开始追踪") }
                Button(onClick = { presses++; Log.i(LaunchActivity.TAG, "selection=$presses; source_requires_observer_label") }) { Text("Selection test / 选择测试: $presses") }
                Button(onClick = { activity?.let { ProbeActions.scheduleNotification(it) } }) { Text("3. Test notice after 15 s / 15 秒后测试通知") }
                Button(onClick = { activity?.let { ProbeActions.scheduleNotification(it, highImportance = true) } }) { Text("4. HIGH notice after 15 s / 高重要性通知测试") }
                Text("Repeat selection with hand, controller and system dwell if available. Observer labels each mode.")
                Text("Profile / 头像昵称: UNVERIFIED. No account API is invented or called.")
            }
        }
    }
    val panelEntity = remember { Entity().apply { components[AttachmentPanelComponent::class.java] = panel } }
    SpatialView(initial = { content, _ ->
        panelEntity.components[TransformComponent::class.java]?.setPosition(Vector3(0f, 1.3f, -1.5f))
        content.addEntity(panelEntity)
    })
    DisposableEffect(panelEntity) { onDispose { panelEntity.destroy() } }
}
