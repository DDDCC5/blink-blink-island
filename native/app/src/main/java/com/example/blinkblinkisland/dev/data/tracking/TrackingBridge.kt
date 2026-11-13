package com.example.blinkblinkisland.dev.data.tracking
import android.os.SystemClock
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.core.math.Quat
import com.pico.spatial.tracking.DataProvider
import com.pico.spatial.tracking.eye.EyeTrackingProvider
import com.pico.spatial.tracking.eye.EyeTrackingData
import com.pico.spatial.tracking.hmd.HMDTrackingProvider
import com.pico.spatial.tracking.hmd.HMDTrackingData
import com.example.blinkblinkisland.dev.domain.usecase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class TrackingBridge(private val scope:CoroutineScope){
 private val eye=EyeTrackingProvider();private val head=HMDTrackingProvider()
 private var job:Job?=null
 private var eyeData:EyeTrackingData?=null;private var headData:HMDTrackingData?=null
 private var eyeAt=0L;private var headAt=0L
 private var eyeCount=0L;private var headCount=0L
 private var eyeStarted=false
 private var headStarted=false
 fun start(eyeAllowed:Boolean,headNeeded:Boolean=true){
  if(job==null){job=scope.launch{
   launch{eye.dataFlow.collect{eyeData=it;eyeAt=SystemClock.elapsedRealtime();eyeCount++}}
   launch{head.dataFlow.collect{headData=it;headAt=SystemClock.elapsedRealtime();headCount++}}
  }}
  if(headNeeded&&!headStarted){head.start();headStarted=true}
  if(!headNeeded&&headStarted){head.stop();headStarted=false;headData=null}
  if(eyeAllowed&&!eyeStarted){eye.start();eyeStarted=true}
  if(!eyeAllowed&&eyeStarted){eye.stop();eyeStarted=false;eyeData=null}
 }
 fun snapshot(root:Entity):Pair<TrackedPose?,TrackedPose?> {
  fun pose(p:Vector3,q:Quat,at:Long):TrackedPose{
   // Official conversion: source=null is global space; target=root is scene-local meters.
   val v=root.convertPositionFrom(p,null);val r=root.convertRotationFrom(q,null)
   return TrackedPose(Point3(v.x.toDouble(),v.y.toDouble(),v.z.toDouble()),Rotation4(r.x.toDouble(),r.y.toDouble(),r.z.toDouble(),r.w.toDouble()),at)
  }
  // Each stream is isolated: unavailable head data must not discard a usable eye ray.
  val e=runCatching{eyeData?.takeIf{eye.state==DataProvider.State.STARTED&&eye.supportState==DataProvider.SupportState.SUPPORTED}?.eyePose?.let{pose(it.position,it.rotation,eyeAt)}}.getOrNull()
  val h=runCatching{headData?.takeIf{head.state==DataProvider.State.STARTED&&head.supportState==DataProvider.SupportState.SUPPORTED}?.hmdPose?.let{pose(it.position,it.rotation,headAt)}}.getOrNull()
  return e to h
 }
 fun status()="Eye ${eye.supportState}/${eye.state} #$eyeCount · HMD ${head.supportState}/${head.state} #$headCount"
 fun stop(){job?.cancel();job=null;eye.stop();head.stop();eyeStarted=false;headStarted=false;eyeData=null;headData=null}
}
