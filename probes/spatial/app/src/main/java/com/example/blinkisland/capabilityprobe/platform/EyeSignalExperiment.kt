package com.example.blinkisland.capabilityprobe.platform

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blinkisland.capabilityprobe.ProbeState

/** Offline cues label instructed conditions, NOT observed ground truth or training success. */
class EyeSignalExperiment(context:Context) {
 private val app=context.applicationContext
 private val handler=Handler(Looper.getMainLooper())
 private var voice:MediaPlayer?=null
 private var running=false
 private fun play(id:String):Boolean=try{
  voice?.release();voice=null
  app.assets.openFd("probe_voice/$id.wav").use{fd->
   voice=MediaPlayer().apply{setDataSource(fd.fileDescriptor,fd.startOffset,fd.length);prepare();start()}
  }
  Log.i(LaunchActivity.TAG,"probe_voice_started=$id offline=true")
  true
 }catch(e:Exception){Log.e(LaunchActivity.TAG,"probe_voice_failed=$id cause=${e.javaClass.simpleName}");false}
 fun start():Boolean {
  if(running)return false
  if(!play("intro")){ProbeState.experiment.value="Offline voice unavailable; experiment not started";return false}
  running=true;ProbeState.auditRequested.value=true;ProbeState.experiment.value="warmup"
  Log.i(LaunchActivity.TAG,"eye_experiment start; instructed_conditions_only=true")
  var at=12000L
  repeat(3){cycle->
   cue(at,"open_${cycle+1}","open");at+=8000
   cue(at,"closed_${cycle+1}","closed");at+=8000
   cue(at,"reopen_${cycle+1}","open");at+=6000
  }
  cue(at,"open_look_sides","sides");at+=10000
  handler.postDelayed({ProbeState.experiment.value="finished_pending_user_confirmation";Log.i(LaunchActivity.TAG,"eye_experiment finished; wearer_confirmation_required=true");play("done");running=false},at)
  return true
 }
 private fun cue(at:Long,label:String,asset:String){handler.postDelayed({if(running){ProbeState.experiment.value=label;Log.i(LaunchActivity.TAG,"eye_experiment instructed_condition=$label; not_ground_truth=true");if(!play(asset)){cancel();ProbeState.experiment.value="Offline cue failed; experiment cancelled"}}},at)}
 fun cancel(){handler.removeCallbacksAndMessages(null);voice?.release();voice=null;if(running){Log.i(LaunchActivity.TAG,"eye_experiment cancelled");ProbeState.experiment.value="cancelled"};running=false;ProbeState.auditRequested.value=false}
 fun close()=cancel()
}
