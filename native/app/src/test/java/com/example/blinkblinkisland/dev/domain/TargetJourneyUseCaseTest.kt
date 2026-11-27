package com.example.blinkblinkisland.dev.domain

import com.example.blinkblinkisland.dev.domain.model.Activity
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class TargetJourneyUseCaseTest {
 @Test fun entryHoldsAfterCompletionUntilExplicitFinish(){
  val j=TargetJourneyUseCase();j.begin(Activity.BUTTERFLY)
  for(t in 0L..3000L step 50)j.tick(t,true)
  assertEquals(JourneyStage.TRAINING,j.snapshot.stage)
  j.waitForEnd();for(t in 3050L..6000L step 50)j.tick(t,true)
  assertEquals(JourneyStage.WAITING_END,j.snapshot.stage)
  j.finish();j.tick(6050,true);j.tick(6100,true);val progress=j.snapshot.progress
  j.finish();assertEquals(progress,j.snapshot.progress,0.0)
  for(t in 6150L..8100L step 50)j.tick(t,true)
  assertEquals(JourneyStage.ISLAND,j.snapshot.stage)
 }
 @Test fun pauseAndBackgroundGapsDoNotAdvanceJourney(){
  val j=TargetJourneyUseCase();j.begin(Activity.GAZE);j.tick(0,true);j.tick(100,true)
  val p=j.snapshot.progress;j.tick(200,false);j.tick(10000,true)
  assertEquals(p,j.snapshot.progress,0.0);j.tick(10050,true);assertTrue(j.snapshot.progress>p)
  val q=j.snapshot.progress;j.tick(20000,true);assertEquals(q,j.snapshot.progress,0.0)
 }
 @Test fun curvesHaveExactEndpointsAndSmoothTakeoff(){
  val a=Point3(1.7,1.9,-2.1);val b=Point3(0.0,1.6,-1.8)
  for(ball in listOf(false,true)){
   assertEquals(a,TargetJourneyUseCase.arc(a,b,0.0,ball));assertEquals(b,TargetJourneyUseCase.arc(a,b,1.0,ball))
   val middle=TargetJourneyUseCase.arc(a,b,.5,ball);assertTrue(middle.y>(a.y+b.y)/2)
   assertEquals(a,TargetJourneyUseCase.arc(a,a,.5,ball))
  }
 }
 @Test fun retryDoesNotRepeatEntryAndExplicitFinishDuringEntryIsAllowed(){
  val j=TargetJourneyUseCase();j.begin(Activity.BUTTERFLY);j.tick(0,true);j.tick(100,true)
  val p=j.snapshot.progress;j.begin(Activity.BUTTERFLY);assertEquals(p,j.snapshot.progress,0.0)
  j.finish();assertEquals(JourneyStage.RETURNING,j.snapshot.stage)
 }
 @Test fun modeHandoffReturnsOldTargetBeforeSendingNewOne(){
  val j=TargetJourneyUseCase();j.begin(Activity.BUTTERFLY);for(t in 0L..3000L step 50)j.tick(t,true)
  j.begin(Activity.GAZE);assertEquals(JourneyStage.HANDOFF,j.snapshot.stage);assertEquals(Activity.BUTTERFLY,j.snapshot.activity)
  for(t in 3050L..5050L step 50){j.begin(Activity.GAZE);j.tick(t,true)}
  assertEquals(JourneyStage.ENTERING,j.snapshot.stage);assertEquals(Activity.GAZE,j.snapshot.activity)
  for(t in 5100L..8050L step 50)j.tick(t,true)
  assertEquals(JourneyStage.TRAINING,j.snapshot.stage)
 }

 @Test fun skippingDuringEntryUsesCurrentTargetAndTerminalHandoffCancelsPending(){
  val j=TargetJourneyUseCase();j.begin(Activity.BUTTERFLY);j.tick(0,true);j.tick(100,true)
  j.begin(Activity.GAZE);assertEquals(JourneyStage.HANDOFF,j.snapshot.stage);assertEquals(Activity.BUTTERFLY,j.snapshot.activity)
  j.waitForEnd();for(t in 150L..6000L step 50)j.tick(t,true)
  assertEquals(JourneyStage.WAITING_END,j.snapshot.stage);assertEquals(Activity.BUTTERFLY,j.snapshot.activity)
  j.finish();for(t in 6050L..8050L step 50)j.tick(t,true)
  assertEquals(JourneyStage.ISLAND,j.snapshot.stage)
 }

}
