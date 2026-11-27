package com.example.blinkblinkisland.dev.domain.usecase

import com.example.blinkblinkisland.dev.domain.model.Record
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant

data class PracticeDay(val date:LocalDate,val sessions:Int,val activeMs:Long)
data class WeeklyStats(val days:List<PracticeDay>,val guidedSessions:Int,val gazeSessions:Int,val combinedSessions:Int){
 val sessions get()=days.sumOf{it.sessions}
 val activeMs get()=days.sumOf{it.activeMs}
 val today get()=days.last()
}
/** Local calendar days and recorded active practice, never wear time or measured benefit. */
object WeeklyStatsUseCase {
 fun summarize(records:List<Record>,today:LocalDate=LocalDate.now(),zone:ZoneId=ZoneId.systemDefault()):WeeklyStats {
  val first=today.minusDays(6)
  val rows=records.filter{it.activeMs>0}.map{it to Instant.ofEpochMilli(it.startedAt).atZone(zone).toLocalDate()}.filter{(_,day)->!day.isBefore(first)&&!day.isAfter(today)}
  val days=(0L..6L).map{offset->val day=first.plusDays(offset);val items=rows.filter{it.second==day};PracticeDay(day,items.size,items.sumOf{it.first.activeMs})}
  var guided=0;var gaze=0;var combined=0
  rows.forEach{(r,_)->when(r.kind){"BLINK","BLINK_GUIDED","QUICK_BLINK"->guided++;"GAZE","BUTTERFLY","QUICK_GAZE","QUICK_BUTTERFLY"->gaze++;else->combined++}}
  return WeeklyStats(days,guided,gaze,combined)
 }
}
