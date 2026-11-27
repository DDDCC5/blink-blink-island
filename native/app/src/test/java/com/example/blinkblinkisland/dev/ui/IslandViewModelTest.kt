package com.example.blinkblinkisland.dev.ui
import com.example.blinkblinkisland.dev.data.repository.*
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import com.example.blinkblinkisland.dev.ui.island.*
import com.example.blinkblinkisland.dev.ui.navigation.Page
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
@OptIn(ExperimentalCoroutinesApi::class)
class IslandViewModelTest {
 private val d=StandardTestDispatcher()
 private class Memory: IslandRepository {
  val items=linkedMapOf<String,Record>();var config=Settings();var failSaves=0
  override fun records()=items.values.toList()
  override fun save(record:Record){if(failSaves>0){failSaves--;throw IllegalStateException("test storage unavailable")};items.putIfAbsent(record.id,record)}
  override fun deleteRecords(){items.clear()}
  override fun settings()=config
  override fun saveSettings(settings:Settings){config=settings}
 }
 @Before fun setup(){Dispatchers.setMain(d)}
 @After fun cleanup(){Dispatchers.resetMain()}
 @Test fun firstRunShowsWelcome()=runTest(d){val vm=IslandViewModel(IslandUseCase(Memory()),d);advanceUntilIdle();assertEquals(Page.WELCOME,vm.state.value.page)}
 @Test fun acceptingWelcomePersistsChoice()=runTest(d){val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle();vm.onEvent(IslandEvent.AcceptWelcome);advanceUntilIdle();assertTrue(r.config.onboarding);assertEquals(Page.HOME,vm.state.value.page)}
 @Test fun skipNeverCreatesSuccessRecord()=runTest(d){val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle();vm.onEvent(IslandEvent.Start(listOf(Activity.BLINK)));repeat(10){vm.onEvent(IslandEvent.Skip)};advanceUntilIdle();assertEquals(1,r.items.size);assertEquals(Outcome.SKIPPED,r.records()[0].outcome);assertEquals(0,r.records()[0].completed)}
 @Test fun duplicateExitSavesOnlyOnce()=runTest(d){val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle();vm.onEvent(IslandEvent.Start(listOf(Activity.GAZE)));vm.onEvent(IslandEvent.Exit);vm.onEvent(IslandEvent.Exit);advanceUntilIdle();assertEquals(1,r.items.size)}
 @Test fun deletionRequiresConfirmationPage()=runTest(d){val r=Memory();r.save(Record("a",1,2,"BLINK",Outcome.SKIPPED,1,0,0,1,0,0,0));val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle();vm.onEvent(IslandEvent.DeleteRecords);advanceUntilIdle();assertEquals(1,r.items.size);vm.onEvent(IslandEvent.Navigate(Page.DELETE_CONFIRM));vm.onEvent(IslandEvent.DeleteRecords);advanceUntilIdle();assertTrue(r.items.isEmpty())}
 @Test fun savedSettingsClampToDesignRange()=runTest(d){val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle();vm.onEvent(IslandEvent.ChangeSettings(Settings(interval=500)));advanceUntilIdle();assertEquals(120,r.config.interval)}

 @Test fun failedSaveCanBeRetriedWithoutLosingRecord()=runTest(d){val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle();r.failSaves=1;vm.onEvent(IslandEvent.Start(listOf(Activity.BLINK)));vm.onEvent(IslandEvent.Exit);advanceUntilIdle();assertTrue(r.items.isEmpty());assertTrue(vm.state.value.savePending);vm.onEvent(IslandEvent.RetrySave);advanceUntilIdle();assertEquals(1,r.items.size);assertFalse(vm.state.value.savePending)}
 @Test fun notificationRouteIsNotOverriddenByOldFinishedSession()=runTest(d){val vm=IslandViewModel(IslandUseCase(Memory()),d);advanceUntilIdle();vm.onEvent(IslandEvent.Start(listOf(Activity.BLINK)));vm.onEvent(IslandEvent.Exit);advanceUntilIdle();vm.onEvent(IslandEvent.Route("start"));vm.onEvent(IslandEvent.Sensor(Observation(1000,true,true),"test"));assertEquals(Page.HOME,vm.state.value.page)}
 @Test fun inputDuringInitializationDoesNotOverwriteSavedSettings()=runTest(d){val r=Memory();r.config=Settings(interval=100,music=.9f,onboarding=true);val vm=IslandViewModel(IslandUseCase(r),d);vm.onEvent(IslandEvent.ToggleLanguage);advanceUntilIdle();assertEquals(100,r.config.interval);assertEquals(.9f,r.config.music);assertTrue(r.config.onboarding)}
 @Test fun firstTrainingFrameWaitsForFixedHorizontalSceneReference()=runTest(d){
  val vm=IslandViewModel(IslandUseCase(Memory()),d);advanceUntilIdle();vm.onEvent(IslandEvent.Start(listOf(Activity.BUTTERFLY)))
  val p=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(.1736481777,0.0,0.0,.984807753),100)
  vm.onEvent(IslandEvent.Frame(p,p,100,"test"));assertNull(vm.trainingVisual)
  val ref=p.copy(rotation=Rotation4(0.0,0.0,0.0,1.0))
  vm.onEvent(IslandEvent.Frame(p.copy(atMs=150),p.copy(atMs=150),150,"test",ref))
  assertNotNull(vm.trainingVisual);assertEquals(1.6,vm.trainingVisual!!.point.y,1e-8);assertEquals(-1.8,vm.trainingVisual!!.point.z,1e-8)
 }
 @Test fun actualStartUsesFreshEyeThenThreeSecondCountdownWithoutFivePointGate()=runTest(d){
  val vm=IslandViewModel(IslandUseCase(Memory()),d);advanceUntilIdle();vm.onEvent(IslandEvent.Start(listOf(Activity.BUTTERFLY)))
  val anchor=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),0)
  fun frame(t:Long,eye:TrackedPose?=anchor.copy(atMs=t)){vm.onEvent(IslandEvent.Frame(eye,null,t,"test",anchor))}
  frame(1000,null);frame(1050,anchor);assertEquals(Phase.CAPABILITY_BLOCKED,vm.state.value.phase)
  frame(1100);assertEquals(Phase.PREPARING,vm.state.value.phase);assertEquals(3000L,vm.state.value.preparationMs)
  assertFalse(vm.state.value.calibrating);assertEquals(0,vm.state.value.calibrationIndex)
  for(t in 1150L..4100 step 50)frame(t)
  assertEquals(Phase.PREPARING,vm.state.value.phase);assertEquals(0L,vm.state.value.progressMs)
  frame(4150);assertEquals(Phase.ACTIVE,vm.state.value.phase);assertEquals(0L,vm.state.value.progressMs)
  frame(4200);frame(4250);assertEquals(50L,vm.state.value.progressMs)
  frame(4300,null);assertEquals(Phase.TRACKING_PAUSED,vm.state.value.phase);assertEquals(50L,vm.state.value.progressMs)
 }
 @Test fun guidedRestSavesSeparateKindAndExitRemainsExited()=runTest(d){
  val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle()
  vm.onEvent(IslandEvent.Start(listOf(Activity.BLINK)))
  for(t in 0L..3000 step 50)vm.onEvent(IslandEvent.GuideTick(t))
  val id=vm.state.value.sessionId
  vm.onEvent(IslandEvent.GuidePromptFinished(id,0,BlinkGuideStage.CLOSE_PROMPT))
  for(t in 4000L..7000 step 50)vm.onEvent(IslandEvent.GuideTick(t))
  vm.onEvent(IslandEvent.GuidePromptFinished(id,0,BlinkGuideStage.OPEN_PROMPT))
  for(t in 8000L..10000 step 50)vm.onEvent(IslandEvent.GuideTick(t))
  assertEquals(1,vm.state.value.completed)
  vm.onEvent(IslandEvent.Exit);advanceUntilIdle()
  assertEquals("BLINK_GUIDED",r.records().single().kind);assertEquals(Outcome.EXITED,r.records().single().outcome)
 }
 @Test fun oldSessionAudioCompletionCannotAdvanceNewSession()=runTest(d){
  val vm=IslandViewModel(IslandUseCase(Memory()),d);advanceUntilIdle()
  vm.onEvent(IslandEvent.Start(listOf(Activity.BLINK)));val old=vm.state.value.sessionId
  vm.onEvent(IslandEvent.Start(listOf(Activity.BLINK)))
  for(t in 0L..3000 step 50)vm.onEvent(IslandEvent.GuideTick(t))
  vm.onEvent(IslandEvent.GuidePromptFinished(old,0,BlinkGuideStage.CLOSE_PROMPT))
  assertEquals(BlinkGuideStage.CLOSE_PROMPT,vm.state.value.blinkStage)
 }

 @Test fun setupDoesNotStartOrSaveTrainingUntilBeginAndRemembersChoice()=runTest(d){
  val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle()
  vm.onEvent(IslandEvent.ChooseSession(Activity.entries,SessionPlan.QUICK))
  assertEquals(Page.PREPARE,vm.state.value.page);assertNull(vm.state.value.step);assertTrue(r.items.isEmpty())
  vm.onEvent(IslandEvent.SelectPace(Pace.COMFORTABLE));vm.onEvent(IslandEvent.Begin);advanceUntilIdle()
  assertEquals(Page.TRAINING,vm.state.value.page);assertEquals(8,vm.state.value.total)
  assertEquals(Pace.COMFORTABLE,r.config.pace);assertEquals(SessionPlan.QUICK,r.config.lastPlan)
  vm.onEvent(IslandEvent.Exit);advanceUntilIdle();assertEquals("QUICK_GUIDED",r.records().single().kind)
 }

 @Test fun islandEntryNeverAccumulatesPracticeAndExitSavesBeforeReturn()=runTest(d){
  val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle()
  vm.onEvent(IslandEvent.Start(listOf(Activity.BUTTERFLY)))
  val anchor=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),0)
  fun frame(t:Long){vm.onEvent(IslandEvent.Frame(anchor.copy(atMs=t),null,t,"test",anchor))}
  frame(0);assertEquals(JourneyStage.ENTERING,vm.state.value.journey.stage)
  for(t in 50L..2900L step 50)frame(t)
  assertEquals(0L,vm.state.value.progressMs)
  vm.onEvent(IslandEvent.Exit);advanceUntilIdle()
  assertEquals(Outcome.EXITED,r.records().single().outcome)
  assertEquals(JourneyStage.RETURNING,vm.state.value.journey.stage)
  frame(2950);frame(3000);val progress=vm.state.value.journey.progress
  vm.onEvent(IslandEvent.Exit);assertEquals(progress,vm.state.value.journey.progress,0.0)
  for(t in 3050L..5000L step 50)frame(t)
  assertEquals(JourneyStage.ISLAND,vm.state.value.journey.stage);assertNull(vm.trainingVisual)
  advanceUntilIdle();assertEquals(1,r.items.size)
 }
 @Test fun terminalSummaryKeepsTargetUntilUserConfirmsFinish()=runTest(d){
  val vm=IslandViewModel(IslandUseCase(Memory()),d);advanceUntilIdle()
  vm.onEvent(IslandEvent.Start(listOf(Activity.BUTTERFLY)))
  val anchor=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),0)
  vm.onEvent(IslandEvent.Frame(anchor,null,0,"test",anchor))
  val target=vm.trainingVisual;assertNotNull(target)
  vm.onEvent(IslandEvent.SkipActivity);advanceUntilIdle()
  assertEquals(Page.SUMMARY,vm.state.value.page);assertEquals(JourneyStage.WAITING_END,vm.state.value.journey.stage)
  vm.onEvent(IslandEvent.Frame(null,null,10000,"test",anchor))
  assertEquals(target,vm.trainingVisual);assertEquals(JourneyStage.WAITING_END,vm.state.value.journey.stage)
  vm.onEvent(IslandEvent.Exit);assertEquals(JourneyStage.RETURNING,vm.state.value.journey.stage)
 }

 @Test fun automaticCompletionWaitsAndCombinedModeHandoffDoesNotCreateARecord()=runTest(d){
  val r=Memory();val vm=IslandViewModel(IslandUseCase(r),d);advanceUntilIdle()
  vm.onEvent(IslandEvent.Start(listOf(Activity.BUTTERFLY,Activity.GAZE),SessionPlan.QUICK))
  val anchor=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),0)
  fun frame(t:Long){vm.onEvent(IslandEvent.Frame(anchor.copy(atMs=t),null,t,"test",anchor))}
  for(t in 0L..3050L step 50)frame(t)
  for(t in 3100L..33200L step 50)vm.onEvent(IslandEvent.Sensor(Observation(t,true,true,targetHit=true,targetReady=true),"test"))
  assertEquals(1,vm.state.value.completed);assertEquals(Activity.GAZE,vm.state.value.step?.activity)
  val butterfly=vm.trainingVisual
  frame(33250);assertEquals(JourneyStage.HANDOFF,vm.state.value.journey.stage)
  assertEquals(butterfly,vm.trainingVisual);assertTrue(r.items.isEmpty())
  for(t in 33300L..35250L step 50)frame(t)
  assertEquals(JourneyStage.HANDOFF,vm.state.value.journey.stage)
  frame(35300);assertEquals(JourneyStage.ENTERING,vm.state.value.journey.stage)
  assertEquals(Activity.GAZE,vm.state.value.journey.activity);assertEquals(0L,vm.state.value.progressMs)
  assertFalse(vm.trainingVisual!!.butterfly)
  vm.onEvent(IslandEvent.SkipActivity);advanceUntilIdle()
  assertEquals(Page.SUMMARY,vm.state.value.page);assertEquals(JourneyStage.WAITING_END,vm.state.value.journey.stage)
  assertEquals(1,r.items.size)
 }
 @Test fun userPauseFreezesEntryAndResumeDoesNotResendTarget()=runTest(d){
  val vm=IslandViewModel(IslandUseCase(Memory()),d);advanceUntilIdle();vm.onEvent(IslandEvent.Start(listOf(Activity.GAZE)))
  val a=TrackedPose(Point3(0.0,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),0)
  fun frame(t:Long){vm.onEvent(IslandEvent.Frame(a.copy(atMs=t),null,t,"test",a))}
  for(t in 0L..500L step 50)frame(t)
  val p=vm.state.value.journey.progress;vm.onEvent(IslandEvent.Pause)
  for(t in 550L..1500L step 50)frame(t)
  assertEquals(p,vm.state.value.journey.progress,0.0)
  vm.onEvent(IslandEvent.Resume);frame(1550);frame(1600)
  assertTrue(vm.state.value.journey.progress>p);assertEquals(JourneyStage.ENTERING,vm.state.value.journey.stage)
 }

}
