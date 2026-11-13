package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import android.util.Log
import com.pico.spatial.ui.design.Button
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
 Text(text,modifier=modifier.panelFitProbe("text"),onTextLayout=panelTextProbe("text"),style=PicoTheme.typography.bodyLargeMultiline.copy(fontSize=32.sp,lineHeight=44.sp),color=PicoTheme.colorScheme.labelPrimary)
}
@Composable
internal fun PanelHeading(text:String){
 Text(text,modifier=Modifier.panelFitProbe("heading"),onTextLayout=panelTextProbe("heading"),style=PicoTheme.typography.headlineMedium.copy(fontSize=48.sp,lineHeight=58.sp),color=PicoTheme.colorScheme.labelPrimary)
}

@Composable
internal fun PanelNote(text:String,modifier:Modifier=Modifier){
 Text(text,modifier=modifier.panelFitProbe("note"),onTextLayout=panelTextProbe("note"),style=PicoTheme.typography.bodyLargeMultiline.copy(fontSize=26.sp,lineHeight=36.sp),color=PicoTheme.colorScheme.labelSecondary)
}
@Composable
private fun ModeChoice(activity:Activity,zh:Boolean,event:(IslandEvent)->Unit){
 fun l(a:String,b:String)=if(zh)a else b
 val icon=when(activity){Activity.BLINK->R.drawable.mode_rest;Activity.BUTTERFLY->R.drawable.mode_butterfly;Activity.GAZE->R.drawable.mode_gaze}
 val detail=when(activity){Activity.BLINK->l("闭眼三秒，再轻轻睁开","Close for three seconds, then gently reopen");Activity.BUTTERFLY->l("用目光，跟随翩飞的蝴蝶","Gently follow the butterfly with your gaze");Activity.GAZE->l("跟着橙色小球，走一段轨迹","Follow the orange target through its path")}
 val badge=when(activity){Activity.BLINK->l("暂未开放","Unavailable");Activity.BUTTERFLY->l("60 秒","60 sec");Activity.GAZE->l("四组动作","4 patterns")}
 Button(onClick={Log.i("BlinkIsland","ui_action=${activityName(activity,zh)}");event(IslandEvent.Start(listOf(activity)))},modifier=Modifier.fillMaxWidth().heightIn(min=112.dp).panelFitProbe("mode"),colors=ButtonDefaults.buttonColors(containerColor=IslandColors.Mint,contentColor=IslandColors.Forest),contentPadding=PaddingValues(horizontal=24.dp,vertical=14.dp)){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(20.dp)){
   Image(painterResource(icon),contentDescription=null,modifier=Modifier.size(72.dp))
   Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
    Text(activityName(activity,zh),onTextLayout=panelTextProbe("mode_title"),style=PicoTheme.typography.titleLarge.copy(fontSize=32.sp,lineHeight=42.sp))
    Text(detail,onTextLayout=panelTextProbe("mode_detail"),style=PicoTheme.typography.bodyLargeMultiline.copy(fontSize=25.sp,lineHeight=34.sp))
   }
   Text(badge,onTextLayout=panelTextProbe("mode_badge"),style=PicoTheme.typography.labelLarge.copy(fontSize=26.sp,lineHeight=36.sp))
  }
 }
}
@Composable
internal fun HomePanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 PanelHeading(l("让视线，慢下来。","A softer place to pause."))
 PanelNote(l("选一种休息方式，把这一刻留给自己。","Choose a gentle activity. Take a moment for yourself."))
 IslandActionButton(l("开始完整休息","Start full session"),modifier=Modifier.fillMaxWidth(),primary=true){event(IslandEvent.Start(Activity.entries))}
 Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
  listOf(Activity.BLINK,Activity.BUTTERFLY,Activity.GAZE).forEach{ModeChoice(it,zh,event)}
 }
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
  IslandActionButton(l("记录与统计","History"),quiet=true){event(IslandEvent.Navigate(Page.HISTORY))}
  IslandActionButton(l("声音与设置","Settings"),quiet=true){event(IslandEvent.Navigate(Page.SETTINGS))}
  IslandActionButton(l("使用说明","Help"),quiet=true){event(IslandEvent.Navigate(Page.CAPABILITIES))}
 }
}

@Composable
internal fun SummaryPanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 PanelHeading(l("这次休息的记录","Your moment of rest"))
 PanelNote(l("每一次停下来，都值得被记录。","Every little pause deserves a place."))
 Spacer(Modifier.height(20.dp))
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
  listOf(l("完成","Complete") to s.completed,l("未完成","Incomplete") to s.incomplete,l("跳过","Skipped") to s.skipped,l("中断","Pauses") to s.interruptions).forEach{(label,value)->
   Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
    Text("$value",modifier=Modifier.panelFitProbe("statistic"),style=PicoTheme.typography.headlineLarge.copy(fontSize=64.sp,lineHeight=76.sp),color=PicoTheme.colorScheme.labelPrimary)
    PanelNote(label)
   }
  }
 }
 Spacer(Modifier.height(20.dp))
 PanelText(if(s.savePending)l("正在保存这次休息。","Saving your session.") else l("这份记录已留在本机。","Your record is saved on this device."))
 IslandActionButton(l("查看本地记录","View local history"),primary=true){event(IslandEvent.Navigate(Page.HISTORY))}
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
  val kind=if(r.kind=="FULL")l("完整训练","Full session") else Activity.entries.firstOrNull{it.name==r.kind}?.let{activityName(it,zh)}?:r.kind
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
