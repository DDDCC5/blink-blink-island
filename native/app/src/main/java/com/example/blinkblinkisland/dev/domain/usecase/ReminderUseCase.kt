package com.example.blinkblinkisland.dev.domain.usecase
/** Requires verified wear observations. No app lifecycle/proximity substitution. */
class ReminderUseCase(interval:Int=60) {
 val intervalMinutes=interval.coerceIn(30,120)
 var wornMs=0L;private set
 private var previous:Long?=null
 private var worn:Boolean?=null
 private var date=""
 private var silentDate:String?=null
 private var dueAt=intervalMinutes*60000L
 fun sample(now:Long,isWorn:Boolean?,localDate:String){
  val dt=previous?.let{(now-it).coerceAtLeast(0)}?:0
  if(worn==true&&isWorn==true)wornMs+=dt
  previous=now;worn=isWorn;date=localDate
 }
 fun due(training:Boolean)=!training&&worn==true&&silentDate!=date&&wornMs>=dueAt
 fun later(){dueAt=wornMs+600000}
 fun silenceToday(){silentDate=date}
 fun resetAfterBreak(){wornMs=0;dueAt=intervalMinutes*60000L}
}
