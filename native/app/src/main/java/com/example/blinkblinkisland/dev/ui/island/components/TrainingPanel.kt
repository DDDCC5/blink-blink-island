package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.ui.island.*
import com.example.blinkblinkisland.dev.ui.navigation.Page

@Composable
fun TrainingPanel(s:IslandUiState,event:(IslandEvent)->Unit,permissions:()->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 val blink=s.step?.move==Move.BLINK
 val preparing=s.phase==Phase.PREPARING
 val paused=s.phase==Phase.USER_PAUSED||s.phase==Phase.TRACKING_PAUSED
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
  PanelHeading(s.step?.activity?.let{activityName(it,zh)}?:l("这次休息","Your session"))
  PanelNote(when{preparing->l("准备开始","Get ready");paused->l("已暂停","Paused");s.calibrating->l("等待目光","Waiting for gaze");s.phase==Phase.ACTIVE->l("跟随中","Following");else->""})
 }
 if(s.step!=null&&!s.calibrating&&!preparing)PanelNote(moveName(s.step.move,zh))
 when{
  blink&&s.phase==Phase.CAPABILITY_BLOCKED->{
   Spacer(Modifier.height(20.dp))
   PanelText(l("闭眼休息暂未开放。\n可以先跟随蝴蝶，或试试眼球运动。","Close & rest is not available yet.\nTry the butterfly or eye movements first."))
   IslandActionButton(l("跳过此项","Skip this activity"),primary=true){event(IslandEvent.SkipActivity)}
  }
  !s.eyePermissionGranted->{
   PanelText(l("让目光连接小岛。","Let your gaze find the island."))
   PanelNote(l("训练需要眼动权限。你可以在系统提示中选择是否允许。","This activity needs eye tracking. Choose whether to allow it in the system prompt."))
   IslandActionButton(l("开启眼动","Enable eye tracking"),primary=true){permissions()}
  }
  preparing->{
   if(!s.trackingGood)PanelNote(l("调整好佩戴后，倒计时会继续。","Adjust your headset to continue the countdown."))
   Column(Modifier.fillMaxWidth().padding(vertical=20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
    Text("${(s.preparationMs+999)/1000}",modifier=Modifier.panelFitProbe("countdown"),onTextLayout=panelTextProbe("countdown"),style=PicoTheme.typography.headlineLarge.copy(fontSize=112.sp,lineHeight=124.sp),color=PicoTheme.colorScheme.labelPrimary)
    PanelText(l("看向目标，准备好就一起出发。","Look at the target. We begin in a moment."))
   }
  }
  s.phase==Phase.USER_PAUSED->{
   PanelText(l("慢慢来，进度已为你保留。","Take your time. Your progress is kept."))
   PanelNote(l("准备好后，点继续。","Choose Resume when you are ready."))
  }
  s.calibrating->{
   PanelText(l("戴好头显，让目光找到目标。","Put on your headset and find the target with your gaze."))
   PanelNote(l("眼动准备好后，将倒计时开始。","The countdown begins when eye tracking is ready."))
  }
  s.phase==Phase.TRACKING_PAUSED->{
   PanelText(l("先休息一下。","Take a moment."))
   PanelNote(l("调整好佩戴后会继续，进度已经保留。","Adjust your headset to continue. Your progress is kept."))
  }
  s.phase==Phase.INCOMPLETE->{
   PanelText(l("刚才没有跟上，没关系。","Missed that one? That is okay."))
   PanelNote(l("可以再试一次，也可以跳过这一轮。","Try again, or move on to the next round."))
  }
  s.phase==Phase.ACTIVE->{
   if(s.step?.activity==Activity.GAZE&&s.trackingGood&&!s.targetHit)PanelNote(l("小球在等你，重新看向它就会继续。","The target is waiting. Look at it to continue."))
   Row(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.Bottom){
    Text(if(s.step?.move==Move.PURSUIT)"${s.progressMs/1000}" else "${s.step?.round?:1}",modifier=Modifier.panelFitProbe("progress"),onTextLayout=panelTextProbe("progress"),style=PicoTheme.typography.headlineLarge.copy(fontSize=96.sp,lineHeight=112.sp),color=PicoTheme.colorScheme.labelPrimary)
    PanelText(if(s.step?.move==Move.PURSUIT)l(" / 60 秒"," / 60 sec") else l(" / ${s.step?.rounds?:1} 轮"," / ${s.step?.rounds?:1} rounds"))
   }
   PanelText(if(s.step?.move==Move.PURSUIT)l("目光轻轻跟随蝴蝶。\n放松身体，可以自然眨眼。","Follow the butterfly gently with your gaze.\nRelax, and blink naturally.") else l("让目光跟随橙色小球，\n按它的节奏走完这段轨迹。","Follow the orange target with your gaze,\nand move through the path at its pace."))
  }
 }
 if(!blink&&!s.calibrating)PanelNote(l("已完成 ${s.completed}轮  ·  已跳过 ${s.skipped}轮","${s.completed} rounds complete  ·  ${s.skipped} skipped"))
 Spacer(Modifier.height(4.dp))
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  if(s.phase==Phase.INCOMPLETE)IslandActionButton(l("再试一次","Retry"),primary=true){event(IslandEvent.Retry)}
  if(s.phase==Phase.USER_PAUSED)IslandActionButton(l("继续","Resume"),primary=true){event(IslandEvent.Resume)}
  else if(s.phase in listOf(Phase.ACTIVE,Phase.TRACKING_PAUSED,Phase.PREPARING))IslandActionButton(l("暂停","Pause")){event(IslandEvent.Pause)}
  if(!blink)IslandActionButton(l("跳过本轮","Skip round")){event(IslandEvent.Skip)}
 }
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  IslandActionButton(l("结束休息","Finish"),quiet=true){event(IslandEvent.Exit)}
  IslandActionButton(l("返回选择","Back to activities"),quiet=true){event(IslandEvent.Navigate(Page.HOME))}
 }
 if(!blink&&s.eyePermissionGranted)IslandActionButton(l("重新准备","Prepare again"),s.phase!=Phase.USER_PAUSED,quiet=true){event(IslandEvent.Recalibrate)}
}
