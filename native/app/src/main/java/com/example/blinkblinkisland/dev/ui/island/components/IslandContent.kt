package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.layout.*
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.blinkblinkisland.dev.ui.theme.IslandColors
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.blinkblinkisland.dev.R
import com.pico.spatial.ui.design.*
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.ui.island.*
import com.example.blinkblinkisland.dev.ui.navigation.Page

@Composable
fun IslandContent(s:IslandUiState,event:(IslandEvent)->Unit,permissions:()->Unit,previewSound:()->Unit){
 var contentBounds:Rect? by remember{mutableStateOf(null)}
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 @Composable fun action(a:String,b:String,enabled:Boolean=true,primary:Boolean=false,quiet:Boolean=false,click:()->Unit){IslandActionButton(l(a,b),enabled,primary=primary,quiet=quiet,onClick=click)}
 @Composable fun body(a:String,b:String){PanelText(l(a,b))}
 @Composable fun heading(a:String,b:String){PanelHeading(l(a,b))}
 val panelShape=remember{RoundedCornerShape(36.dp)}
 // Transparent inset keeps every rounded edge inside the attachment's raster boundary.
 Box(Modifier.width(1100.dp).height(1120.dp).onGloballyPositioned{Log.i("BlinkIsland","panel_root size=${it.size} bounds=${it.boundsInRoot()}")}.padding(8.dp)){
 // design-style: opaque-root Existing Stage attachment has no system glass; preserve accepted clipped opaque attachment, nature palette
 // design-style: design-surface paper-surface
 Column(Modifier.fillMaxSize().clip(panelShape).background(IslandColors.PaperWhite,panelShape).padding(32.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
  Row(Modifier.fillMaxWidth().height(80.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
   Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(16.dp),verticalAlignment=Alignment.CenterVertically){
    Image(painterResource(R.mipmap.ic_spatial_launcher),contentDescription=null,modifier=Modifier.size(48.dp))
    Column{
     Text(l("眨眨岛","Blink Blink Island"),style=PicoTheme.typography.titleLarge.copy(fontSize=34.sp,lineHeight=44.sp),color=IslandColors.ForestInk)
    }
   }
   if(s.page in listOf(Page.WELCOME,Page.HOME,Page.PREPARE))Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
    IslandActionButton(l("English","中文"),!s.busy,quiet=true){event(IslandEvent.ToggleLanguage)}
   }
   else IslandActionButton(l("返回","Back"),!s.busy,quiet=true){event(IslandEvent.Navigate(if(s.page in listOf(Page.DELETE_CONFIRM,Page.STATISTICS))Page.HISTORY else Page.HOME))}
  }
  if(s.busy){body("正在读取本机数据…","Loading local data…");return@Column}
  if(s.error!=null||s.savePending)Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
   PanelText(if(s.error!=null)l("暂时无法完成操作，请稍后重试。","Unable to finish this action. Please try again.") else l("正在保存这次休息。","Saving your session."),Modifier.weight(1f))
   if(s.savePending&&s.error!=null)action("重试保存","Retry save"){event(IslandEvent.RetrySave)}
  }
  Column(Modifier.fillMaxWidth().weight(1f).onGloballyPositioned{contentBounds=it.boundsInRoot()},verticalArrangement=Arrangement.spacedBy(16.dp)){
  CompositionLocalProvider(LocalPanelArea provides contentBounds,LocalPanelPage provides s.page.name){
  when(s.page){
   Page.WELCOME->{
    heading("给眼睛的一段留白","A little room to pause")
    NatureIsland(Modifier.width(600.dp).height(260.dp))
    body("闭眼休息跟随语音即可；蝴蝶和小球训练需要眼动权限。","Close & rest is voice-guided. Following activities need eye tracking.")
    PanelNote(l("记录只留在本机，语言可以在训练前切换。","Records stay on this device. Choose your language before training."))
    Spacer(Modifier.weight(1f))
    IslandActionButton(l("了解并继续","Continue"),primary=true,modifier=Modifier.fillMaxWidth()){event(IslandEvent.AcceptWelcome)}
   }
   Page.HOME->HomePanel(s,event)
   Page.PREPARE->PreparePanel(s,event)
   Page.STATISTICS->StatisticsPanel(s,event)
   Page.TRAINING->TrainingPanel(s,event,permissions)
   Page.SUMMARY->SummaryPanel(s,event)
   Page.HISTORY->HistoryPanel(s,event)
   Page.DELETE_CONFIRM->{heading("删除全部训练记录？","Delete all activity records?");body("确认后将删除本机全部训练记录；语言和音量设置保留。此操作无法从应用内撤销。","This deletes all local activity records and keeps language/audio settings. There is no in-app undo.");action("确认删除训练记录","Confirm deletion"){event(IslandEvent.DeleteRecords)};action("取消","Cancel"){event(IslandEvent.Navigate(Page.HISTORY))}}
   Page.SETTINGS->SettingsPanel(s,event,previewSound)
   Page.CAPABILITIES->{heading("权限与使用说明","Permissions & help");body("跟随蝴蝶和橙点需要开启眼动，你随时可以在系统设置中关闭。","Following the butterfly and orange targets requires eye tracking. You can turn it off in system settings at any time.");body(if(s.eyePermissionGranted)"眼动已开启。" else "眼动尚未开启。",if(s.eyePermissionGranted)"Eye tracking is enabled." else "Eye tracking is not enabled.");action("开启眼动","Enable eye tracking"){permissions()};body("闭眼休息跟随语音完成，不检测眼睛开合。佩戴提醒暂未开放。训练记录保存在本机，可随时删除。","Close & rest is voice-guided; eye closure is not measured. Wear reminders are not available yet. Records stay on this device and can be deleted.");PanelNote(l("调整视角请使用系统长按回正。","Use the system long-press recenter to adjust your view."))}
  }
  }
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(20.dp),verticalAlignment=Alignment.CenterVertically){
   PanelNote(l("感到不适时，请停下来，摘下头显休息。","If you feel discomfort, stop and remove the headset."),Modifier.weight(1f))
  }
 }
 }
}

fun activityName(a:Activity,zh:Boolean)=when(a){Activity.BLINK->if(zh)"闭眼休息" else "Close & rest";Activity.BUTTERFLY->if(zh)"蝴蝶漫游" else "Butterfly pursuit";Activity.GAZE->if(zh)"眼球运动" else "Eye movements"}
fun moveName(m:Move,zh:Boolean)=when(m){Move.BLINK->if(zh)"完整闭眼3秒后睁眼" else "Close for 3 seconds, then reopen";Move.PURSUIT->if(zh)"60秒有效追视" else "60 seconds of evaluable pursuit";Move.HORIZONTAL->if(zh)"左→右→左" else "Left → right → left";Move.VERTICAL->if(zh)"上→下→上" else "Up → down → up";Move.CLOCKWISE->if(zh)"顺时针一圈" else "One clockwise circle";Move.COUNTERCLOCKWISE->if(zh)"逆时针一圈" else "One counterclockwise circle"}
fun outcomeName(o:Outcome,zh:Boolean)=when(o){Outcome.COMPLETED->if(zh)"完成" else "Completed";Outcome.INCOMPLETE->if(zh)"未完成" else "Incomplete";Outcome.SKIPPED->if(zh)"跳过" else "Skipped";Outcome.EXITED->if(zh)"退出" else "Exited"}
