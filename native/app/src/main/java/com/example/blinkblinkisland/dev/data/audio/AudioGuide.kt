package com.example.blinkblinkisland.dev.data.audio
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.blinkblinkisland.dev.domain.model.Settings
/** Three independently controlled tracks. All players owned by the visible Stage. */
class AudioGuide(private val context:Context){
 private var settings=Settings()
 private var bgm:MediaPlayer?=null
 private var voice:MediaPlayer?=null
 private var narrationWaiter:CancellableContinuation<Boolean>?=null
 private var cue:MediaPlayer?=null
 private var holding:MediaPlayer?=null
 private val handler=Handler(Looper.getMainLooper())
 private fun player(path:String,loop:Boolean=false):MediaPlayer?=try{context.assets.openFd(path).use{fd->MediaPlayer().apply{setDataSource(fd.fileDescriptor,fd.startOffset,fd.length);isLooping=loop;prepare()}}}catch(e:Exception){Log.e("BlinkIsland","audio_load_failed path=$path",e);null}
 fun update(s:Settings){settings=s;if(s.muted)stopAll() else applyVolumes()}
 private fun applyVolumes(){val gain=if(settings.muted)0f else 1f;val duck=if(voice?.isPlaying==true).32f else 1f;bgm?.setVolume(settings.music*gain*duck,settings.music*gain*duck);voice?.setVolume(settings.voice*gain,settings.voice*gain);cue?.setVolume(settings.effects*gain,settings.effects*gain);holding?.setVolume(settings.effects*.15f*gain,settings.effects*.15f*gain)}
 fun ambient(){if(settings.muted)return;if(bgm==null)bgm=player("audio/bgm_island_loop_96s_review.wav",true);applyVolumes();if(bgm?.isPlaying==false)bgm?.start()}
 private fun stopVoice(){voice?.release();voice=null;val waiter=narrationWaiter;narrationWaiter=null;if(waiter?.isActive==true)waiter.resume(false)}
 fun speak(id:String){stopVoice();if(settings.muted||settings.voice<=0)return;voice=player("audio/voice/voice_${settings.language}_${id}.wav");voice?.setOnCompletionListener{voice?.release();voice=null;applyVolumes()};voice?.start();applyVolumes()}
 /** Guided phases advance only after the actual local narration completes. */
 suspend fun narrate(id:String):Boolean=suspendCancellableCoroutine{waiter->
  stopVoice()
  val p=player("audio/voice/voice_${settings.language}_${id}.wav")
  if(p==null){waiter.resume(false);return@suspendCancellableCoroutine}
  voice=p;narrationWaiter=waiter
  fun finish(ok:Boolean){
   if(voice===p){voice=null;narrationWaiter=null;p.release();applyVolumes()}
   if(waiter.isActive)waiter.resume(ok)
  }
  p.setOnCompletionListener{finish(true)}
  p.setOnErrorListener{_,_,_->finish(false);true}
  waiter.invokeOnCancellation{if(voice===p){voice=null;narrationWaiter=null;p.release();applyVolumes()}}
  // Muted narration retains its timing and visual guidance without producing sound.
  applyVolumes()
  runCatching{p.start()}.onFailure{finish(false)}
 }
 fun sound(id:String){cue?.release();cue=null;if(settings.muted||settings.effects<=0)return;cue=player("audio/$id.wav");applyVolumes();cue?.start()}
 fun hold(active:Boolean){handler.removeCallbacksAndMessages(null);if(active&&!settings.muted){if(holding==null)holding=player("audio/sfx_eyes_holding_loop.wav",true);applyVolumes();holding?.start()}else{val p=holding?:return;for(i in 1..10)handler.postDelayed({if(i==10){p.release();if(holding===p)holding=null} else {val v=settings.effects*.15f*(1-i/10f);runCatching{p.setVolume(v,v)}}},i*20L)}}
 fun stopAll(){handler.removeCallbacksAndMessages(null);stopVoice();listOf(bgm,cue,holding).forEach{runCatching{it?.release()}};bgm=null;voice=null;cue=null;holding=null}
 fun close()=stopAll()
}
