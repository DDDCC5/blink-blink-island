package com.example.blinkblinkisland.dev.domain.usecase
import com.example.blinkblinkisland.dev.domain.model.*

class SessionUseCase(val activities:List<Activity>, capabilities:Capabilities) {
 var capabilities=capabilities;private set
 fun updateCapabilities(value:Capabilities){capabilities=value;if(phase in listOf(Phase.ACTIVE,Phase.CAPABILITY_BLOCKED,Phase.TRACKING_PAUSED))checkGate()}
 val steps=activities.flatMap { a-> when(a){
  Activity.BLINK->(1..10).map{Step(a,Move.BLINK,it,10)}
  Activity.BUTTERFLY->listOf(Step(a,Move.PURSUIT,1,1))
  Activity.GAZE->listOf(Move.HORIZONTAL,Move.VERTICAL,Move.CLOCKWISE,Move.COUNTERCLOCKWISE).flatMap{m->val count=if(m==Move.HORIZONTAL||m==Move.VERTICAL)10 else 5;(1..count).map{Step(a,m,it,count)}}
 }}
 var index=0; private set
 val total get()=steps.size
 val step get()=steps.getOrNull(index)
 var phase=Phase.ACTIVE;private set
 var completed=0;private set
 var incomplete=0;private set
 var skipped=0;private set
 var interruptions=0;private set
 var interruptedMs=0L;private set
 var activeMs=0L;private set
 var targetIndex=0;private set
 var stepMs=0L;private set
 var preparationRestartsPath=true;private set
 var preparationMs=0L;private set
 private var pausedPreparing=false
 private var last:Long?=null
 private var hold=0L
 private var targetHold=0L
 private var travelTotal=0L
 private var travelGood=0L
 private var misses=0L
 private var headUnstableMs=0L
 private var headRecovering=false
 private var attemptedThisStep=false
 private var closed=false
 private var interruptionAt:Long?=null
 val attempts get()=completed+incomplete+skipped+(if(attemptedThisStep)1 else 0)
 val outcome get()=when{phase==Phase.EXITED->Outcome.EXITED;completed==total&&total>0->Outcome.COMPLETED;incomplete>0||completed>0->Outcome.INCOMPLETE;else->Outcome.SKIPPED}
 init { require(activities.isNotEmpty());checkGate() }
 private fun checkGate(){phase=when{index>=total->Phase.DONE;step?.move==Move.BLINK&&!capabilities.eyelid->Phase.CAPABILITY_BLOCKED;step?.move!=Move.BLINK&&!capabilities.calibrated->Phase.CAPABILITY_BLOCKED;else->Phase.ACTIVE}}
 fun pause(){if(phase in listOf(Phase.ACTIVE,Phase.TRACKING_PAUSED,Phase.PREPARING)){pausedPreparing=phase==Phase.PREPARING;phase=Phase.USER_PAUSED;resetObservation()}}
 fun resume(){if(phase==Phase.USER_PAUSED){if(pausedPreparing)phase=Phase.PREPARING else {checkGate();if(phase==Phase.ACTIVE&&step?.move!=Move.BLINK){preparationRestartsPath=false;preparationMs=3000;phase=Phase.PREPARING}};resetObservation()}}
 private fun resetObservation(){last=null;hold=0;closed=false;targetHold=0;misses=0;headUnstableMs=0;headRecovering=false}
 fun retry(){if(phase==Phase.INCOMPLETE){stepMs=0;targetIndex=0;travelTotal=0;travelGood=0;resetObservation();checkGate();if(phase==Phase.ACTIVE){preparationRestartsPath=true;preparationMs=3000;phase=Phase.PREPARING}}}
 fun skip(){if(phase !in listOf(Phase.DONE,Phase.EXITED)){skipped++;advance();if(phase==Phase.ACTIVE&&step?.move!=Move.BLINK){preparationRestartsPath=true;preparationMs=3000;phase=Phase.PREPARING}}}
 fun skipActivity(){val a=step?.activity?:return;while(step?.activity==a&&phase !in listOf(Phase.DONE,Phase.EXITED))skip()}
 fun exit(){if(phase !in listOf(Phase.DONE,Phase.EXITED))phase=Phase.EXITED}
 private fun advance(){attemptedThisStep=false;index++;stepMs=0;targetIndex=0;travelTotal=0;travelGood=0;resetObservation();checkGate()}
 private fun fail(){attemptedThisStep=false;incomplete++;travelTotal=0;travelGood=0;phase=Phase.INCOMPLETE;resetObservation()}
 private fun pass(){completed++;advance()}
 fun observe(o:Observation){
  if(phase==Phase.PREPARING){
   val delta=last?.let{(o.atMs-it).takeIf{d->d in 0..250}?:0}?:0;last=o.atMs
   preparationMs=(preparationMs-delta).coerceAtLeast(0)
   if(preparationMs==0L){checkGate();resetObservation()}
   return
  }
  if(phase !in listOf(Phase.ACTIVE,Phase.TRACKING_PAUSED))return
  if(!o.quality){if(phase!=Phase.TRACKING_PAUSED){interruptions++;interruptionAt=o.atMs};phase=Phase.TRACKING_PAUSED;resetObservation();return}
  if(phase==Phase.TRACKING_PAUSED){interruptionAt?.let{interruptedMs+=(o.atMs-it).coerceAtLeast(0)};interruptionAt=null;phase=Phase.ACTIVE;last=null}
  var delta=last?.let{(o.atMs-it).coerceAtLeast(0)}?:0;last=o.atMs
  if(delta>250){interruptions++;hold=0;closed=false;targetHold=0;headUnstableMs=0;headRecovering=false;return}
  val current=step?:return
  attemptedThisStep=true
  if(current.move==Move.BLINK){
   if(o.eyesClosed==null){phase=Phase.CAPABILITY_BLOCKED;resetObservation();return}
   if(o.eyesClosed){if(closed)hold+=delta;closed=true;activeMs+=delta;stepMs=hold}
   else if(closed){if(hold>=3000)pass() else fail()}
   return
  }
  if(!o.headStable){
   targetHold=0
   if(!headRecovering){headRecovering=true;headUnstableMs=0}else headUnstableMs+=delta
   if(headUnstableMs>=500)fail()
   return
  }
  if(headRecovering){headRecovering=false;headUnstableMs=0;delta=0;misses=0;targetHold=0}
  if(!o.targetReady){
   targetHold=0;travelTotal+=delta
   if(o.targetHit){travelGood+=delta;activeMs+=delta;stepMs+=delta;misses=0}else{misses+=delta;if(misses>=1500)fail()}
   return
  }
  if(travelTotal>0){if(travelGood*4<travelTotal*3){fail();return};travelTotal=0;travelGood=0}
  if(o.targetHit){activeMs+=delta;stepMs+=delta;misses=0
   if(current.move==Move.PURSUIT){if(stepMs>=60000)pass()}
   else {targetHold+=delta;if(targetHold>=200){targetIndex++;targetHold=0;val count=if(current.move==Move.HORIZONTAL||current.move==Move.VERTICAL)3 else 13;if(targetIndex>=count)pass()}}
  } else {targetHold=0;misses+=delta;if(misses>=1500)fail()}
 }
}
