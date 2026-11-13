package com.example.blinkblinkisland.dev.domain.model

enum class Activity { BLINK, BUTTERFLY, GAZE }
enum class Phase { PREPARING, ACTIVE, USER_PAUSED, TRACKING_PAUSED, CAPABILITY_BLOCKED, INCOMPLETE, DONE, EXITED }
enum class Outcome { COMPLETED, INCOMPLETE, SKIPPED, EXITED }
enum class Move { BLINK, PURSUIT, HORIZONTAL, VERTICAL, CLOCKWISE, COUNTERCLOCKWISE }
data class Step(val activity:Activity,val move:Move,val round:Int,val rounds:Int)
data class Capabilities(val eyelid:Boolean=false,val gazeReady:Boolean=false)
enum class GazeIssue { NONE, NO_DATA, INVALID_SAMPLE, HEAD_ROTATION, HEAD_POSITION, GAZE_OFF_TARGET, EYE_PARTICIPATION }
/** Adapter-owned observations, not claimed PICO SDK fields. Null closure means unavailable. */
data class Observation(val atMs:Long,val quality:Boolean,val headStable:Boolean,val eyesClosed:Boolean?=null,val targetHit:Boolean=false,val targetReady:Boolean=true,val issue:GazeIssue=GazeIssue.NONE,val headRotationBand:Int=-1,val headShiftBand:Int=-1)
data class Record(val id:String,val startedAt:Long,val endedAt:Long,val kind:String,val outcome:Outcome,val attempted:Int,val completed:Int,val incomplete:Int,val skipped:Int,val interruptions:Int,val activeMs:Long,val interruptedMs:Long)
data class Settings(val language:String="zh",val interval:Int=60,val music:Float=.25f,val effects:Float=.4f,val voice:Float=.7f,val muted:Boolean=false,val onboarding:Boolean=false)
