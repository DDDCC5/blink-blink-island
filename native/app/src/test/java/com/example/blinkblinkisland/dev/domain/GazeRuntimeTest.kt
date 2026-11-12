package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*
class GazeRuntimeTest {
 private fun head(t:Long)=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),t)
 private val step=Step(Activity.BUTTERFLY,Move.PURSUIT,1,1)
 @Test fun actualPosesProduceVisibleCalibrationTarget(){val r=GazeRuntime();val f=r.frame(head(0),head(0),0,step,Phase.CAPABILITY_BLOCKED,0);assertNotNull(f.visual);assertFalse(f.calibrated);assertEquals(1.6,f.visual!!.point.y,1e-8)}
 @Test fun staringAtCenterCannotPassAllCalibrationPoints(){val r=GazeRuntime();for(t in 0L..10000 step 50)r.frame(head(t),head(t),t,step,Phase.CAPABILITY_BLOCKED,0);assertFalse(r.calibrated);assertEquals(1,r.calibration.index)}
 @Test fun missingTrackingDoesNotAdmitOrCount(){val r=GazeRuntime();for(t in 0L..10000 step 50){val f=r.frame(null,head(t),t,step,Phase.CAPABILITY_BLOCKED,0);assertFalse(f.observation.quality)};assertFalse(r.calibrated)}
 @Test fun blinkDoesNotInventEyelidData(){val r=GazeRuntime();val f=r.frame(head(0),head(0),0,Step(Activity.BLINK,Move.BLINK,1,10),Phase.CAPABILITY_BLOCKED,0);assertNull(f.observation.eyesClosed);assertNull(f.visual)}
 private fun gazeAt(p:Point3,t:Long):TrackedPose{val dx=p.x;val dy=p.y-1.6;val dz=p.z;val yaw=atan2(-dx,-dz)/2;val pitch=atan2(dy,sqrt(dx*dx+dz*dz))/2;return TrackedPose(head(t).position,Rotation4(cos(yaw)*sin(pitch),sin(yaw)*cos(pitch),-sin(yaw)*sin(pitch),cos(yaw)*cos(pitch)),t)}
 @Test fun fivePointAlignmentThenRealGeometryCanCompletePursuit(){val r=GazeRuntime();val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities());var target=Point3(0.0,1.6,-1.8);for(t in 0L..70000 step 50){val f=r.frame(gazeAt(target,t),head(t),t,s.step,s.phase,s.index,s.targetIndex);f.visual?.let{target=it.point};if(f.calibrated&&!s.capabilities.calibrated)s.updateCapabilities(Capabilities(calibrated=true)) else if(f.calibrated)s.observe(f.observation)};assertEquals(Phase.DONE,s.phase);assertEquals(Outcome.COMPLETED,s.outcome)}
 @Test fun actualPathCannotCompleteWithStationaryCenterGaze(){val r=GazeRuntime();val s=SessionUseCase(listOf(Activity.GAZE),Capabilities());for(t in 0L..70000 step 50){val f=r.frame(head(t),head(t),t,s.step,s.phase,s.index,s.targetIndex);if(f.calibrated)s.updateCapabilities(Capabilities(calibrated=true))};assertFalse(r.calibrated);assertEquals(0,s.completed)}
 @Test fun userPauseFreezesButterflyPath(){val r=GazeRuntime();var target=Point3(0.0,1.6,-1.8);var last:GazeFrame?=null;for(t in 0L..6000 step 50){last=r.frame(gazeAt(target,t),head(t),t,step,if(r.calibrated)Phase.ACTIVE else Phase.CAPABILITY_BLOCKED,0);last.visual?.let{target=it.point}};assertTrue(r.calibrated);val before=last!!.visual!!.point;for(t in 6050L..12000 step 50){val f=r.frame(gazeAt(target,t),head(t),t,step,Phase.USER_PAUSED,0);assertEquals(before,f.visual!!.point)}}
 @Test fun headOnlyRotationCannotQualifyAsEyeMovement(){val r=GazeRuntime();var target=Point3(0.0,1.6,-1.8);for(t in 0L..15000 step 50){val aim=gazeAt(target,t).rotation;val q=Rotation4(aim.x*.75,aim.y*.75,aim.z*.75,aim.w);val pose=head(t).copy(rotation=q);val f=r.frame(pose,pose,t,step,Phase.CAPABILITY_BLOCKED,0);f.visual?.let{target=it.point}};assertFalse(r.calibrated);assertTrue(r.calibration.index<5)}
 private fun aligned():Pair<GazeRuntime,Long>{val r=GazeRuntime();var t=0L;var target=Point3(0.0,1.6,-1.8);while(!r.calibrated&&t<10000){r.frame(gazeAt(target,t),head(t),t,step,Phase.CAPABILITY_BLOCKED,0).visual?.let{target=it.point};t+=50};assertTrue(r.calibrated);return r to t}
 @Test fun headMotionWithinToleranceStillNeedsRelativeEyeMotion(){val (r,start)=aligned();var f:GazeFrame?=null;var t=start;val q=Rotation4(sin(2.9*PI/360),0.0,0.0,cos(2.9*PI/360));repeat(50){val p=head(t).copy(position=Point3(0.0,1.61,0.0),rotation=q);f=r.frame(p,p,t,Step(Activity.GAZE,Move.VERTICAL,1,10),Phase.ACTIVE,0,0);t+=50};val pose=head(t-50).copy(position=Point3(0.0,1.61,0.0),rotation=q);val raw=GazeGeometry.evaluate(pose,pose,head(0),f!!.visual!!.point,t-50,GazeRules(3.0,3.0,.05,200,100));assertTrue(raw.targetHit);assertTrue(f!!.observation.headStable);assertFalse(f!!.observation.targetHit)}
 @Test fun circleRoundBoundaryDoesNotJumpBackToCenter(){val (r,start)=aligned();var t=start;var target=Point3(0.0,1.6,-1.8);var f:GazeFrame?=null;repeat(50){f=r.frame(gazeAt(target,t),head(t),t,Step(Activity.GAZE,Move.CLOCKWISE,1,5),Phase.ACTIVE,20,12);target=f!!.visual!!.point;t+=50};val before=f!!.visual!!.point;val after=r.frame(gazeAt(target,t),head(t),t,Step(Activity.GAZE,Move.CLOCKWISE,2,5),Phase.ACTIVE,21,0).visual!!.point;assertEquals(before.x,after.x,1e-6);assertEquals(before.y,after.y,1e-6);assertEquals(before.z,after.z,1e-6)}
 @Test fun permissionWaitCannotCaptureAnOldHeadReference(){val r=GazeRuntime();val old=head(0).copy(rotation=Rotation4(0.0,sin(PI/8),0.0,cos(PI/8)));r.frame(null,old,0,step,Phase.CAPABILITY_BLOCKED,0);val f=r.frame(head(1000),head(1000),1000,step,Phase.CAPABILITY_BLOCKED,0);assertTrue(f.observation.headStable);assertEquals(0.0,f.visual!!.point.x,1e-6);assertEquals(-1.8,f.visual!!.point.z,1e-6)}
 @Test fun centerCanReanchorAfterLookingUpButLaterTargetsStayLocked(){
  val r=GazeRuntime();r.frame(head(0),head(0),0,step,Phase.CAPABILITY_BLOCKED,0)
  fun raised(t:Long,angle:Double)=head(t).copy(rotation=Rotation4(sin(angle*PI/360),0.0,0.0,cos(angle*PI/360)))
  for(t in 50L..1000 step 50){val p=raised(t,12.0);r.frame(p,p,t,step,Phase.CAPABILITY_BLOCKED,0)}
  assertEquals(1,r.calibration.index)
  var last:GazeFrame?=null
  for(t in 1050L..3000 step 50){val p=raised(t,24.0);last=r.frame(p,p,t,step,Phase.CAPABILITY_BLOCKED,0)}
  assertEquals(1,r.calibration.index);assertFalse(last!!.observation.headStable);assertFalse(r.calibrated)
 }
 @Test fun retryPreparationCentersAndFreezesTargetBeforeMotion(){
  val (r,start)=aligned()
  for(t in start..start+2950 step 50){val f=r.frame(head(t),head(t),t,step,Phase.PREPARING,0);assertTrue(f.visual!!.frozen);assertEquals(0L,f.visual!!.motionMs);assertEquals(0.0,f.visual!!.point.x,1e-8);assertEquals(1.6,f.visual!!.point.y,1e-8)}
  val first=r.frame(head(start+3000),head(start+3000),start+3000,step,Phase.ACTIVE,0)
  assertEquals(0L,first.visual!!.motionMs)
  val next=r.frame(head(start+3050),head(start+3050),start+3050,step,Phase.ACTIVE,0)
  assertEquals(50L,next.visual!!.motionMs)
 }
 @Test fun resumePreparationKeepsTargetAndPathProgress(){
  val (r,start)=aligned();var f:GazeFrame?=null
  for(t in start..start+1000 step 50)f=r.frame(head(t),head(t),t,step,Phase.ACTIVE,0)
  val before=f!!.visual!!
  for(t in start+1050..start+4000 step 50){val waiting=r.frame(head(t),head(t),t,step,Phase.PREPARING,0,restartPreparation=false).visual!!;assertEquals(before.point,waiting.point);assertEquals(before.motionMs,waiting.motionMs);assertTrue(waiting.frozen)}
  val after=r.frame(head(start+4050),head(start+4050),start+4050,step,Phase.ACTIVE,0).visual!!
  assertEquals(before.point,after.point);assertEquals(before.motionMs,after.motionMs)
 }
 @Test fun fixedSceneCenterStaysInFrontWhenEnteringFromSidePanel(){
  val r=GazeRuntime();val anchor=head(0)
  val side=head(100).copy(rotation=Rotation4(0.0,sin(38.4*PI/360),0.0,cos(38.4*PI/360)))
  val first=r.frame(side,side,100,step,Phase.CAPABILITY_BLOCKED,0,sceneReference=anchor)
  assertEquals(0.0,first.visual!!.point.x,1e-8);assertEquals(-1.8,first.visual!!.point.z,1e-8)
  val forward=r.frame(head(200),head(200),200,step,Phase.CAPABILITY_BLOCKED,0,sceneReference=anchor)
  assertEquals(first.visual!!.point,forward.visual!!.point)
 }
}
