package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pico.spatial.ui.design.LinearProgressIndicator
import com.pico.spatial.ui.design.LinearProgressDefaults
import com.example.blinkblinkisland.dev.ui.theme.IslandColors
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.WeeklyStatsUseCase
import com.example.blinkblinkisland.dev.ui.island.*
import com.example.blinkblinkisland.dev.ui.navigation.Page
import java.util.Locale

@Composable
internal fun ColumnScope.PreparePanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 PanelHeading(l("先选一个舒服的节奏","Make yourself comfortable"))
 PanelNote(if(s.pendingActivities.size>1)l("三项休息，一次轻松完成。","Three activities, one gentle break.") else activityName(s.pendingActivities.first(),zh))
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  IslandActionButton(l("短练习","Short"),primary=s.selectedPlan==SessionPlan.QUICK){event(IslandEvent.SelectPlan(SessionPlan.QUICK))}
  IslandActionButton(l("完整练习","Full"),primary=s.selectedPlan==SessionPlan.FULL){event(IslandEvent.SelectPlan(SessionPlan.FULL))}
 }
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  IslandActionButton(l("舒缓","Comfortable"),primary=s.settings.pace==Pace.COMFORTABLE){event(IslandEvent.SelectPace(Pace.COMFORTABLE))}
  IslandActionButton(l("标准","Standard"),primary=s.settings.pace==Pace.STANDARD){event(IslandEvent.SelectPace(Pace.STANDARD))}
 }
 PanelNote(if(s.settings.pace==Pace.COMFORTABLE)l("飞行和小球移动更慢，运动范围更小。","Slower movement and smaller paths.") else l("使用你熟悉的速度和运动范围。","The familiar speed and movement range."))
 val quick=s.selectedPlan==SessionPlan.QUICK
 if(Activity.BLINK in s.pendingActivities)PanelNote(l("闭眼休息：${if(quick)3 else 10}轮语音引导","Close & rest: ${if(quick)3 else 10} guided rounds"))
 if(Activity.BUTTERFLY in s.pendingActivities)PanelNote(l("蝴蝶漫游：${if(quick)30 else 60}秒有效跟随","Butterfly: ${if(quick)30 else 60} seconds of following"))
 if(Activity.GAZE in s.pendingActivities)PanelNote(if(quick)l("眼球运动：四组路径，各1轮","Eye movements: four paths, one round each") else l("眼球运动：左右/上下各10轮，顺逆圆各5轮","Eye movements: 10 horizontal/vertical, 5 each circle"))
 Spacer(Modifier.weight(1f))
 IslandActionButton(l("准备好了，开始休息","Begin my break"),modifier=Modifier.fillMaxWidth(),primary=true){event(IslandEvent.Begin)}
 IslandActionButton(l("返回选择","Back to activities"),quiet=true){event(IslandEvent.Navigate(Page.HOME))}
}

@Composable
internal fun ColumnScope.StatisticsPanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 val stats=WeeklyStatsUseCase.summarize(s.records)
 fun minutes(ms:Long)=String.format(Locale.ROOT,"%.1f",ms/60000.0)
 PanelHeading(l("把休息，留在日常。","Make room for little pauses."))
 PanelText(l("今天 ${stats.today.sessions}次 · ${minutes(stats.today.activeMs)}分钟","Today: ${stats.today.sessions} breaks · ${minutes(stats.today.activeMs)} min"))
 PanelNote(l("七天共 ${stats.sessions}次 · ${minutes(stats.activeMs)}分钟有效练习","7 days: ${stats.sessions} breaks · ${minutes(stats.activeMs)} active minutes"))
 val maxMs=maxOf(60000L,stats.days.maxOf{it.activeMs})
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp)){
  stats.days.forEach{day->
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(20.dp)){
    PanelNote("${day.date.monthValue}/${day.date.dayOfMonth}",Modifier.width(100.dp))
    LinearProgressIndicator(progress={(day.activeMs.toFloat()/maxMs).coerceIn(0f,1f)},colors=LinearProgressDefaults.linearProgressColors(indicatorColor=IslandColors.DeepForest,backgroundColor=IslandColors.MintWash),modifier=Modifier.weight(1f))
    PanelNote(l("${minutes(day.activeMs)}分","${minutes(day.activeMs)} min"),Modifier.width(150.dp))
   }
  }
 }
 PanelNote(l("语音引导 ${stats.guidedSessions}次 · 目光跟随 ${stats.gazeSessions}次 · 组合休息 ${stats.combinedSessions}次","Guided ${stats.guidedSessions} · Gaze ${stats.gazeSessions} · Combined ${stats.combinedSessions}"))
 PanelNote(l("按本地日期与有效练习时长统计，不代表佩戴时长或训练效果。","Local dates and active practice time; not wear time or measured benefit."))
 Spacer(Modifier.weight(1f))
 IslandActionButton(l("返回记录","Back to history"),quiet=true){event(IslandEvent.Navigate(Page.HISTORY))}
}
