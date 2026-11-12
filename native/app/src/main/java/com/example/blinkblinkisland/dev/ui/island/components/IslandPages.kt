package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
 Text(text,modifier=modifier.panelFitProbe("text"),style=PicoTheme.typography.bodyLargeMultiline.copy(fontSize=32.sp,lineHeight=42.sp),color=PicoTheme.colorScheme.labelPrimary)
}
@Composable
internal fun PanelHeading(text:String){
 Text(text,modifier=Modifier.panelFitProbe("heading"),style=PicoTheme.typography.headlineMedium.copy(fontSize=38.sp,lineHeight=48.sp),color=PicoTheme.colorScheme.labelPrimary)
}

@Composable
internal fun HomePanel(s:IslandUiState,event:(IslandEvent)->Unit){
 val zh=s.settings.language=="zh"
 fun l(a:String,b:String)=if(zh)a else b
 Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(20.dp)){
  Image(painterResource(R.drawable.local_avatar),contentDescription=l("本机默认头像","Local default avatar"),modifier=Modifier.size(64.dp))
  Column{PanelHeading(l("让视线，慢下来。","A softer place to pause."));PanelText(l("岛上访客 · 本机默认资料","Island visitor · local default profile"))}
 }
 PanelText(l("完整训练：闭眼10轮，蝴蝶60秒，左右/上下各10轮，顺/逆圆周各5轮。","Full: close ×10; butterfly 60 s; left/right and up/down ×10; clockwise and counterclockwise ×5."))
 Row(horizontalArrangement=Arrangement.spacedBy(16.dp)){
  listOf(Activity.BLINK,Activity.BUTTERFLY,Activity.GAZE).forEach{a->IslandActionButton(activityName(a,zh)){event(IslandEvent.Start(listOf(a)))}}
 }
 PanelText(l("蝴蝶与眼球运动需要眼动。闭眼休息暂未开放。","Eye tracking is required. Close & rest is not available yet."))
 IslandActionButton(l("开始完整训练","Start full session")){event(IslandEvent.Start(Activity.entries))}
 Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
  IslandActionButton(l("本地记录与统计","Local history")){event(IslandEvent.Navigate(Page.HISTORY))}
  IslandActionButton(l("设置","Settings")){event(IslandEvent.Navigate(Page.SETTINGS))}
  IslandActionButton(l("权限与使用说明","Permissions & help")){event(IslandEvent.Navigate(Page.CAPABILITIES))}
 }
 PanelText(if(s.sceneLoaded)l("小岛已准备好","Your island is ready") else l("正在准备小岛…","Preparing your island…"))
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
