package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.blinkblinkisland.dev.ui.theme.IslandColors
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.*
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.ui.island.*
import com.example.blinkblinkisland.dev.ui.navigation.Page

@Composable
fun IslandContent(s:IslandUiState,event:(IslandEvent)->Unit,permissions:()->Unit,previewSound:()->Unit){
 var contentBounds:Rect? by remember{mutableStateOf(null)}
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 @Composable fun action(a:String,b:String,enabled:Boolean=true,click:()->Unit){IslandActionButton(l(a,b),enabled,click)}
 @Composable fun body(a:String,b:String){PanelText(l(a,b))}
 @Composable fun heading(a:String,b:String){PanelHeading(l(a,b))}
 // design-style: opaque-root Stage attachment has no system glass; DESIGN_SPEC cream content card
 Column(Modifier.width(1100.dp).height(1120.dp).background(IslandColors.Cream,RoundedCornerShape(28.dp)).padding(30.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(l("眨眨岛","Blink Blink Island"),style=PicoTheme.typography.titleLarge.copy(fontSize=32.sp,lineHeight=40.sp),color=PicoTheme.colorScheme.labelPrimary);if(s.page in listOf(Page.WELCOME,Page.HOME))action("English","中文",!s.busy){event(IslandEvent.ToggleLanguage)}}
  if(s.busy){body("正在读取本机数据…","Loading local data…");return@Column}
  if(s.error!=null||s.savePending)Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
   PanelText(if(s.error!=null)l("暂时无法完成操作，请稍后重试。","Unable to finish this action. Please try again.") else l("正在保存这次休息。","Saving your session."),Modifier.weight(1f))
   if(s.savePending&&s.error!=null)action("重试保存","Retry save"){event(IslandEvent.RetrySave)}
  }
  Column(Modifier.fillMaxWidth().weight(1f).onGloballyPositioned{contentBounds=it.boundsInRoot()},verticalArrangement=Arrangement.spacedBy(12.dp)){
  CompositionLocalProvider(LocalPanelArea provides contentBounds,LocalPanelPage provides s.page.name){
  when(s.page){
   Page.WELCOME->{heading("让眼睛休息一下","A gentle place to pause");body("先选择语言。训练需要眼动权限；提醒需要通知权限。你可以拒绝权限并查看本地记录。","Choose a language. Eye tracking and notifications need your permission; local records remain available if you decline.");body("如有疼痛、眩晕或不适，请停止并摘下头显。虚拟远近不代表真实调焦；本应用不测闭眼力度。","Stop and remove the headset if uncomfortable. Virtual depth is not real accommodation. Eyelid force is not measured.");action("了解并继续","Continue"){event(IslandEvent.AcceptWelcome)}}
   Page.HOME->HomePanel(s,event)
   Page.TRAINING->TrainingPanel(s,event,permissions)
   Page.SUMMARY->{heading("这次休息的记录","Session result");body("完成 ${s.completed} · 未完成 ${s.incomplete} · 跳过 ${s.skipped} · 中断 ${s.interruptions}","Completed ${s.completed} · incomplete ${s.incomplete} · skipped ${s.skipped} · interrupted ${s.interruptions}");body(if(s.savePending)"正在保存这次休息。" else "这次休息已保存在本机。",if(s.savePending)"Saving your session." else "Your session is saved on this device.");action("查看记录","View history"){event(IslandEvent.Navigate(Page.HISTORY))}}
   Page.HISTORY->HistoryPanel(s,event)
   Page.DELETE_CONFIRM->{heading("删除全部训练记录？","Delete all activity records?");body("确认后将删除本机全部训练记录；语言和音量设置保留。此操作无法从应用内撤销。","This deletes all local activity records and keeps language/audio settings. There is no in-app undo.");action("确认删除训练记录","Confirm deletion"){event(IslandEvent.DeleteRecords)};action("取消","Cancel"){event(IslandEvent.Navigate(Page.HISTORY))}}
   Page.SETTINGS->SettingsPanel(s,event,previewSound)
   Page.CAPABILITIES->{heading("权限与使用说明","Permissions & help");body("跟随蝴蝶和橙点需要开启眼动，你随时可以在系统设置中关闭。","Following the butterfly and orange targets requires eye tracking. You can turn it off in system settings at any time.");body(if(s.eyePermissionGranted)"眼动已开启。" else "眼动尚未开启。",if(s.eyePermissionGranted)"Eye tracking is enabled." else "Eye tracking is not enabled.");action("开启眼动","Enable eye tracking"){permissions()};body("闭眼休息和佩戴提醒暂未开放。训练记录保存在本机，你可以随时删除。","Close & rest and wear reminders are not available yet. Activity records stay on this device and can be deleted at any time.")}
  }
  }
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(20.dp),verticalAlignment=Alignment.CenterVertically){
   if(s.page !in listOf(Page.HOME,Page.WELCOME,Page.TRAINING,Page.DELETE_CONFIRM))action("返回首页","Home"){event(IslandEvent.Navigate(Page.HOME))}
   PanelText(l("不适请立即停止，并摘下头显休息。","Stop and remove the headset if you feel discomfort."),Modifier.weight(1f))
  }
 }
}

fun activityName(a:Activity,zh:Boolean)=when(a){Activity.BLINK->if(zh)"闭眼休息" else "Close & rest";Activity.BUTTERFLY->if(zh)"蝴蝶漫游" else "Butterfly pursuit";Activity.GAZE->if(zh)"眼球运动" else "Eye movements"}
fun moveName(m:Move,zh:Boolean)=when(m){Move.BLINK->if(zh)"完整闭眼3秒后睁眼" else "Close for 3 seconds, then reopen";Move.PURSUIT->if(zh)"60秒有效追视" else "60 seconds of evaluable pursuit";Move.HORIZONTAL->if(zh)"左→右→左" else "Left → right → left";Move.VERTICAL->if(zh)"上→下→上" else "Up → down → up";Move.CLOCKWISE->if(zh)"顺时针一圈" else "One clockwise circle";Move.COUNTERCLOCKWISE->if(zh)"逆时针一圈" else "One counterclockwise circle"}
fun outcomeName(o:Outcome,zh:Boolean)=when(o){Outcome.COMPLETED->if(zh)"完成" else "Completed";Outcome.INCOMPLETE->if(zh)"未完成" else "Incomplete";Outcome.SKIPPED->if(zh)"跳过" else "Skipped";Outcome.EXITED->if(zh)"退出" else "Exited"}
