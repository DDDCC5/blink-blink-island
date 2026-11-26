package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.blinkblinkisland.dev.R
import com.example.blinkblinkisland.dev.ui.theme.IslandColors
import com.pico.spatial.ui.design.LinearProgressIndicator
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
fun ColumnScope.TrainingPanel(s:IslandUiState,event:(IslandEvent)->Unit,permissions:()->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 val blink=s.step?.move==Move.BLINK
 val preparing=s.phase==Phase.PREPARING
 val paused=s.phase==Phase.USER_PAUSED||s.phase==Phase.TRACKING_PAUSED
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
  Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
   Image(painterResource(if(blink)R.drawable.mode_rest else if(s.step?.move==Move.PURSUIT)R.drawable.mode_butterfly else R.drawable.mode_gaze),null,Modifier.size(64.dp))
   Text(s.step?.activity?.let{activityName(it,zh)}?:l("这次休息","Your session"),style=PicoTheme.typography.titleLarge.copy(fontSize=34.sp,lineHeight=44.sp),color=IslandColors.ForestInk)
  }
  PanelNote(when{preparing->l("准备开始","Get ready");paused->l("已暂停","Paused");s.calibrating->l("等待目光","Waiting for gaze");s.phase==Phase.ACTIVE->if(blink)l("语音引导","Voice guidance") else l("跟随中","Following");else->""})
 }
 if(s.step!=null&&!s.calibrating&&!preparing&&s.phase!=Phase.ACTIVE)PanelNote(moveName(s.step.move,zh))
 when{
  !blink&&!s.eyePermissionGranted->{
   PanelText(l("让目光连接小岛。","Let your gaze find the island."))
   PanelNote(l("训练需要眼动权限。你可以在系统提示中选择是否允许。","This activity needs eye tracking. Choose whether to allow it in the system prompt."))
   IslandActionButton(l("开启眼动","Enable eye tracking"),primary=true){permissions()}
  }
  preparing->{
   if(!blink&&!s.trackingGood)PanelNote(l("调整好佩戴后，倒计时会继续。","Adjust your headset to continue the countdown."))
   Column(Modifier.fillMaxWidth().padding(vertical=20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
    Text("${(s.preparationMs+999)/1000}",modifier=Modifier.panelFitProbe("countdown"),onTextLayout=panelTextProbe("countdown"),style=PicoTheme.typography.headlineLarge.copy(fontSize=112.sp,lineHeight=124.sp),color=IslandColors.ForestInk)
    PanelText(if(blink)l("放松坐好，跟着语音休息。","Sit comfortably and follow the voice.") else l("看向目标，准备好就一起出发。","Look at the target. We begin in a moment."))
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
  blink&&s.phase==Phase.ACTIVE->{
   PanelHeading(when(s.blinkStage){
    BlinkGuideStage.CLOSE_PROMPT->l("轻轻闭上眼睛。","Gently close your eyes.")
    BlinkGuideStage.HOLD->l("放松，保持闭眼。","Relax with your eyes closed.")
    BlinkGuideStage.OPEN_PROMPT->l("慢慢睁开眼睛。","Slowly open your eyes.")
    BlinkGuideStage.REST->l("放松一下，稍后继续。","Take a moment before the next round.")
   })
   Column(Modifier.fillMaxWidth().height(248.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
    Text("${s.step?.round?:1} / 10",modifier=Modifier.panelFitProbe("guided_round"),onTextLayout=panelTextProbe("guided_round"),style=PicoTheme.typography.headlineLarge.copy(fontSize=112.sp,lineHeight=132.sp),color=IslandColors.ForestInk)
    PanelText(l("跟随语音 · 闭眼3秒，再睁眼","Follow the voice · close for 3 seconds, then open"))
    if(s.blinkStage==BlinkGuideStage.HOLD)PanelText("${((3000-s.progressMs).coerceAtLeast(0)+999)/1000}")
   }
   PanelNote(l("这是语音引导练习，请按自己的舒适节奏跟随。","A voice-guided practice. Follow at a comfortable pace."))
  }
  s.phase==Phase.ACTIVE->{
   PanelHeading(if(s.step?.move==Move.PURSUIT)l("让目光，轻轻跟随。","Let your gaze follow gently.") else l("沿着小球，缓缓移动目光。","Follow the orange target gently."))
   Column(Modifier.fillMaxWidth().height(248.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
    Text(if(s.step?.move==Move.PURSUIT)"${s.progressMs/1000}" else "${s.step?.round?:1}",modifier=Modifier.panelFitProbe("progress"),onTextLayout=panelTextProbe("progress"),style=PicoTheme.typography.headlineLarge.copy(fontSize=132.sp,lineHeight=152.sp),color=IslandColors.ForestInk)
    PanelText(if(s.step?.move==Move.PURSUIT)l("/ 60 秒有效追随","/ 60 seconds of following") else l("/ ${s.step?.rounds?:1} 轮 · ${moveName(s.step!!.move,zh)}","/ ${s.step?.rounds?:1} rounds · ${moveName(s.step!!.move,zh)}"))
    Spacer(Modifier.height(16.dp))
    // Real session observations only; gaze progress retains its existing waypoint acceptance.
    LinearProgressIndicator(progress={if(s.step?.move==Move.PURSUIT)(s.progressMs/60000f).coerceIn(0f,1f) else (s.targetIndex.toFloat()/(if(s.step?.move in listOf(Move.HORIZONTAL,Move.VERTICAL))3f else 13f)).coerceIn(0f,1f)},modifier=Modifier.width(240.dp))
   }
   Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
    PanelText(if(s.step?.move==Move.PURSUIT)l("中央的蝴蝶会带你慢慢移动。","Gently follow the butterfly in the center.") else if(s.trackingGood&&!s.targetHit)l("小球在等你，重新看向它就会继续。","The target is waiting. Look at it to continue.") else l("跟随中央橙色小球的轨迹。","Follow the orange target in the center."))
    PanelNote(l("可以自然转头，也可以轻轻眨眼。","Turn your head naturally, and blink as usual."))
   }
  }
 }
 if(!blink&&!s.calibrating)PanelNote(l("已完成 ${s.completed}轮  ·  已跳过 ${s.skipped}轮","${s.completed} rounds complete  ·  ${s.skipped} skipped"))
 Spacer(Modifier.weight(1f))
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  if(s.phase==Phase.INCOMPLETE)IslandActionButton(l("再试一次","Retry"),primary=true){event(IslandEvent.Retry)}
  if(s.phase==Phase.USER_PAUSED)IslandActionButton(l("继续","Resume"),primary=true){event(IslandEvent.Resume)}
  else if(s.phase in listOf(Phase.ACTIVE,Phase.TRACKING_PAUSED,Phase.PREPARING))IslandActionButton(l("暂停","Pause"),primary=true){event(IslandEvent.Pause)}
  if(!blink)IslandActionButton(l("跳过本轮","Skip round")){event(IslandEvent.Skip)}
 }
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  IslandActionButton(l("结束休息","Finish"),quiet=true){event(IslandEvent.Exit)}
  IslandActionButton(l("返回选择","Back to activities"),quiet=true){event(IslandEvent.Navigate(Page.HOME))}
 }
 if(!blink&&s.eyePermissionGranted)IslandActionButton(l("重新准备","Prepare again"),s.phase!=Phase.USER_PAUSED,quiet=true){event(IslandEvent.Recalibrate)}
}
