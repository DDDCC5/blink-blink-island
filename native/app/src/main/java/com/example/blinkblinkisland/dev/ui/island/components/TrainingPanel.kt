package com.example.blinkblinkisland.dev.ui.island.components
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.ui.island.*

@Composable
fun TrainingPanel(s:IslandUiState,event:(IslandEvent)->Unit,permissions:()->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 @Composable fun message(a:String,b:String){PanelText(l(a,b))}
 @Composable fun action(a:String,b:String,enabled:Boolean=true,click:()->Unit){IslandActionButton(l(a,b),enabled,click)}
 val blink=s.step?.move==Move.BLINK
 PanelHeading(s.step?.activity?.let{activityName(it,zh)}?:l("这次休息","Your session"))
 s.step?.let{message("${moveName(it.move,true)} · 第${it.round}/${it.rounds}轮","${moveName(it.move,false)} · round ${it.round}/${it.rounds}")}
 when{
  blink&&s.phase==Phase.CAPABILITY_BLOCKED->{
   message("闭眼休息暂未开放，可以先体验其他训练。","Close & rest is not available yet. You can try the other activities.")
   action("跳过此项","Skip this activity"){event(IslandEvent.SkipActivity)}
  }
  !s.eyePermissionGranted->{
   message("开启眼动后开始","Enable eye tracking to begin")
   message("眼动用于跟随目标，你可以在接下来的提示中选择是否允许。","Eye tracking helps you follow the target. Choose whether to allow it in the next prompt.")
   action("开启眼动","Enable eye tracking"){permissions()}
  }
  s.phase==Phase.PREPARING->{
   Text("${(s.preparationMs+999)/1000}",modifier=Modifier.panelFitProbe("countdown"),style=PicoTheme.typography.headlineLarge.copy(fontSize=64.sp,lineHeight=76.sp),color=PicoTheme.colorScheme.labelPrimary)
   message("看向训练目标，轻轻回正头部，倒计时后开始。","Look at the training target and settle your head. Training begins after the countdown.")
  }
  s.phase==Phase.USER_PAUSED->message("已暂停，准备好后继续。","Paused. Continue when you are ready.")
  s.calibrating->{
   message("视线对齐 · ${s.calibrationIndex+1}/5","Gaze alignment · ${s.calibrationIndex+1}/5")
   message("头部自然保持稳定，用眼睛看中央橙点。","Keep your head comfortably steady and look at the orange target in front.")
   when{
    !s.trackingGood->message("调整好头显，稍等一下。","Adjust your headset and wait a moment.")
    !s.headStable->message("轻轻回正头部，或点重新对齐。","Return your head gently, or align again.")
    s.targetHit->message("看得很稳，继续保持…","Steady gaze. Keep looking…")
    else->message("看向橙点，保持片刻即可。","Look at the orange target and hold briefly.")
   }
  }
  s.phase==Phase.TRACKING_PAUSED->message("先休息一下。调整好佩戴后会继续，进度已保留。","Take a moment. Adjust your headset to continue; your progress is kept.")
  s.phase==Phase.INCOMPLETE->message("这轮没有跟上，准备好后再试一次。","This round was not completed. Try again when ready.")
  s.phase==Phase.ACTIVE->{
   if(!s.headStable)message("轻轻回正头部，目标和计时已暂停。","Return your head gently; the target and timer are paused.")
   if(s.step?.move==Move.PURSUIT){message("用眼睛轻轻跟随蝴蝶，可以自然眨眼。","Follow the butterfly gently with your eyes. Blink naturally.");message("已跟随 ${s.progressMs/1000} / 60秒","Followed ${s.progressMs/1000} / 60 seconds")}
   else message("跟随橙点走完路径，头部自然保持稳定。","Follow the orange target through the path, keeping your head comfortably steady.")
  }
 }
 if(!blink)message("已完成 ${s.completed}轮 · 跳过 ${s.skipped}轮","${s.completed} rounds completed · ${s.skipped} skipped")
 Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
  if(s.phase==Phase.INCOMPLETE)action("再试一次","Retry"){event(IslandEvent.Retry)}
  if(s.phase==Phase.USER_PAUSED)action("继续","Resume"){event(IslandEvent.Resume)}
  else if(s.phase==Phase.ACTIVE||s.phase==Phase.TRACKING_PAUSED||s.phase==Phase.PREPARING)action("暂停","Pause"){event(IslandEvent.Pause)}
  if(!blink)action("跳过本轮","Skip round"){event(IslandEvent.Skip)}
  action("结束休息","Finish"){event(IslandEvent.Exit)}
 }
 if(!blink&&s.eyePermissionGranted)action("重新对齐","Align again",s.phase!=Phase.USER_PAUSED){event(IslandEvent.Recalibrate)}
}
