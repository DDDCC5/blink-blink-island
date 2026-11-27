package com.example.blinkblinkisland.dev.ui.island.components

import android.os.SystemClock
import android.util.Log
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.animation.AnimationPlaybackController
import kotlin.math.abs
import kotlin.math.min

/** SDK 6.1.9 public APIs; continuous renderer playback, synchronized to the scored clock. */
internal class SkeletalWingbeat private constructor(
 private val controllers:List<AnimationPlaybackController>,
 private val label:String,
):AutoCloseable {
 private var lastMotionMs=0L
 private var lastWallMs=0L
 private var moving=false
 private var evidenceSamples=0

 fun update(motionMs:Long,advance:Boolean){
  val now=SystemClock.elapsedRealtime()
  val delta=motionMs-lastMotionMs
  val wall=now-lastWallMs
  controllers.forEach { c->
   if(!c.valid)return@forEach
   val duration=c.getDuration()
   if(!duration.isFinite()||duration<=0f)return@forEach
   val desired=((motionMs.coerceAtLeast(0)/1000.0)%duration).toFloat()
   if(!advance){c.pause();c.setTime(desired)}
   else {
    // Seek only on transitions/discontinuities or meaningful drift. The renderer
    // interpolates every frame instead of a 30 Hz sequence of frozen poses.
    val actual=((c.getTime()%duration)+duration)%duration
    val distance=abs(actual-desired)
    if(!moving||delta<0||delta>250||min(distance,duration-distance)>.075f)c.setTime(desired)
    if(moving&&wall in 1..250&&delta>0)c.setSpeed((delta.toFloat()/wall).coerceIn(.25f,1.5f))
    if(!moving)c.setSpeed(1f)
    c.resume()
   }
   if(moving!=advance||evidenceSamples<3){
    if(evidenceSamples<8){Log.i("BlinkIsland","wingbeat_playback label=$label advancing=$advance actualSeconds=${c.getTime()} paused=${c.isPaused()}");evidenceSamples++}
   }
  }
  lastMotionMs=motionMs;lastWallMs=now;moving=advance
 }
 fun hold(){update(lastMotionMs,false)}
 override fun close(){controllers.forEach{if(it.valid)it.close()}}
 companion object {
  fun create(model:Entity,label:String):SkeletalWingbeat {
   val roots=model.findSkeletonRootEntity(true).toList().ifEmpty{model.findSkinnedMeshEntity(true).toList()}
   val controllers=roots.mapNotNull{owner->owner.getAnimationResources().firstOrNull()?.let{resource->
    owner.playAnimation(resource).also{c->check(c.valid){"$label animation unavailable"};c.pause();Log.i("BlinkIsland","skeletal_wingbeat label=$label valid=true duration=${c.getDuration()}")}
   }}
   check(controllers.isNotEmpty()){"$label GLB has no playable skeleton clip"}
   return SkeletalWingbeat(controllers,label).also{it.update(0,false)}
  }
 }
}
