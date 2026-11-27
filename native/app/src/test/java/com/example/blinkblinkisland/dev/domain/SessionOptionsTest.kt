package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
class SessionOptionsTest {
 @Test fun quickIncludesAllThreeActivitiesWithShorterDoses(){
  val s=SessionUseCase(Activity.entries,Capabilities(),SessionPlan.QUICK)
  assertEquals(8,s.total);assertEquals(3,s.steps.count{it.move==Move.BLINK});assertEquals(30000L,s.pursuitGoalMs)
  assertEquals(4,s.steps.count{it.activity==Activity.GAZE})
 }
 @Test fun fullRetainsOriginalDoses(){val s=SessionUseCase(Activity.entries,Capabilities());assertEquals(41,s.total);assertEquals(60000L,s.pursuitGoalMs)}
 @Test fun quickPursuitStillRequiresGaze(){val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities(gazeReady=true),SessionPlan.QUICK);for(t in 0L..30000 step 50)s.observe(Observation(t,true,true,targetHit=false));assertEquals(0,s.completed)}
 @Test fun quickPursuitFinishesAtThirtyGoodSeconds(){val s=SessionUseCase(listOf(Activity.BUTTERFLY),Capabilities(gazeReady=true),SessionPlan.QUICK);for(t in 0L..29950 step 50)s.observe(Observation(t,true,true,targetHit=true));assertEquals(0,s.completed);s.observe(Observation(30000,true,true,targetHit=true));assertEquals(Phase.DONE,s.phase)}
}
