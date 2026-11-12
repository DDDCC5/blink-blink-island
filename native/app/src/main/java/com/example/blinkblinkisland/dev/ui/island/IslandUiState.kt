package com.example.blinkblinkisland.dev.ui.island
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.ui.navigation.Page
import com.example.blinkblinkisland.dev.domain.usecase.TrackedPose

data class IslandUiState(val page:Page=Page.WELCOME,val settings:Settings=Settings(),val records:List<Record> = emptyList(),val phase:Phase?=null,val step:Step?=null,val completed:Int=0,val skipped:Int=0,val incomplete:Int=0,val interruptions:Int=0,val index:Int=0,val total:Int=0,val preparationMs:Long=0,val progressMs:Long=0,val targetIndex:Int=0,val sensorStatus:String="",val error:String?=null,val busy:Boolean=true,val sceneLoaded:Boolean=false,val route:String?=null,val savePending:Boolean=false,val calibrating:Boolean=false,val calibrationIndex:Int=0,val calibrationHold:Long=0,val trackingGood:Boolean=false,val headStable:Boolean=false,val targetHit:Boolean=false,val eyePermissionGranted:Boolean=false)
sealed interface IslandEvent {
 data class Navigate(val page:Page):IslandEvent
 data object AcceptWelcome:IslandEvent
 data object ToggleLanguage:IslandEvent
 data class Start(val activities:List<Activity>):IslandEvent
 data object Pause:IslandEvent
 data object Resume:IslandEvent
 data object Retry:IslandEvent
 data object Skip:IslandEvent
 data object SkipActivity:IslandEvent
 data object Exit:IslandEvent
 data object RetrySave:IslandEvent
 data object Recalibrate:IslandEvent
 data class EyePermission(val granted:Boolean):IslandEvent
 data object DeleteRecords:IslandEvent
 data class ChangeSettings(val settings:Settings):IslandEvent
 data class Sensor(val observation:Observation,val status:String):IslandEvent
 data class Frame(val eye:TrackedPose?,val head:TrackedPose?,val now:Long,val status:String,val sceneReference:TrackedPose?=null):IslandEvent
 data class Scene(val loaded:Boolean,val error:String?=null):IslandEvent
 data class Route(val action:String):IslandEvent
}
