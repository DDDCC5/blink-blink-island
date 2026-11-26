package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import android.util.Log
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.ListItem
import com.pico.spatial.ui.design.ListItemDefaults
import androidx.compose.foundation.clickable
import com.pico.spatial.ui.design.ButtonDefaults
import com.example.blinkblinkisland.dev.ui.theme.IslandColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.example.blinkblinkisland.dev.R
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.HistoryPaginationUseCase
import com.example.blinkblinkisland.dev.ui.island.*
import com.example.blinkblinkisland.dev.ui.navigation.Page
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun PanelText(text:String,modifier:Modifier=Modifier){
 Text(text,modifier=modifier.panelFitProbe("text"),onTextLayout=panelTextProbe("text"),style=PicoTheme.typography.bodyLargeMultiline.copy(fontSize=32.sp,lineHeight=44.sp),color=IslandColors.ForestInk)
}
@Composable
internal fun PanelHeading(text:String){
 Text(text,modifier=Modifier.panelFitProbe("heading"),onTextLayout=panelTextProbe("heading"),style=PicoTheme.typography.headlineMedium.copy(fontSize=48.sp,lineHeight=58.sp),color=IslandColors.ForestInk)
}

@Composable
internal fun PanelNote(text:String,modifier:Modifier=Modifier){
 Text(text,modifier=modifier.panelFitProbe("note"),onTextLayout=panelTextProbe("note"),style=PicoTheme.typography.bodyLargeMultiline.copy(fontSize=26.sp,lineHeight=36.sp),color=IslandColors.MutedInk)
}
/** Native ListItem owns the hover surface; its modifier adds only the existing start action. */
@Composable
private fun ModeChoice(activity:Activity,zh:Boolean,event:(IslandEvent)->Unit){
 fun l(a:String,b:String)=if(zh)a else b
 val icon=if(activity==Activity.BUTTERFLY)R.drawable.mode_butterfly else R.drawable.mode_gaze
 val detail=if(activity==Activity.BUTTERFLY)l("用目光，跟随翩飞的蝴蝶","Gently follow the butterfly") else l("沿着橙色小球，走一段轨迹","Follow the orange target")
 val badge=if(activity==Activity.BUTTERFLY)l("60 秒","60 sec") else l("四组动作","4 patterns")
 ListItem(
  modifier=Modifier.fillMaxWidth().heightIn(min=120.dp).panelFitProbe("mode").clickable{Log.i("BlinkIsland","ui_action=${activityName(activity,zh)}");event(IslandEvent.Start(listOf(activity)))},
  colors=ListItemDefaults.listItemColors(backgroundColor=IslandColors.MintWash,headlineColor=IslandColors.ForestInk,supportingColor=IslandColors.MutedInk,trailingColor=IslandColors.DeepForest),
  padding=PaddingValues(horizontal=24.dp,vertical=16.dp),
  headlineContent={
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(24.dp)){
    Image(painterResource(icon),contentDescription=null,modifier=Modifier.size(76.dp))
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
     Text(activityName(activity,zh),onTextLayout=panelTextProbe("mode_title"),style=PicoTheme.typography.titleLarge.copy(fontSize=34.sp,lineHeight=44.sp))
     Text(detail,onTextLayout=panelTextProbe("mode_detail"),style=PicoTheme.typography.bodyLargeMultiline.copy(fontSize=26.sp,lineHeight=36.sp),color=IslandColors.MutedInk)
    }
    Text(badge,onTextLayout=panelTextProbe("mode_badge"),style=PicoTheme.typography.labelLarge.copy(fontSize=26.sp,lineHeight=36.sp),color=IslandColors.DeepForest)
   }
  }
 )
}

@Composable
internal fun ColumnScope.HomePanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 NatureWelcome(zh)
 IslandActionButton(l("开始完整休息","Start full session"),modifier=Modifier.fillMaxWidth(),primary=true){event(IslandEvent.Start(Activity.entries))}
 Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
  listOf(Activity.BUTTERFLY,Activity.GAZE).forEach{ModeChoice(it,zh,event)}
 }
 IslandActionButton(l("闭眼休息 · 10轮语音引导","Close & rest · 10 guided rounds"),modifier=Modifier.fillMaxWidth()){event(IslandEvent.Start(listOf(Activity.BLINK)))}
 Spacer(Modifier.weight(1f))
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
  IslandActionButton(l("记录与统计","History"),quiet=true){event(IslandEvent.Navigate(Page.HISTORY))}
  IslandActionButton(l("声音与设置","Settings"),quiet=true){event(IslandEvent.Navigate(Page.SETTINGS))}
  IslandActionButton(l("使用说明","Help"),quiet=true){event(IslandEvent.Navigate(Page.CAPABILITIES))}
 }
}

@Composable
internal fun ColumnScope.SummaryPanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 Row(Modifier.fillMaxWidth().height(236.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
  Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(12.dp)){
   PanelNote(l("这次休息的记录","YOUR MOMENT OF REST"))
   PanelHeading(if(s.phase==Phase.EXITED)l("这次休息，先到这里。","A pause, for now.") else l("这段小憩，已被记录。","Your little pause has a place."))
   PanelNote(l("每一次停下来，都值得被记录。","Every little pause deserves a place."))
  }
  NatureIsland(Modifier.width(360.dp).height(220.dp))
 }
 Row(Modifier.fillMaxWidth().height(170.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){
  listOf(l("完成","Complete") to s.completed,l("未完成","Incomplete") to s.incomplete,l("跳过","Skipped") to s.skipped,l("中断","Pauses") to s.interruptions).forEach{(label,value)->
   Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
    Text("$value",modifier=Modifier.panelFitProbe("statistic"),style=PicoTheme.typography.headlineLarge.copy(fontSize=72.sp,lineHeight=88.sp),color=IslandColors.ForestInk)
    PanelNote(label)
   }
  }
 }
 Spacer(Modifier.height(32.dp))
 PanelText(if(s.savePending)l("正在保存这次休息。","Saving your session.") else l("记录已保存在这台设备上。","Your record is saved on this device."))
 PanelNote(if(s.guidedBlink)l("闭眼休息按语音流程记录，不检测眼睛开合。","Close & rest records the guided sequence; eye closure is not measured.") else l("只留下一份记录，把轻松留给自己。","A record stays here. The moment is yours."))
 Spacer(Modifier.weight(1f))
 IslandActionButton(l("查看本地记录","View local history"),modifier=Modifier.fillMaxWidth(),primary=true){event(IslandEvent.Navigate(Page.HISTORY))}
 IslandActionButton(l("返回选择","Back to activities"),quiet=true){event(IslandEvent.Navigate(Page.HOME))}
}

@Composable
internal fun HistoryPanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 var requested by remember{mutableIntStateOf(0)}
 val page=HistoryPaginationUseCase.page(s.records,requested)
 PanelHeading(l("我的休息记录","Local activity history"))
 val rows=s.records
 PanelText(l("共${rows.size}次 · 完成${rows.count{it.outcome==Outcome.COMPLETED}} · 未完成${rows.count{it.outcome==Outcome.INCOMPLETE}} · 跳过${rows.count{it.outcome==Outcome.SKIPPED}} · 退出${rows.count{it.outcome==Outcome.EXITED}}","${rows.size} sessions · ${rows.count{it.outcome==Outcome.COMPLETED}} complete · ${rows.count{it.outcome==Outcome.INCOMPLETE}} incomplete · ${rows.count{it.outcome==Outcome.SKIPPED}} skipped · ${rows.count{it.outcome==Outcome.EXITED}} exited"))
 if(rows.isEmpty())PanelText(l("还没有记录。记录仅保存在本机，不保存原始眼动轨迹。","No records yet. Records stay on this device; raw gaze traces are not stored."))
 page.items.forEach{r->
  val kind=if(r.kind=="BLINK_GUIDED")l("闭眼休息（语音引导）","Close & rest (guided)") else if(r.kind=="FULL_GUIDED")l("完整休息（含语音引导）","Full session (includes guided rest)") else if(r.kind=="FULL")l("完整训练","Full session") else Activity.entries.firstOrNull{it.name==r.kind}?.let{activityName(it,zh)}?:r.kind
  PanelText("${SimpleDateFormat("MM-dd HH:mm",Locale.getDefault()).format(Date(r.startedAt))} · $kind · ${outcomeName(r.outcome,zh)}\n${l("完成","Completed")} ${r.completed} / ${l("未完成","Incomplete")} ${r.incomplete} / ${l("跳过","Skipped")} ${r.skipped}")
 }
 if(rows.isNotEmpty())Row(horizontalArrangement=Arrangement.spacedBy(20.dp),verticalAlignment=Alignment.CenterVertically){
  IslandActionButton(l("上一页","Previous"),page.index>0){requested=page.index-1}
  PanelText(l("第${page.index+1}/${page.pageCount}页","${page.index+1} / ${page.pageCount}"))
  IslandActionButton(l("下一页","Next"),page.index+1<page.pageCount){requested=page.index+1}
 }
 IslandActionButton(l("删除本机训练记录…","Delete local activity records…"),rows.isNotEmpty()){event(IslandEvent.Navigate(Page.DELETE_CONFIRM))}
}

@Composable
internal fun SettingsPanel(s:IslandUiState,event:(IslandEvent)->Unit,previewSound:()->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 PanelHeading(l("按你的节奏","Your own pace"))
 Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
  PanelText(l("提醒间隔：${s.settings.interval}分钟\n范围30–120分钟","Reminder: ${s.settings.interval} min\nRange: 30–120 min"),Modifier.weight(1f))
  IslandActionButton("−10",s.settings.interval>30){event(IslandEvent.ChangeSettings(s.settings.copy(interval=s.settings.interval-10)))}
  IslandActionButton("+10",s.settings.interval<120){event(IslandEvent.ChangeSettings(s.settings.copy(interval=s.settings.interval+10)))}
 }
 listOf("BGM" to s.settings.music,"SFX" to s.settings.effects,"Voice" to s.settings.voice).forEach{(label,value)->
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
   PanelText("${when(label){"BGM"->l("背景音乐","Music");"SFX"->l("提示音","Effects");else->l("语音","Voice")}} ${(value*100).toInt()}%",Modifier.weight(1f))
   for(delta in listOf(-.1f,.1f))IslandActionButton(if(delta<0)"−" else "+"){
    val v=(value+delta).coerceIn(0f,1f)
    event(IslandEvent.ChangeSettings(when(label){"BGM"->s.settings.copy(music=v);"SFX"->s.settings.copy(effects=v);else->s.settings.copy(voice=v)}))
   }
  }
 }
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  IslandActionButton(if(s.settings.muted)l("取消静音","Unmute") else l("全部静音","Mute all")){event(IslandEvent.ChangeSettings(s.settings.copy(muted=!s.settings.muted)))}
  IslandActionButton(l("试听当前语言引导","Preview voice"),!s.settings.muted){previewSound()}
 }
 PanelText(l("佩戴提醒暂未开放，当前不会自动提醒。","Wear reminders are not available yet; no automatic reminders are sent."))
}
