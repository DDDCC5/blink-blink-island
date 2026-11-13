package com.example.blinkblinkisland.dev.domain.usecase
import kotlin.math.*

/** Persistent room placement is independent of the training/calibration reference. */
data class ScenePlacement(val reference:TrackedPose,val panel:Point3,val island:Point3)
class ScenePlacementUseCase {
 private var placement:ScenePlacement?=null
 /** Explicit user action only; normal head movement must never move the scene. */
 fun reset(){placement=null}
 fun resolve(head:TrackedPose?,now:Long):ScenePlacement? {
  placement?.let{return it}
  if(head==null||now-head.atMs !in 0..200)return null
  val center=TargetPaths.fromHead(TargetPaths.center,head)
  if(!GazeGeometry.evaluate(head,head,head,center,now,GazeRules(3.0,3.0,.05,200,100)).quality)return null
  // Keep world +Y upright. Launch pitch/roll must never become a room tilt.
  val forward=TargetPaths.fromHead(Point3(0.0,0.0,-1.0),head)
  val fx=forward.x-head.position.x;val fz=forward.z-head.position.z
  if(hypot(fx,fz)<1e-4)return null
  val yaw=atan2(-fx,-fz)
  val level=head.copy(rotation=Rotation4(0.0,sin(yaw/2),0.0,cos(yaw/2)))
  return ScenePlacement(level,TargetPaths.fromHead(Point3(-.95,0.0,-1.2),level),TargetPaths.fromHead(Point3(1.55,0.0,-2.4),level)).also{placement=it}
 }
}
