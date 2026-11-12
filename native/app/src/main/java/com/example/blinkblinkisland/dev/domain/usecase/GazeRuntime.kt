package com.example.blinkblinkisland.dev.domain.usecase
import com.example.blinkblinkisland.dev.domain.model.*

data class TrainingVisual(val point:Point3,val reference:TrackedPose,val butterfly:Boolean,val motionMs:Long,val frozen:Boolean)
data class GazeFrame(val observation:Observation,val visual:TrainingVisual?,val calibrated:Boolean)
/** Actual pose-to-target pipeline. These development tolerances still need Swan acceptance. */
class GazeRuntime {
 val calibration=CalibrationUseCase()
 val calibrated get()=calibration.complete
 private var reference:TrackedPose?=null
 private var headReference:TrackedPose?=null
 private var centerEye:TrackedPose?=null
 private var centerHead:TrackedPose?=null
 private var transitionStart:Point3?=null
 private var previousAt:Long?=null
 private var previousPhase:Phase?=null
 private var key=""
 private var pathMs=0L
 private val rules=GazeRules(targetDegrees=3.0,headDegrees=3.0,headMeters=.05,maxAgeMs=200,maxSkewMs=100)
 private var visual:TrainingVisual?=null
 fun frame(eye:TrackedPose?,head:TrackedPose?,now:Long,step:Step?,phase:Phase?,roundIndex:Int,targetIndex:Int=0,restartPreparation:Boolean=true,sceneReference:TrackedPose?=null):GazeFrame {
  val invalid=Observation(now,false,false,issue=GazeIssue.NO_DATA)
  if(step==null||step.move==Move.BLINK)return GazeFrame(invalid,null,false)
  val dt=previousAt?.let{(now-it).takeIf{d->d in 0..250}?:0}?:0;previousAt=now
  if(eye!=null&&head!=null&&reference==null&&now-head.atMs in 0..200){
   // Permission dialogs may move the head: never anchor to a pose sampled before eye data exists.
   val candidate=GazeGeometry.evaluate(eye,head,head,TargetPaths.fromHead(TargetPaths.center,head),now,rules)
   if(candidate.quality){reference=sceneReference?:head;headReference=head}
  }
  var origin=reference?:return GazeFrame(invalid,null,false)
  fun evaluate(point:Point3):Observation {
   val e=eye?:return invalid;val h=head?:return invalid
   val o=GazeGeometry.evaluate(e,h,headReference?:origin,point,now,rules)
   val ce=centerEye;val ch=centerHead;val offset=GazeGeometry.eccentricity(point,origin)
   val eyeParticipates=offset<=3||ce==null||ch==null||GazeGeometry.relativeEyeMotion(e,h,ce,ch)>=offset*.5
   return o.copy(targetHit=o.targetHit&&eyeParticipates,issue=if(o.targetHit&&!eyeParticipates)GazeIssue.EYE_PARTICIPATION else o.issue)
  }
  if(!calibrated){
   var point=TargetPaths.fromHead(TargetPaths.calibration(calibration.index),origin)
   var o=evaluate(point)
   if(calibration.index==0&&o.quality&&!o.headStable&&head!=null&&phase!=Phase.USER_PAUSED){
    // The user may look up from a menu. Acquire a comfortable center before locking later targets.
    headReference=head
    if(sceneReference==null){reference=head;origin=head}
    point=TargetPaths.fromHead(TargetPaths.center,origin)
    o=evaluate(point).copy(headStable=false,targetHit=false)
   }
   val previousIndex=calibration.index
   if(phase!=Phase.USER_PAUSED)calibration.observe(o)
   if(previousIndex==0&&calibration.index==1&&head!=null){centerEye=eye;centerHead=head;headReference=head;if(sceneReference==null){reference=head;origin=head}}
   visual=TrainingVisual(point,origin,false,0,phase==Phase.USER_PAUSED||!o.quality)
   return GazeFrame(o,visual,calibrated)
  }
  if(phase==Phase.PREPARING){
   if(restartPreparation){pathMs=0;transitionStart=TargetPaths.fromHead(TargetPaths.center,origin)}
   previousPhase=phase
   val point=if(restartPreparation)transitionStart!! else visual?.point?:TargetPaths.fromHead(TargetPaths.center,origin)
   visual=TrainingVisual(point,origin,step.move==Move.PURSUIT,pathMs,true)
   return GazeFrame(evaluate(point),visual,true)
  }
  val newKey="$roundIndex:${step.move}:$targetIndex"
  if(newKey!=key||(previousPhase==Phase.INCOMPLETE&&phase==Phase.ACTIVE)){key=newKey;pathMs=0;transitionStart=visual?.point}
  val justPrepared=previousPhase==Phase.PREPARING
  previousPhase=phase
  val currentPoint=visual?.point?:TargetPaths.fromHead(TargetPaths.center,origin)
  val check=if(eye!=null&&head!=null)GazeGeometry.evaluate(eye,head,headReference?:origin,currentPoint,now,rules) else invalid
  if(phase==Phase.ACTIVE&&check.quality&&check.headStable&&!justPrepared)pathMs+=dt
  val point=if(step.move==Move.PURSUIT)TargetPaths.fromHead(TargetPaths.pursuit(pathMs),origin) else {
   val end=TargetPaths.fromHead(TargetPaths.destination(step.move,targetIndex),origin)
   val start=transitionStart?:end;val f=(pathMs/2000.0).coerceIn(0.0,1.0);val ease=f*f*(3-2*f)
   Point3(start.x+(end.x-start.x)*ease,start.y+(end.y-start.y)*ease,start.z+(end.z-start.z)*ease)
  }
  val observation=evaluate(point).copy(targetReady=step.move==Move.PURSUIT||pathMs>=2000)
  visual=TrainingVisual(point,origin,step.move==Move.PURSUIT,pathMs,phase!=Phase.ACTIVE||!observation.quality||!observation.headStable)
  return GazeFrame(observation,visual,true)
 }
}
