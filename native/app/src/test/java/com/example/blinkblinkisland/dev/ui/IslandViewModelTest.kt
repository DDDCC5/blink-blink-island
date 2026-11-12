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
}
