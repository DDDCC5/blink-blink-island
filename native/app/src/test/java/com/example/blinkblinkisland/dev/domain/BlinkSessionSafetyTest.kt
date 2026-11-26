package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.SessionUseCase
import org.junit.Assert.*
import org.junit.Test

/** Guided playback/clock rules, not claims about observed eyelids. */
class BlinkSessionSafetyTest {
 private fun session()=SessionUseCase(listOf(Activity.BLINK),Capabilities())
 private fun prepare(s:SessionUseCase,start:Long=0){for(t in start..start+3000 step 50)s.guideTick(t)}
 private fun cycle(s:SessionUseCase,start:Long){
  s.guidePromptFinished(s.index,BlinkGuideStage.CLOSE_PROMPT)
  for(t in start..start+3000 step 50)s.guideTick(t)
  assertEquals(BlinkGuideStage.OPEN_PROMPT,s.blinkStage)
  s.guidePromptFinished(s.index,BlinkGuideStage.OPEN_PROMPT)
  for(t in start+3050..start+5050 step 50)s.guideTick(t)
 }
 @Test fun tenGuidedCyclesCompleteWithoutEyelidOrGazeData(){val s=session();prepare(s);repeat(10){cycle(s,4000+it*6000L)};assertEquals(10,s.completed);assertEquals(Phase.DONE,s.phase);assertEquals(Outcome.COMPLETED,s.outcome)}
 @Test fun timerCannotSkipTheSpokenInstructions(){val s=session();prepare(s);for(t in 4000L..20000 step 50)s.guideTick(t);assertEquals(BlinkGuideStage.CLOSE_PROMPT,s.blinkStage);assertEquals(0,s.completed)}
 @Test fun closedHoldStartsAfterClosePromptAndReopenHasItsOwnPrompt(){val s=session();prepare(s);s.guidePromptFinished(0,BlinkGuideStage.CLOSE_PROMPT);for(t in 4000L..6950 step 50)s.guideTick(t);assertEquals(BlinkGuideStage.HOLD,s.blinkStage);s.guideTick(7000);assertEquals(BlinkGuideStage.OPEN_PROMPT,s.blinkStage);assertEquals(0,s.completed);for(t in 7050L..10000 step 50)s.guideTick(t);assertEquals(0,s.completed)}
 @Test fun eyeLossAndHeadMovementDoNotInterruptGuidance(){val s=session();prepare(s);s.observe(Observation(3100,false,false));assertEquals(Phase.ACTIVE,s.phase);assertEquals(0,s.interruptions);cycle(s,4000);assertEquals(1,s.completed)}
 @Test fun pauseStopsProgressAndResumeRestartsCurrentRoundAfterCountdown(){val s=session();prepare(s);s.guidePromptFinished(0,BlinkGuideStage.CLOSE_PROMPT);for(t in 4000L..5000 step 50)s.guideTick(t);s.pause();for(t in 5050L..10000 step 50)s.guideTick(t);assertEquals(0,s.completed);s.resume();assertEquals(Phase.PREPARING,s.phase);prepare(s,11000);assertEquals(BlinkGuideStage.CLOSE_PROMPT,s.blinkStage);cycle(s,15000);assertEquals(1,s.completed)}
 @Test fun delayedPromptFromPreviousRoundIsIgnored(){val s=session();prepare(s);cycle(s,4000);s.guidePromptFinished(0,BlinkGuideStage.CLOSE_PROMPT);assertEquals(BlinkGuideStage.CLOSE_PROMPT,s.blinkStage)}
 @Test fun clockStallsAndReversedTimesDoNotBackfillHold(){val s=session();prepare(s);s.guidePromptFinished(0,BlinkGuideStage.CLOSE_PROMPT);s.guideTick(4000);s.guideTick(50000);s.guideTick(49000);assertEquals(0L,s.stepMs);assertEquals(0,s.completed)}
 @Test fun skippingNeverCountsAsCompletion(){val s=session();repeat(10){s.skip()};assertEquals(0,s.completed);assertEquals(Outcome.SKIPPED,s.outcome)}
 @Test fun exitDuringGuidanceIsExited(){val s=session();prepare(s);cycle(s,4000);s.exit();assertEquals(Outcome.EXITED,s.outcome);assertEquals(1,s.completed)}
 @Test fun fullSessionMovesFromGuidedRestToGazePermissionGate(){val s=SessionUseCase(Activity.entries,Capabilities());prepare(s);repeat(10){cycle(s,4000+it*6000L)};assertEquals(Activity.BUTTERFLY,s.step?.activity);assertEquals(Phase.CAPABILITY_BLOCKED,s.phase);assertEquals(10,s.completed)}
}
