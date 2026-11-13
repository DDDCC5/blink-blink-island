package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*
class GazeRuntimeTest {
 private fun head(t:Long)=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),t)
 private val step=Step(Activity.BUTTERFLY,Move.PURSUIT,1,1)
 @Test fun freshEyeProducesReadyVisibleCenterTarget(){val r=GazeRuntime();val f=r.sampleFrame(head(0),head(0),0,step,Phase.CAPABILITY_BLOCKED,0);assertNotNull(f.visual);assertTrue(f.gazeReady);assertEquals(1.6,f.visual!!.point.y,1e-8)}
 @Test fun waitingForAdmissionNeverMovesThroughFivePoints(){val r=GazeRuntime();for(t in 0L..10000 step 50){val f=r.sampleFrame(head(t),null,t,step,Phase.CAPABILITY_BLOCKED,0);assertTrue(f.gazeReady);assertEquals(Point3(0.0,1.6,-1.8),f.visual!!.point);assertEquals(0L,f.visual!!.motionMs)}}
 @Test fun missingTrackingDoesNotAdmitOrCount(){val r=GazeRuntime();for(t in 0L..10000 step 50){val f=r.sampleFrame(null,head(t),t,step,Phase.CAPABILITY_BLOCKED,0);assertFalse(f.observation.quality)};assertFalse(r.gazeReady)}
 @Test fun blinkDoesNotInventEyelidData(){val r=GazeRuntime();val f=r.sampleFrame(head(0),head(0),0,Step(Activity.BLINK,Move.BLINK,1,10),Phase.CAPABILITY_BLOCKED,0);assertNull(f.observation.eyesClosed);assertNull(f.visual)}
 private fun gazeAt(p:Point3,t:Long):TrackedPose{val dx=p.x;val dy=p.y-1.6;val dz=p.z;val yaw=atan2(-dx,-dz)/2;val pitch=atan2(dy,sqrt(dx*dx+dz*dz))/2;return TrackedPose(head(t).position,Rotation4(cos(yaw)*sin(pitch),sin(yaw)*cos(pitch),-sin(yaw)*sin(pitch),cos(yaw)*cos(pitch)),t)}
 @Test fun freshDataThenRealGeometryCanCompletePursuit(){val r=GazeRuntime();val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities());var target=Point3(0.0,1.6,-1.8);for(t in 0L..70000 step 50){val f=r.sampleFrame(gazeAt(target,t),head(t),t,s.step,s.phase,s.index,s.targetIndex);f.visual?.let{target=it.point};if(f.gazeReady&&!s.capabilities.gazeReady)s.updateCapabilities(Capabilities(gazeReady=true)) else if(s.capabilities.gazeReady)s.observe(f.observation)};assertEquals(Phase.DONE,s.phase);assertEquals(Outcome.COMPLETED,s.outcome)}
 @Test fun actualPathCannotCompleteWithStationaryCenterGaze(){val r=GazeRuntime();val s=SessionUseCase(listOf(Activity.GAZE),Capabilities());for(t in 0L..70000 step 50){val f=r.sampleFrame(head(t),null,t,s.step,s.phase,s.index,s.targetIndex);if(f.gazeReady&&!s.capabilities.gazeReady)s.updateCapabilities(Capabilities(gazeReady=true))else if(s.capabilities.gazeReady)s.observe(f.observation)};assertTrue(r.gazeReady);assertEquals(Phase.ACTIVE,s.phase);assertEquals(0,s.incomplete);assertEquals(0,s.targetIndex);assertEquals(0,s.completed)}
 @Test fun userPauseFreezesButterflyPath(){val r=GazeRuntime();var target=Point3(0.0,1.6,-1.8);var last:GazeFrame?=null;for(t in 0L..6000 step 50){last=r.sampleFrame(gazeAt(target,t),head(t),t,step,if(r.gazeReady)Phase.ACTIVE else Phase.CAPABILITY_BLOCKED,0);last.visual?.let{target=it.point}};assertTrue(r.gazeReady);val before=last!!.visual!!.point;for(t in 6050L..12000 step 50){val f=r.sampleFrame(gazeAt(target,t),head(t),t,step,Phase.USER_PAUSED,0);assertEquals(before,f.visual!!.point)}}
 @Test fun headMovementDoesNotPreventDataReadiness(){val r=GazeRuntime();for(t in 0L..1000 step 50){val pose=head(t).copy(position=Point3(t/1000.0,1.6,0.0),rotation=Rotation4(0.0,sin(PI/4),0.0,cos(PI/4)));assertTrue(r.sampleFrame(pose,pose,t,step,Phase.CAPABILITY_BLOCKED,0).gazeReady)}}
 private fun readyRuntime():Pair<GazeRuntime,Long>{val r=GazeRuntime();assertTrue(r.sampleFrame(head(0),null,0,step,Phase.CAPABILITY_BLOCKED,0).gazeReady);return r to 50L}
 @Test fun headAssistedWorldGazeFollowsExpandedVerticalPath(){
  val (r,start)=readyRuntime();var target=Point3(0.0,1.6,-1.8);var f:GazeFrame?=null;var pose=head(start)
  for(t in start..start+4000 step 50){pose=gazeAt(target,t);f=r.sampleFrame(pose,pose,t,Step(Activity.GAZE,Move.VERTICAL,1,10),Phase.ACTIVE,0,0);target=f!!.visual!!.point}
  val raw=GazeGeometry.evaluate(pose,pose,head(0),target,start+4000,GazeRules(3.0,3.0,.05,200,100))
  assertFalse(raw.headStable);assertTrue(f!!.observation.targetHit);assertTrue(f!!.observation.targetReady);assertEquals(1.6+1.8*tan(10*PI/180),target.y,1e-8)
 }
 @Test fun circleRoundBoundaryDoesNotJumpBackToCenter(){val (r,start)=readyRuntime();var t=start;var target=Point3(0.0,1.6,-1.8);var f:GazeFrame?=null;repeat(50){f=r.sampleFrame(gazeAt(target,t),head(t),t,Step(Activity.GAZE,Move.CLOCKWISE,1,5),Phase.ACTIVE,20,12);target=f!!.visual!!.point;t+=50};val before=f!!.visual!!.point;val after=r.sampleFrame(gazeAt(target,t),head(t),t,Step(Activity.GAZE,Move.CLOCKWISE,2,5),Phase.ACTIVE,21,0).visual!!.point;assertEquals(before.x,after.x,1e-6);assertEquals(before.y,after.y,1e-6);assertEquals(before.z,after.z,1e-6)}
 @Test fun permissionWaitCannotCaptureAnOldHeadReference(){val r=GazeRuntime();val old=head(0).copy(rotation=Rotation4(0.0,sin(PI/8),0.0,cos(PI/8)));r.sampleFrame(null,old,0,step,Phase.CAPABILITY_BLOCKED,0);val f=r.sampleFrame(head(1000),head(1000),1000,step,Phase.CAPABILITY_BLOCKED,0);assertTrue(f.observation.headStable);assertEquals(0.0,f.visual!!.point.x,1e-6);assertEquals(-1.8,f.visual!!.point.z,1e-6)}
 @Test fun movingHeadDoesNotMoveFixedWaitingTargetOrPreventReadiness(){
  val r=GazeRuntime();val anchor=head(0);var target=Point3(0.0,1.6,-1.8)
  for(t in 0L..1000 step 50){val moving=head(t).copy(position=Point3(t/1000.0,2.0,0.0),rotation=Rotation4(0.0,sin(t*PI/2000),0.0,cos(t*PI/2000)));val f=r.sampleFrame(gazeAt(target,t),moving,t,step,Phase.CAPABILITY_BLOCKED,0,sceneReference=anchor);target=f.visual!!.point}
  assertTrue(r.gazeReady);assertEquals(0.0,target.x,1e-8);assertEquals(1.6,target.y,1e-8)
 }
 @Test fun fixedSceneAllowsReadinessAndFullPursuitWithoutHmdUpdates(){
  val r=GazeRuntime();val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities());val anchor=head(0)
  var target=Point3(0.0,1.6,-1.8)
  for(t in 0L..70000 step 50){val h=if(t%100==0L)null else head(0);val f=r.sampleFrame(gazeAt(target,t),h,t,s.step,s.phase,s.index,s.targetIndex,sceneReference=anchor);f.visual?.let{target=it.point};if(f.gazeReady&&!s.capabilities.gazeReady)s.updateCapabilities(Capabilities(gazeReady=true))else if(s.capabilities.gazeReady)s.observe(f.observation)}
  assertTrue(s.capabilities.gazeReady);assertEquals(Phase.DONE,s.phase);assertEquals(60000L,s.stepMs+s.activeMs);assertEquals(0,s.interruptions)
 }
 @Test fun eyeLossStillFreezesPathAndPausesSessionWithoutHmd(){
  val (r,start)=readyRuntime();val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities(gazeReady=true))
  val before=r.sampleFrame(head(start),null,start,s.step,s.phase,s.index);assertTrue(before.observation.quality);s.observe(before.observation)
  val missing=r.sampleFrame(null,null,start+50,s.step,s.phase,s.index);s.observe(missing.observation)
  assertFalse(missing.observation.quality);assertTrue(missing.visual!!.frozen);assertEquals(before.visual!!.motionMs,missing.visual!!.motionMs);assertEquals(Phase.TRACKING_PAUSED,s.phase)
 }
 @Test fun retryPreparationCentersAndFreezesTargetBeforeMotion(){
  val (r,start)=readyRuntime()
  for(t in start..start+2950 step 50){val f=r.sampleFrame(head(t),head(t),t,step,Phase.PREPARING,0);assertTrue(f.visual!!.frozen);assertEquals(0L,f.visual!!.motionMs);assertEquals(0.0,f.visual!!.point.x,1e-8);assertEquals(1.6,f.visual!!.point.y,1e-8)}
  val first=r.sampleFrame(head(start+3000),head(start+3000),start+3000,step,Phase.ACTIVE,0)
  assertEquals(0L,first.visual!!.motionMs)
  val next=r.sampleFrame(head(start+3050),head(start+3050),start+3050,step,Phase.ACTIVE,0)
  assertEquals(50L,next.visual!!.motionMs)
 }
 @Test fun resumePreparationKeepsTargetAndPathProgress(){
  val (r,start)=readyRuntime();var f:GazeFrame?=null
  for(t in start..start+1000 step 50)f=r.sampleFrame(head(t),head(t),t,step,Phase.ACTIVE,0)
  val before=f!!.visual!!
  for(t in start+1050..start+4000 step 50){val waiting=r.sampleFrame(head(t),head(t),t,step,Phase.PREPARING,0,restartPreparation=false).visual!!;assertEquals(before.point,waiting.point);assertEquals(before.motionMs,waiting.motionMs);assertTrue(waiting.frozen)}
  val after=r.sampleFrame(head(start+4050),head(start+4050),start+4050,step,Phase.ACTIVE,0).visual!!
  assertEquals(before.point,after.point);assertEquals(before.motionMs,after.motionMs)
 }
 @Test fun fixedSceneCenterStaysInFrontWhenEnteringFromSidePanel(){
  val r=GazeRuntime();val anchor=head(0)
  val side=head(100).copy(rotation=Rotation4(0.0,sin(38.4*PI/360),0.0,cos(38.4*PI/360)))
  val first=r.sampleFrame(side,side,100,step,Phase.CAPABILITY_BLOCKED,0,sceneReference=anchor)
  assertEquals(0.0,first.visual!!.point.x,1e-8);assertEquals(-1.8,first.visual!!.point.z,1e-8)
  val forward=r.sampleFrame(head(200),head(200),200,step,Phase.CAPABILITY_BLOCKED,0,sceneReference=anchor)
  assertEquals(first.visual!!.point,forward.visual!!.point)
 }
 @Test fun allGazeRoundsCanCompleteWithEyeOnlyTracking(){
  val r=GazeRuntime();val s=SessionUseCase(listOf(Activity.GAZE),Capabilities());val anchor=head(0)
  var target=Point3(0.0,1.6,-1.8)
  for(t in 0L..540000 step 50){val f=r.sampleFrame(gazeAt(target,t),null,t,s.step,s.phase,s.index,s.targetIndex,sceneReference=anchor);f.visual?.let{target=it.point};if(f.gazeReady&&!s.capabilities.gazeReady)s.updateCapabilities(Capabilities(gazeReady=true))else if(s.capabilities.gazeReady)s.observe(f.observation)}
  assertEquals(Phase.DONE,s.phase);assertEquals(30,s.completed);assertEquals(0,s.incomplete);assertEquals(0,s.interruptions)
 }
 @Test fun headAssistedWorldGazeCanCompletePursuit(){
  val r=GazeRuntime();val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities());val anchor=head(0)
  var target=Point3(0.0,1.6,-1.8)
  for(t in 0L..70000 step 50){val eyeAndHead=gazeAt(target,t);val f=r.sampleFrame(eyeAndHead,eyeAndHead,t,s.step,s.phase,s.index,s.targetIndex,sceneReference=anchor);f.visual?.let{target=it.point};if(f.gazeReady&&!s.capabilities.gazeReady)s.updateCapabilities(Capabilities(gazeReady=true))else if(s.capabilities.gazeReady)s.observe(f.observation)}
  assertEquals(Phase.DONE,s.phase);assertEquals(Outcome.COMPLETED,s.outcome)
 }
 @Test fun staleEyeStillFreezesPathEvenWithFreshHmd(){
  val (r,start)=readyRuntime();val before=r.sampleFrame(head(start),null,start,step,Phase.ACTIVE,0)
  val stale=r.sampleFrame(head(start-1000),head(start+50),start+50,step,Phase.ACTIVE,0)
  assertFalse(stale.observation.quality);assertTrue(stale.visual!!.frozen);assertEquals(before.visual!!.motionMs,stale.visual!!.motionMs)
 }
 @Test fun freshEyeWithFixedSceneIsReadyWithoutLookingAtFivePoints(){
  val r=GazeRuntime();val anchor=head(0);val eye=gazeAt(Point3(.8,1.6,-1.8),100)
  val f=r.sampleFrame(eye,null,100,step,Phase.CAPABILITY_BLOCKED,0,sceneReference=anchor)
  assertTrue(f.gazeReady);assertTrue(f.observation.quality);assertFalse(f.observation.targetHit)
  assertEquals(Point3(0.0,1.6,-1.8),f.visual!!.point);assertTrue(f.visual!!.frozen)
 }
 @Test fun liveHmdCannotReplaceMissingSceneReference(){
  val r=GazeRuntime();val f=r.frame(head(1000),head(1000),1000,step,Phase.CAPABILITY_BLOCKED,0)
  assertFalse(f.gazeReady);assertNull(f.visual)
 }
 @Test fun staleMissingAndMalformedEyeNeverBecomeReady(){
  val r=GazeRuntime()
  for(eye in listOf(null,head(0),head(1100),head(1000).copy(rotation=Rotation4(0.0,0.0,0.0,0.0)))){
   val f=r.sampleFrame(eye,head(1000),1000,step,Phase.CAPABILITY_BLOCKED,0)
   assertFalse(f.gazeReady);assertFalse(f.observation.quality);assertEquals(0L,f.visual!!.motionMs)
  }
 }
 @Test fun gazeOffTargetFreezesCurrentPointAndResumeDoesNotBackfillMotion(){
  val (r,start)=readyRuntime();val gazeStep=Step(Activity.GAZE,Move.HORIZONTAL,1,10);var target=Point3(0.0,1.6,-1.8);var f:GazeFrame?=null
  for(t in start..start+1000 step 50){f=r.sampleFrame(gazeAt(target,t),null,t,gazeStep,Phase.ACTIVE,0);target=f!!.visual!!.point}
  val before=f!!.visual!!
  for(t in start+1050..start+6050 step 50){f=r.sampleFrame(gazeAt(Point3(2.0,1.6,-1.8),t),null,t,gazeStep,Phase.ACTIVE,0);assertEquals(before.point,f!!.visual!!.point);assertEquals(before.motionMs,f!!.visual!!.motionMs);assertTrue(f!!.visual!!.frozen)}
  val returning=r.sampleFrame(gazeAt(before.point,start+6100),null,start+6100,gazeStep,Phase.ACTIVE,0)
  assertTrue(returning.observation.targetHit);assertEquals(before.motionMs,returning.visual!!.motionMs);assertEquals(before.point,returning.visual!!.point)
  val continuing=r.sampleFrame(gazeAt(before.point,start+6150),null,start+6150,gazeStep,Phase.ACTIVE,0)
  assertEquals(before.motionMs+50,continuing.visual!!.motionMs)
 }
 @Test fun staringOnlyAtFutureEndpointCannotAdvanceTheActualGazePath(){
  val (r,start)=readyRuntime();val s=SessionUseCase(listOf(Activity.GAZE),Capabilities(gazeReady=true));val endpoint=TargetPaths.fromHead(TargetPaths.destination(Move.HORIZONTAL,0),head(0))
  for(t in start..start+10000 step 50){val f=r.sampleFrame(gazeAt(endpoint,t),null,t,s.step,s.phase,s.index,s.targetIndex);s.observe(f.observation);assertEquals(0L,f.visual!!.motionMs);assertEquals(Point3(0.0,1.6,-1.8),f.visual!!.point)}
  assertEquals(0,s.completed);assertEquals(0,s.targetIndex);assertEquals(0L,s.activeMs);assertEquals(Phase.ACTIVE,s.phase)
 }
 @Test fun invalidEyeStillPausesGazeInsteadOfSilentlyCountingWaitingTime(){
  val (r,start)=readyRuntime();val s=SessionUseCase(listOf(Activity.GAZE),Capabilities(gazeReady=true))
  val before=r.sampleFrame(head(start),null,start,s.step,s.phase,s.index);s.observe(before.observation)
  val lost=r.sampleFrame(head(0),null,start+1000,s.step,s.phase,s.index);s.observe(lost.observation)
  assertFalse(lost.observation.quality);assertTrue(lost.visual!!.frozen);assertEquals(before.visual!!.point,lost.visual!!.point);assertEquals(Phase.TRACKING_PAUSED,s.phase)
  val recovered=r.sampleFrame(gazeAt(before.visual!!.point,start+1050),null,start+1050,s.step,s.phase,s.index);s.observe(recovered.observation)
  assertEquals(Phase.ACTIVE,s.phase);assertEquals(0L,s.activeMs);assertEquals(0,s.completed);assertEquals(before.visual!!.motionMs,recovered.visual!!.motionMs)
 }
 private fun GazeRuntime.sampleFrame(eye:TrackedPose?,h:TrackedPose?,now:Long,step:Step?,phase:Phase?,roundIndex:Int,targetIndex:Int=0,restartPreparation:Boolean=true,sceneReference:TrackedPose?=head(0))=frame(eye,h,now,step,phase,roundIndex,targetIndex,restartPreparation,sceneReference)
}
