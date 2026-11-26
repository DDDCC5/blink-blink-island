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
import com.example.blinkisland.capabilityprobe.EyeSignalWindow
import android.os.SystemClock
import com.pico.spatial.tracking.eye.EyePose
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
    val context=LocalContext.current
    val activity = context.activity()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    var requested by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Tracking stopped / 尚未启动追踪") }
    var presses by remember { mutableIntStateOf(0) }
    val auditRequested by ProbeState.auditRequested.collectAsState()
    val condition by ProbeState.experiment.collectAsState()
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
    LaunchedEffect(requested, auditRequested, active) {
        if ((!requested && !auditRequested) || !active) return@LaunchedEffect
        if (context.checkSelfPermission("com.picovr.permission.EYE_TRACKING") != PackageManager.PERMISSION_GRANTED) {
            status = "Eye permission absent; tracking not started / 请先授权眼动"
            return@LaunchedEffect
        }
        try {
            val starts = listOf(eye.start(), head.start(), hand.start(), controller.start())
            Log.i(LaunchActivity.TAG, "provider_start_results=$starts")
            var eyeTicks = 0; var headTicks = 0; var handTicks = 0; var controllerTicks = 0
            // Count real Flow emissions and the public EyeTrackingData.timestamp; no raw poses persisted.
            val window=EyeSignalWindow()
            var previousPose:EyePose?=null
            launch { eye.dataFlow.collect {
                eyeTicks++
                val p=it.eyePose
                val finite=listOf(p.position.x,p.position.y,p.position.z,p.rotation.x,p.rotation.y,p.rotation.z,p.rotation.w).all{v->v.isFinite()}
                window.sample(it.timestamp,SystemClock.elapsedRealtime(),finite,previousPose==p)
                previousPose=p
            } }
            launch { head.dataFlow.collect { headTicks++ } }
            launch { hand.dataFlow.collect { handTicks++ } }
            launch { controller.dataFlow.collect { controllerTicks++ } }
            while (true) {
                status = "Eye: ${eye.supportState}/${eye.state} ticks=$eyeTicks\nHead: ${head.supportState}/${head.state} ticks=$headTicks\nHand: ${hand.supportState}/${hand.state} ticks=$handTicks\nController: ${controller.supportState}/${controller.state} ticks=$controllerTicks"
                Log.i(LaunchActivity.TAG,"eye_signal condition=${ProbeState.experiment.value} eye_support=${eye.supportState} eye_state=${eye.state} head_support=${head.supportState} head_state=${head.state} "+window.snapshot(SystemClock.elapsedRealtime()))
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
                Text("Eyelid openness: UNVERIFIED. No inferred closure or success. Condition cue: $condition")
                Text(proximity)
                Text(route)
                Button(onClick = { Log.i(LaunchActivity.TAG,"ui_action=request_permissions");ProbeState.requestEyePermission?.invoke() }) { Text("1. 请求视线权限 / Request eye permission") }
                Button(onClick = { requested = !requested }) { Text(if (requested) "Stop tracking / 停止追踪" else "2. Start tracking / 开始追踪") }
                Button(onClick = {
                    Log.i(LaunchActivity.TAG,"ui_action=eye_signal_experiment")
                    presses++
                    if(context.checkSelfPermission("com.picovr.permission.EYE_TRACKING")==PackageManager.PERMISSION_GRANTED){
                        ProbeState.startEyeExperiment?.invoke()
                    }else status="请先授权眼动，再开始实验 / Grant eye permission first"
                }) { Text("闭眼信号对照实验 / Eye signal experiment") }
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
