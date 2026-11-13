package com.example.blinkblinkisland.dev.domain.usecase
import com.example.blinkblinkisland.dev.domain.model.*

data class TrainingVisual(val point:Point3,val reference:TrackedPose,val butterfly:Boolean,val motionMs:Long,val frozen:Boolean)
data class GazeFrame(val observation:Observation,val visual:TrainingVisual?,val gazeReady:Boolean)
/** Actual pose-to-target pipeline. These development tolerances still need Swan acceptance. */
class GazeRuntime {
 /** Data readiness only; this is not SDK eye-tracking calibration. */
 var gazeReady=false;private set
 private var reference:TrackedPose?=null
 private var transitionStart:Point3?=null
 private var previousAt:Long?=null
 private var previousPhase:Phase?=null
 private var key=""
 private var pathMs=0L
 private var waitingForGaze=false
 private val rules=GazeRules(targetDegrees=3.0,headDegrees=3.0,headMeters=.05,maxAgeMs=200,maxSkewMs=100)
 private var visual:TrainingVisual?=null
 fun frame(eye:TrackedPose?,head:TrackedPose?,now:Long,step:Step?,phase:Phase?,roundIndex:Int,targetIndex:Int=0,restartPreparation:Boolean=true,sceneReference:TrackedPose?=null):GazeFrame {
  val invalid=Observation(now,false,true,issue=GazeIssue.NO_DATA)
  if(step==null||step.move==Move.BLINK){gazeReady=false;return GazeFrame(invalid,null,false)}
  val dt=previousAt?.let{(now-it).takeIf{d->d in 0..250}?:0}?:0;previousAt=now
  // A verified, persistent scene reference is supplied by scene placement, not live HMD tracking.
  if(reference==null)reference=sceneReference
  val origin=reference?:run{gazeReady=false;return GazeFrame(invalid,null,false)}
  fun evaluate(point:Point3)=eye?.let{GazeGeometry.evaluateEye(it,point,now,rules)}?:invalid
  fun result(o:Observation):GazeFrame{gazeReady=o.quality;return GazeFrame(o,visual,gazeReady)}
  if(phase==Phase.CAPABILITY_BLOCKED){
   val point=TargetPaths.fromHead(TargetPaths.center,origin)
   visual=TrainingVisual(point,origin,step.move==Move.PURSUIT,0,true)
   previousPhase=phase
   return result(evaluate(point))
  }
  if(phase==Phase.PREPARING){
   if(restartPreparation){pathMs=0;transitionStart=TargetPaths.fromHead(TargetPaths.center,origin)}
   previousPhase=phase
   val point=if(restartPreparation)transitionStart!! else visual?.point?:TargetPaths.fromHead(TargetPaths.center,origin)
   visual=TrainingVisual(point,origin,step.move==Move.PURSUIT,pathMs,true)
   return result(evaluate(point))
  }
  val newKey="$roundIndex:${step.move}:$targetIndex"
  if(newKey!=key||(previousPhase==Phase.INCOMPLETE&&phase==Phase.ACTIVE)){key=newKey;pathMs=0;transitionStart=visual?.point}
  val justPrepared=previousPhase==Phase.PREPARING
  previousPhase=phase
  val currentPoint=visual?.point?:TargetPaths.fromHead(TargetPaths.center,origin)
  val check=evaluate(currentPoint)
  val gazeMove=step.move!=Move.PURSUIT
  // Small-target paths wait at their current visible point; returning gaze never catches up time.
  if(phase==Phase.ACTIVE&&check.quality&&!justPrepared&&(!gazeMove||(check.targetHit&&!waitingForGaze)))pathMs+=dt
  waitingForGaze=gazeMove&&!check.targetHit
  val duration=TargetPaths.transitionDurationMs(step.move)
  val point=if(step.move==Move.PURSUIT)TargetPaths.fromHead(TargetPaths.pursuit(pathMs),origin) else {
   val end=TargetPaths.fromHead(TargetPaths.destination(step.move,targetIndex),origin)
   val start=transitionStart?:end;val f=(pathMs/duration.toDouble()).coerceIn(0.0,1.0);val ease=f*f*(3-2*f)
   Point3(start.x+(end.x-start.x)*ease,start.y+(end.y-start.y)*ease,start.z+(end.z-start.z)*ease)
  }
  val observation=evaluate(point).copy(targetReady=step.move==Move.PURSUIT||pathMs>=duration)
  visual=TrainingVisual(point,origin,step.move==Move.PURSUIT,pathMs,phase!=Phase.ACTIVE||!observation.quality||(gazeMove&&!observation.targetHit))
  return result(observation)
 }
}
