package com.example.blinkblinkisland.dev.domain.usecase

import com.example.blinkblinkisland.dev.domain.model.Activity
import kotlin.math.*

enum class JourneyStage { ISLAND, ENTERING, TRAINING, WAITING_END, HANDOFF, RETURNING }
data class TargetJourney(val stage:JourneyStage=JourneyStage.ISLAND,val activity:Activity?=null,val elapsedMs:Long=0){
 val progress:Double get()=(elapsedMs.toDouble()/if(stage in listOf(JourneyStage.RETURNING,JourneyStage.HANDOFF))2000 else 3000).coerceIn(0.0,1.0)
}

/** Presentation clock only; never contributes practice observations or saved outcomes. */
class TargetJourneyUseCase {
 var snapshot=TargetJourney();private set
 private var last:Long?=null
 private var pending:Activity?=null
 fun begin(activity:Activity){
  if(activity==Activity.BLINK||snapshot.activity==activity&&snapshot.stage!=JourneyStage.ISLAND)return
  if(snapshot.stage==JourneyStage.HANDOFF&&pending==activity)return
  if(snapshot.stage in listOf(JourneyStage.TRAINING,JourneyStage.ENTERING)&&snapshot.activity!=activity){pending=activity;snapshot=snapshot.copy(stage=JourneyStage.HANDOFF,elapsedMs=0);last=null;return}
  snapshot=TargetJourney(JourneyStage.ENTERING,activity);last=null
 }
 fun reset(){snapshot=TargetJourney();last=null;pending=null}
 fun waitForEnd(){if(snapshot.stage in listOf(JourneyStage.ENTERING,JourneyStage.TRAINING,JourneyStage.HANDOFF)){snapshot=snapshot.copy(stage=JourneyStage.WAITING_END);pending=null}}
 fun finish(){if(snapshot.stage !in listOf(JourneyStage.ISLAND,JourneyStage.RETURNING)){snapshot=snapshot.copy(stage=JourneyStage.RETURNING,elapsedMs=0);last=null;pending=null}}
 fun tick(now:Long,running:Boolean){
  if(!running){last=null;return}
  val delta=last?.let{(now-it).takeIf{d->d in 0..250}?:0}?:0;last=now
  val s=snapshot
  if(s.stage !in listOf(JourneyStage.ENTERING,JourneyStage.HANDOFF,JourneyStage.RETURNING))return
  val duration=if(s.stage==JourneyStage.ENTERING)3000L else 2000L
  val elapsed=(s.elapsedMs+delta).coerceAtMost(duration)
  snapshot=when{elapsed<duration->s.copy(elapsedMs=elapsed);s.stage==JourneyStage.ENTERING->s.copy(stage=JourneyStage.TRAINING,elapsedMs=duration);s.stage==JourneyStage.HANDOFF->TargetJourney(JourneyStage.ENTERING,pending).also{pending=null};else->TargetJourney()}
 }
 companion object {
  fun arc(start:Point3,end:Point3,progress:Double,ball:Boolean):Point3 {
   val p=progress.coerceIn(0.0,1.0);if(p<=0)return start;if(p>=1)return end
   val distance=sqrt((end.x-start.x).pow(2)+(end.y-start.y).pow(2)+(end.z-start.z).pow(2))
   val ease=p*p*(3-2*p);val lift=min(if(ball).25 else .14,distance*.15)*sin(PI*ease)
   return Point3(start.x+(end.x-start.x)*ease,start.y+(end.y-start.y)*ease+lift,start.z+(end.z-start.z)*ease)
  }
 }
}
