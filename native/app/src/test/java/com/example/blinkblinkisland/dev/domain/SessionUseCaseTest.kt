package com.example.blinkblinkisland.dev.domain

import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test

class SessionUseCaseTest {
 private val caps=Capabilities(eyelid=true, calibrated=true)
 private fun sample(t:Long,closed:Boolean?=null,hit:Boolean=true,quality:Boolean=true,head:Boolean=true)=Observation(t,quality,head,closed,hit)
 @Test fun missingEyelidBlocksBlinkWithoutCountingSuccess(){ val s=SessionUseCase(listOf(Activity.BLINK),Capabilities());assertEquals(Phase.CAPABILITY_BLOCKED,s.phase);s.observe(sample(5000,true));assertEquals(0,s.completed) }
 @Test fun elapsedTimeWithoutGazeNeverCompletesPursuit(){val s=SessionUseCase(listOf(Activity.BUTTERFLY),caps);for(t in 0L..61000L step 50)s.observe(sample(t,hit=false));assertEquals(0,s.completed)}
 @Test fun closureMustHoldThreeSecondsThenReopen(){val s=SessionUseCase(listOf(Activity.BLINK),caps);for(t in 0L..3000L step 50)s.observe(sample(t,true));assertEquals(0,s.completed);s.observe(sample(3050,false));assertEquals(1,s.completed)}
 @Test fun earlyOpeningIsIncompleteAndRetryKeepsRound(){val s=SessionUseCase(listOf(Activity.BLINK),caps);for(t in 0L..500L step 50)s.observe(sample(t,true));s.observe(sample(550,false));assertEquals(Phase.INCOMPLETE,s.phase);assertEquals(1,s.incomplete);s.retry();assertEquals(0,s.index)}
 @Test fun skipIsNeverSuccess(){val s=SessionUseCase(listOf(Activity.BLINK),Capabilities());s.skip();assertEquals(1,s.skipped);assertEquals(0,s.completed);assertEquals(1,s.index)}
 @Test fun pauseCannotAccumulateTime(){val s=SessionUseCase(listOf(Activity.BLINK),caps);s.observe(sample(0,true));s.observe(sample(1000,true));s.pause();s.observe(sample(9000,true));s.resume();s.observe(sample(10000,true));s.observe(sample(10050,false));assertEquals(0,s.completed)}
 @Test fun trackingLossDoesNotCountUnobservedClosure(){val s=SessionUseCase(listOf(Activity.BLINK),caps);for(t in 0L..1000L step 50)s.observe(sample(t,true));s.observe(sample(1050,true,quality=false));assertEquals(Phase.TRACKING_PAUSED,s.phase);s.observe(sample(8000,true));s.observe(sample(8050,false));assertEquals(0,s.completed);assertEquals(1,s.interruptions)}
 @Test fun largeTimestampGapDoesNotCountAsGoodTracking(){val s=SessionUseCase(listOf(Activity.BUTTERFLY),caps);s.observe(sample(0));s.observe(sample(60000));assertEquals(0,s.completed);assertEquals(0L,s.activeMs)}
 @Test fun sustainedHeadMovementFailsMove(){val s=SessionUseCase(listOf(Activity.GAZE),caps);for(t in 0L..500 step 50)s.observe(sample(t,head=false));assertEquals(Phase.INCOMPLETE,s.phase)}
 @Test fun briefHeadAdjustmentPausesWithoutLosingValidPursuitTime(){val s=SessionUseCase(listOf(Activity.BUTTERFLY),caps);for(t in 0L..1000 step 50)s.observe(sample(t));for(t in 1050L..1250 step 50)s.observe(sample(t,head=false));assertEquals(Phase.ACTIVE,s.phase);assertEquals(1000L,s.activeMs);s.observe(sample(1300));assertEquals(1000L,s.activeMs);s.observe(sample(1350));assertEquals(1050L,s.activeMs);assertEquals(0,s.incomplete)}
 @Test fun pursuitNeedsSixtySecondsOfValidSamples(){val s=SessionUseCase(listOf(Activity.BUTTERFLY),caps);for(t in 0L..60000L step 50)s.observe(sample(t));assertEquals(Phase.DONE,s.phase);assertEquals(1,s.completed)}
 @Test fun fullSetHasFortyOneJudgedActions(){val s=SessionUseCase(Activity.entries,Capabilities());assertEquals(41,s.total)}
 @Test fun allSkippedSessionIsNotCompleted(){val s=SessionUseCase(listOf(Activity.BLINK),Capabilities());repeat(10){s.skip()};assertEquals(Phase.DONE,s.phase);assertEquals(Outcome.SKIPPED,s.outcome)}
 @Test fun exitKeepsExplicitExitedOutcome(){val s=SessionUseCase(listOf(Activity.GAZE),caps);s.exit();assertEquals(Outcome.EXITED,s.outcome)}
 @Test fun uncalibratedEyeActivitiesAreGated(){val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities());assertEquals(Phase.CAPABILITY_BLOCKED,s.phase)}
 @Test fun targetsMustBeHitInOrder(){val s=SessionUseCase(listOf(Activity.GAZE),caps);assertEquals(0,s.targetIndex);for(t in 0L..200L step 50)s.observe(sample(t));assertEquals(1,s.targetIndex);assertEquals(0,s.completed)}
 @Test fun exitingActiveObservedAttemptKeepsAttemptCount(){val s=SessionUseCase(listOf(Activity.GAZE),caps);s.observe(sample(0));s.exit();assertEquals(1,s.attempts)}
 @Test fun circleMustReturnToStartingPoint(){val s=SessionUseCase(listOf(Activity.GAZE),caps);repeat(20){s.skip()};for(t in -3000L..0 step 50)s.observe(sample(t));for(t in 0L..2400 step 50)s.observe(sample(t));assertEquals(0,s.completed);assertEquals(12,s.targetIndex);for(t in 2450L..2600 step 50)s.observe(sample(t));assertEquals(1,s.completed)}
 @Test fun validTargetTravelCountsActivityButCannotAdvanceDwell(){val s=SessionUseCase(listOf(Activity.GAZE),caps);for(t in 0L..3000 step 50)s.observe(sample(t).copy(targetReady=false));assertEquals(Phase.ACTIVE,s.phase);assertEquals(0,s.targetIndex);assertEquals(0,s.completed);assertEquals(3000L,s.activeMs);assertEquals(3000L,s.stepMs)}
 @Test fun capabilityAdmissionOnlyFollowsVerifiedCalibration(){val s=SessionUseCase(listOf(Activity.GAZE),Capabilities());s.updateCapabilities(Capabilities(calibrated=true));assertEquals(Phase.ACTIVE,s.phase)}
 @Test fun explicitSkipActivityPreservesUncompletedRounds(){val s=SessionUseCase(Activity.entries,Capabilities());s.skipActivity();assertEquals(Activity.BUTTERFLY,s.step?.activity);assertEquals(10,s.skipped);assertEquals(0,s.completed)}
 @Test fun jumpingOnlyToEndpointsDoesNotQualifyPath(){val s=SessionUseCase(listOf(Activity.GAZE),caps);for(t in 0L..2000 step 50)s.observe(sample(t,hit=t<1200).copy(targetReady=false));s.observe(sample(2050));assertEquals(Phase.INCOMPLETE,s.phase);assertEquals(0,s.completed)}
 @Test fun retryGivesThreeSecondsToReturnFromTheButtonWithoutJudging(){
  val s=SessionUseCase(listOf(Activity.BUTTERFLY),caps)
  for(t in 0L..1500 step 50)s.observe(sample(t,hit=false))
  assertEquals(Phase.INCOMPLETE,s.phase);s.retry()
  assertEquals("PREPARING",s.phase.name)
  for(t in 2000L..4950 step 50)s.observe(sample(t,hit=false,head=false))
  assertEquals("PREPARING",s.phase.name);assertEquals(1,s.incomplete);assertEquals(0L,s.stepMs)
  s.observe(sample(5000,head=false));assertEquals(Phase.ACTIVE,s.phase);assertEquals(0L,s.stepMs)
  s.observe(sample(5050));assertEquals(0L,s.stepMs)
  s.observe(sample(5100));assertEquals(50L,s.stepMs)
 }
 @Test fun pausingRetryCountdownDoesNotConsumeBackgroundTime(){
  val s=SessionUseCase(listOf(Activity.BUTTERFLY),caps)
  for(t in 0L..1500 step 50)s.observe(sample(t,hit=false));s.retry()
  for(t in 2000L..3000 step 50)s.observe(sample(t));s.pause()
  s.observe(sample(10000));assertEquals(Phase.USER_PAUSED,s.phase);s.resume()
  s.observe(sample(11000));assertEquals("PREPARING",s.phase.name)
  for(t in 11050L..12950 step 50)s.observe(sample(t));assertEquals("PREPARING",s.phase.name)
  s.observe(sample(13000));assertEquals(Phase.ACTIVE,s.phase);assertEquals(0L,s.stepMs)
 }
 @Test fun resumeAllowsReturningFromSidePanelAndPreservesScoredTime(){
  val s=SessionUseCase(listOf(Activity.BUTTERFLY),caps)
  for(t in 0L..1000 step 50)s.observe(sample(t));s.pause();s.resume()
  assertEquals("PREPARING",s.phase.name)
  for(t in 2000L..4950 step 50)s.observe(sample(t,head=false))
  assertEquals("PREPARING",s.phase.name);assertEquals(1000L,s.stepMs);assertEquals(0,s.incomplete)
  s.observe(sample(5000));assertEquals(Phase.ACTIVE,s.phase);s.observe(sample(5050));s.observe(sample(5100));assertEquals(1050L,s.stepMs)
 }
 @Test fun skippingEyeRoundAlsoAllowsTimeToReturnFromThePanel(){
  val s=SessionUseCase(listOf(Activity.GAZE),caps);s.skip()
  assertEquals("PREPARING",s.phase.name);assertEquals(1,s.skipped);assertEquals(0,s.completed)
  for(t in 0L..2950 step 50)s.observe(sample(t,head=false))
  assertEquals("PREPARING",s.phase.name);assertEquals(0,s.incomplete)
  s.observe(sample(3000));assertEquals(Phase.ACTIVE,s.phase);assertEquals(0L,s.stepMs)
 }
}
