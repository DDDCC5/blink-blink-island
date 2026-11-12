package com.example.blinkblinkisland.dev.ui.island
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import com.example.blinkblinkisland.dev.ui.navigation.Page
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class IslandViewModel(private val store:IslandUseCase,private val dispatcher:CoroutineDispatcher=Dispatchers.IO):ViewModel(){
 private val mutable=MutableStateFlow(IslandUiState())
 val state:StateFlow<IslandUiState> = mutable.asStateFlow()
 private val writes=Mutex()
 private var session:SessionUseCase?=null
 private var gaze=GazeRuntime()
 var trainingVisual:TrainingVisual?=null;private set
 private var sessionId=""
 private var started=0L
 private val savedIds=mutableSetOf<String>()
 private val savingIds=mutableSetOf<String>()
 private val pending=linkedMapOf<String,Record>()
 init{io{val s=store.settings();val rows=store.records();mutable.update{it.copy(settings=s,records=rows,page=if(s.onboarding)Page.HOME else Page.WELCOME,busy=false)}}}
 private fun io(onFailure:(Exception)->Unit={},onSuccess:()->Unit={},block:()->Unit){viewModelScope.launch{writes.withLock{try{withContext(dispatcher){block()};onSuccess()}catch(e:Exception){if(e is CancellationException)throw e;mutable.update{it.copy(error="Local operation failed: ${e.javaClass.simpleName}",busy=false)};onFailure(e)}}}}
 private fun settings(s:Settings){val safe=s.copy(interval=s.interval.coerceIn(30,120),music=s.music.coerceIn(0f,1f),effects=s.effects.coerceIn(0f,1f),voice=s.voice.coerceIn(0f,1f));mutable.update{it.copy(settings=safe)};io{store.update(safe)}}
 fun onEvent(event:IslandEvent){
 if(state.value.busy&&event !is IslandEvent.Sensor&&event !is IslandEvent.Scene&&event !is IslandEvent.Route)return
 when(event){
  is IslandEvent.Navigate->{if(session!=null&&event.page!=Page.TRAINING){session?.exit();saveSession();session=null};mutable.update{it.copy(page=event.page)}}
  IslandEvent.AcceptWelcome->{settings(state.value.settings.copy(onboarding=true));mutable.update{it.copy(page=Page.HOME)}}
  IslandEvent.ToggleLanguage->settings(state.value.settings.copy(language=if(state.value.settings.language=="zh")"en" else "zh"))
  is IslandEvent.EyePermission->mutable.update{it.copy(eyePermissionGranted=event.granted)}
  is IslandEvent.ChangeSettings->settings(event.settings)
  is IslandEvent.Start->{if(event.activities.isEmpty())return;session?.let{it.exit();saveSession()};gaze=GazeRuntime();trainingVisual=null;session=SessionUseCase(event.activities,Capabilities());sessionId=UUID.randomUUID().toString();started=System.currentTimeMillis();mutable.update{it.copy(page=Page.TRAINING,error=null,calibrating=false)};sync()}
  IslandEvent.Recalibrate->{gaze=GazeRuntime();trainingVisual=null;session?.retry();session?.updateCapabilities(Capabilities());sync()}
  IslandEvent.Pause->{session?.pause();sync()}
  IslandEvent.Resume->{session?.resume();sync()}
  IslandEvent.Retry->{session?.retry();sync()}
  IslandEvent.Skip->{session?.skip();sync();saveIfTerminal()}
  IslandEvent.SkipActivity->{session?.skipActivity();sync();saveIfTerminal()}
  IslandEvent.Exit->{session?.exit();sync();saveSession();mutable.update{it.copy(page=Page.SUMMARY)}}
  IslandEvent.RetrySave->pending.values.toList().forEach(::enqueueSave)
  IslandEvent.DeleteRecords->{if(state.value.page!=Page.DELETE_CONFIRM)return;session=null;mutable.update{it.copy(busy=true)};io(onSuccess={pending.clear();savedIds.clear();savingIds.clear();mutable.update{it.copy(savePending=false)}}){store.deleteRecords();mutable.update{it.copy(records=emptyList(),page=Page.HISTORY,busy=false)}}}
  is IslandEvent.Sensor->{mutable.update{it.copy(sensorStatus=event.status)};session?.observe(event.observation);sync();saveIfTerminal()}
  is IslandEvent.Frame->{
   mutable.update{it.copy(sensorStatus=event.status)}
   val s=session
   if(s!=null&&state.value.page==Page.TRAINING){
    // The first valid tracking frame may precede persistent scene placement by one tick.
    if(event.sceneReference==null&&s.step?.move!=Move.BLINK){trainingVisual=null;return}
    val f=gaze.frame(event.eye,event.head,event.now,s.step,s.phase,s.index,s.targetIndex,s.preparationRestartsPath,event.sceneReference)
    trainingVisual=f.visual
    if(f.calibrated&&!s.capabilities.calibrated)s.updateCapabilities(Capabilities(calibrated=true)) else if(f.calibrated)s.observe(f.observation)
    mutable.update{it.copy(calibrating=s.step?.move!=Move.BLINK&&!f.calibrated,calibrationIndex=gaze.calibration.index,calibrationHold=gaze.calibration.heldMs,trackingGood=f.observation.quality,headStable=f.observation.headStable,targetHit=f.observation.targetHit,sensorStatus="${event.status} gate=${f.observation.issue} turnBand=${f.observation.headRotationBand} shiftBand=${f.observation.headShiftBand}")}
    sync();saveIfTerminal()
   }else trainingVisual=null
  }
  is IslandEvent.Scene->mutable.update{it.copy(sceneLoaded=event.loaded,error=event.error?:it.error)}
  is IslandEvent.Route->{mutable.update{it.copy(route=event.action)};if(event.action=="start")onEvent(IslandEvent.Navigate(Page.HOME)) }
 }}
 private fun sync(){session?.let{s->mutable.update{it.copy(phase=s.phase,step=s.step,completed=s.completed,skipped=s.skipped,incomplete=s.incomplete,interruptions=s.interruptions,index=s.index,total=s.total,preparationMs=s.preparationMs,progressMs=s.stepMs,targetIndex=s.targetIndex)}}}
 private fun saveIfTerminal(){if(session?.phase in listOf(Phase.DONE,Phase.EXITED)){saveSession();mutable.update{it.copy(page=Page.SUMMARY)}}}
 private fun saveSession(){val s=session?:return;if(sessionId in savedIds||sessionId in savingIds)return
  val row=pending.getOrPut(sessionId){Record(sessionId,started,System.currentTimeMillis(),if(s.activities.size>1)"FULL" else s.activities.first().name,s.outcome,s.attempts,s.completed,s.incomplete,s.skipped,s.interruptions,s.activeMs,s.interruptedMs)}
  enqueueSave(row)
 }
 private fun enqueueSave(row:Record){if(row.id in savedIds||!savingIds.add(row.id))return
  mutable.update{it.copy(savePending=true)}
  io(onFailure={savingIds.remove(row.id);mutable.update{it.copy(savePending=true)}},onSuccess={savingIds.remove(row.id);savedIds.add(row.id);pending.remove(row.id);mutable.update{it.copy(savePending=pending.isNotEmpty(),error=if(it.error?.startsWith("Local operation failed:")==true)null else it.error)}}){store.save(row);val rows=store.records();mutable.update{it.copy(records=rows)}}
 }
}
