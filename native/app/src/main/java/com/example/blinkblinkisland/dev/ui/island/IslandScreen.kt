package com.example.blinkblinkisland.dev.ui.island
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.content.attachmentPanelComponent
import com.example.blinkblinkisland.dev.data.audio.AudioGuide
import com.example.blinkblinkisland.dev.data.tracking.*
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.platform.AppRoute
import com.example.blinkblinkisland.dev.ui.island.components.*
import com.example.blinkblinkisland.dev.ui.navigation.Page
import kotlinx.coroutines.delay

private fun Context.activity():Activity?=when(this){is Activity->this;is ContextWrapper->baseContext.activity();else->null}
@Composable
fun IslandScreen(){
 val context=LocalContext.current
 val vm:IslandViewModel=viewModel(factory=remember{IslandViewModelFactory(context)})
 val s by vm.state.collectAsStateWithLifecycle()
 val route by AppRoute.current.collectAsStateWithLifecycle()
 val lifecycle=LocalLifecycleOwner.current.lifecycle
 val activity=context.activity()
 var active by remember{mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
 var permissionEpoch by remember{mutableIntStateOf(0)}
 val audio=remember{AudioGuide(context.applicationContext)}
 val scope=rememberCoroutineScope()
 val tracker=remember{TrackingBridge(scope)}
 DisposableEffect(lifecycle){val listener=LifecycleEventObserver{_,event->active=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED);if(event==Lifecycle.Event.ON_RESUME)permissionEpoch++;if(event==Lifecycle.Event.ON_PAUSE){vm.onEvent(IslandEvent.Pause);audio.stopAll();tracker.stop()}};lifecycle.addObserver(listener);onDispose{lifecycle.removeObserver(listener);audio.close();tracker.stop()}}
 LaunchedEffect(s.settings,active){audio.update(s.settings);if(active&&s.settings.onboarding)audio.ambient()}
 LaunchedEffect(route){route?.let{vm.onEvent(IslandEvent.Route(it));AppRoute.current.value=null}}
 // Bounded developer diagnostics: decision states only, never eye/head poses or raw gaze traces.
 LaunchedEffect(s.page,s.step?.activity,active){
  if(!active||s.page!=Page.TRAINING||s.step?.move==Move.BLINK||context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE==0)return@LaunchedEffect
  repeat(20){val current=vm.state.value;Log.i("BlinkIsland","alignment_gate point=${current.calibrationIndex+1} quality=${current.trackingGood} head=${current.headStable} hit=${current.targetHit} permission=${current.eyePermissionGranted} ${current.sensorStatus}");delay(3000)}
 }
 LaunchedEffect(s.phase,s.calibrationIndex,s.completed,s.index){if(s.page==Page.TRAINING)Log.i("BlinkIsland","training phase=${s.phase} move=${s.step?.move} round=${s.step?.round} alignment=${s.calibrationIndex}/5 completed=${s.completed} skipped=${s.skipped}")}
 LaunchedEffect(s.phase,(s.preparationMs+999)/1000){if(s.page==Page.TRAINING&&s.phase==Phase.PREPARING)Log.i("BlinkIsland","preparation move=${s.step?.move} round=${s.step?.round} seconds=${(s.preparationMs+999)/1000}")}
 LaunchedEffect(s.completed){if(active&&s.page==Page.TRAINING&&s.completed>0)audio.sound("sfx_success")}
 // A deliberate entry into an eye-driven activity requests consent once; denial never grants access.
 LaunchedEffect(s.page,s.step?.activity){
  if(s.page==Page.TRAINING&&s.step?.activity in listOf(com.example.blinkblinkisland.dev.domain.model.Activity.BUTTERFLY,com.example.blinkblinkisland.dev.domain.model.Activity.GAZE)&&context.applicationContext.checkSelfPermission("com.picovr.permission.EYE_TRACKING")!=PackageManager.PERMISSION_GRANTED){
   if(activity!=null)PermissionActions.requestEye(activity)else Log.e("BlinkIsland","training_entry_permission_activity_unavailable")
  }
 }
 LaunchedEffect(s.page,s.step?.move,s.phase,active){if(!active)return@LaunchedEffect
  when{ s.page==Page.SUMMARY->{if(s.completed==s.total&&s.total>0){audio.speak("finish");audio.sound("sfx_session_complete")}else audio.sound("sfx_incomplete")}
   s.phase==Phase.TRACKING_PAUSED->{audio.hold(false);audio.speak("tracking_paused");audio.sound("sfx_tracking_pause")}
   s.phase==Phase.INCOMPLETE->{audio.hold(false);audio.speak("incomplete");audio.sound("sfx_incomplete")}
   s.page==Page.TRAINING&&s.phase==Phase.ACTIVE->{audio.speak(when(s.step?.move){Move.BLINK->"intro_blink";Move.PURSUIT->"butterfly";Move.HORIZONTAL->"gaze_left_right";Move.VERTICAL->"gaze_up_down";Move.CLOCKWISE->"gaze_clockwise";else->"gaze_counter"})}
  }
 }
 LaunchedEffect(active,permissionEpoch){if(!active)return@LaunchedEffect;try{while(true){val granted=context.applicationContext.checkSelfPermission("com.picovr.permission.EYE_TRACKING")==PackageManager.PERMISSION_GRANTED;vm.onEvent(IslandEvent.EyePermission(granted));tracker.start(granted);delay(300)}}finally{tracker.stop()}}
 val panel=attachmentPanelComponent{
  val permissionHost=LocalContext.current.activity()?:activity
  DisposableEffect(permissionHost){Log.i("BlinkIsland","permission_host=${permissionHost?.javaClass?.simpleName?:"unavailable"}");onDispose{}}
  PicoTheme{IslandContent(s,vm::onEvent,permissions={if(permissionHost!=null)PermissionActions.requestEye(permissionHost)else Log.e("BlinkIsland","eye_permission_activity_unavailable")},previewSound={audio.speak("intro_blink")})}
 }
 IslandScene(panel,motionAllowed=active&&s.phase !in listOf(Phase.USER_PAUSED,Phase.TRACKING_PAUSED,Phase.INCOMPLETE,Phase.PREPARING),onScene={ok,error->vm.onEvent(IslandEvent.Scene(ok,error))},frame={root,now,sceneReference->
  val snapshot=if(active)tracker.snapshot(root) else null
  snapshot?.let{(eye,head)->vm.onEvent(IslandEvent.Frame(eye,head,now,tracker.status(),sceneReference))}
  IslandSceneFrame(vm.trainingVisual.takeIf{vm.state.value.page==Page.TRAINING},snapshot?.second)
 })
}
