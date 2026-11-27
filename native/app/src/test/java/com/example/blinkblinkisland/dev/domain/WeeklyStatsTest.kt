package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*
class WeeklyStatsTest {
 private val today=LocalDate.of(2026,11,27)
 private val zone=ZoneId.of("Asia/Shanghai")
 private fun row(id:String,date:LocalDate,ms:Long=60000,kind:String="BLINK_GUIDED")=Record(id,date.atStartOfDay(zone).toInstant().toEpochMilli(),0,kind,Outcome.EXITED,1,0,0,0,0,ms,0)
 @Test fun exactlySevenDaysIncludeTodayAndExcludeOldFutureAndZeroTime(){val stats=WeeklyStatsUseCase.summarize(listOf(row("old",today.minusDays(7)),row("edge",today.minusDays(6)),row("today",today),row("future",today.plusDays(1)),row("zero",today,0)),today,zone);assertEquals(7,stats.days.size);assertEquals(2,stats.sessions);assertEquals(120000L,stats.activeMs);assertEquals(1,stats.today.sessions)}
 @Test fun dateBucketingUsesLocalCalendarAndNotUtc(){val at=Instant.parse("2026-11-26T16:10:00Z").toEpochMilli();val r=row("local",today).copy(startedAt=at);val stats=WeeklyStatsUseCase.summarize(listOf(r),today,zone);assertEquals(1,stats.today.sessions)}
 @Test fun separatesGuidedGazeAndCombinedWithoutInventingMixedRoundCounts(){val rows=listOf(row("a",today),row("b",today,kind="QUICK_GAZE"),row("c",today,kind="QUICK_GUIDED"));val stats=WeeklyStatsUseCase.summarize(rows,today,zone);assertEquals(1,stats.guidedSessions);assertEquals(1,stats.gazeSessions);assertEquals(1,stats.combinedSessions)}
 @Test fun emptyHistoryProducesSevenEmptyDays(){val stats=WeeklyStatsUseCase.summarize(emptyList(),today,zone);assertEquals(0L,stats.activeMs);assertEquals(7,stats.days.size)}
}
