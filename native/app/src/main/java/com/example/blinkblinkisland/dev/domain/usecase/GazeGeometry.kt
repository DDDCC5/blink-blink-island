package com.example.blinkblinkisland.dev.domain.usecase

import com.example.blinkblinkisland.dev.domain.model.Observation
import com.example.blinkblinkisland.dev.domain.model.GazeIssue
import kotlin.math.*

/** App-owned values, in one shared world coordinate frame; times are monotonic receipt times. */
data class Point3(val x:Double,val y:Double,val z:Double)
data class Rotation4(val x:Double,val y:Double,val z:Double,val w:Double)
data class TrackedPose(val position:Point3,val rotation:Rotation4,val atMs:Long)
/** No product defaults: tolerances require calibration/comfort validation before runtime admission. */
data class GazeRules(val targetDegrees:Double,val headDegrees:Double,val headMeters:Double,val maxAgeMs:Long,val maxSkewMs:Long){
 init{require(targetDegrees.isFinite()&&targetDegrees>0&&targetDegrees<90);require(headDegrees.isFinite()&&headDegrees>0&&headDegrees<180);require(headMeters.isFinite()&&headMeters>0);require(maxAgeMs in 1..1000&&maxSkewMs in 0..maxAgeMs)}
}

/** Pure geometric evaluator; this alone does not establish successful device calibration. */
object GazeGeometry {
 /** Eye motion in head-local coordinates; turning both poses together gives zero. */
 fun relativeEyeMotion(eye:TrackedPose,head:TrackedPose,centerEye:TrackedPose,centerHead:TrackedPose):Double {
  fun local(e:TrackedPose,h:TrackedPose):Point3{
   val q=e.rotation.unit()?:return Point3(Double.NaN,0.0,0.0)
   val f=Point3(-2*(q.x*q.z+q.w*q.y),-2*(q.y*q.z-q.w*q.x),-(1-2*(q.x*q.x+q.y*q.y)))
   val r=h.rotation
   return TargetPaths.fromHead(f,TrackedPose(Point3(0.0,0.0,0.0),Rotation4(-r.x,-r.y,-r.z,r.w),0))
  }
  val a=local(eye,head);val b=local(centerEye,centerHead)
  return acos(((a.x*b.x+a.y*b.y+a.z*b.z)/(a.length()*b.length())).coerceIn(-1.0,1.0))*180/PI
 }
 fun eccentricity(target:Point3,head:TrackedPose):Double {
  val forward=TargetPaths.fromHead(TargetPaths.center,head)
  val a=Point3(forward.x-head.position.x,forward.y-head.position.y,forward.z-head.position.z)
  val b=Point3(target.x-head.position.x,target.y-head.position.y,target.z-head.position.z)
  return acos(((a.x*b.x+a.y*b.y+a.z*b.z)/(a.length()*b.length())).coerceIn(-1.0,1.0))*180/PI
 }
 fun evaluate(eye:TrackedPose,head:TrackedPose,baseline:TrackedPose,target:Point3,now:Long,rules:GazeRules):Observation {
  fun invalid()=Observation(now,false,false,issue=GazeIssue.INVALID_SAMPLE)
  if(!listOf(eye.position,head.position,baseline.position,target).all{it.finite()})return invalid()
  if(now<0||listOf(eye.atMs,head.atMs).any{it<0||it>now||now-it>rules.maxAgeMs})return invalid()
  if(abs(eye.atMs-head.atMs)>rules.maxSkewMs)return invalid()
  val q=eye.rotation.unit()?:return invalid()
  val h=head.rotation.unit()?:return invalid()
  val b=baseline.rotation.unit()?:return invalid()
  val direction=Point3(target.x-eye.position.x,target.y-eye.position.y,target.z-eye.position.z)
  val distance=direction.length()
  if(!distance.isFinite()||distance<1e-6)return invalid()
  // Right handed world convention used by the scene: local forward is -Z.
  val forward=Point3(-2*(q.x*q.z+q.w*q.y),-2*(q.y*q.z-q.w*q.x),-(1-2*(q.x*q.x+q.y*q.y)))
  val angularError=acos(((forward.x*direction.x+forward.y*direction.y+forward.z*direction.z)/distance).coerceIn(-1.0,1.0))*180/PI
  val headAngle=2*acos(abs(h.x*b.x+h.y*b.y+h.z*b.z+h.w*b.w).coerceIn(0.0,1.0))*180/PI
  val translation=Point3(head.position.x-baseline.position.x,head.position.y-baseline.position.y,head.position.z-baseline.position.z).length()
  val stable=headAngle<=rules.headDegrees&&translation<=rules.headMeters
  fun band(value:Double,limit:Double)=when{value<=limit->0;value<=limit*2->1;value<=limit*4->2;else->3}
  val issue=when{headAngle>rules.headDegrees->GazeIssue.HEAD_ROTATION;translation>rules.headMeters->GazeIssue.HEAD_POSITION;angularError>rules.targetDegrees->GazeIssue.GAZE_OFF_TARGET;else->GazeIssue.NONE}
  return Observation(now,true,stable,eyesClosed=null,targetHit=stable&&angularError<=rules.targetDegrees,issue=issue,headRotationBand=band(headAngle,rules.headDegrees),headShiftBand=band(translation,rules.headMeters))
 }
 private fun Point3.finite()=x.isFinite()&&y.isFinite()&&z.isFinite()
 private fun Point3.length()=sqrt(x*x+y*y+z*z)
 private fun Rotation4.unit():Rotation4?{val n=sqrt(x*x+y*y+z*z+w*w);return if(!n.isFinite()||n<1e-12)null else Rotation4(x/n,y/n,z/n,w/n)}
}
