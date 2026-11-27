package com.example.blinkblinkisland.dev.ui.island.components

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.*
import com.pico.spatial.core.ecs.AttachmentPanelComponent
import com.pico.spatial.core.ecs.DirectionalLightComponent
import com.pico.spatial.core.ecs.Entity
import com.pico.spatial.core.ecs.TransformComponent
import com.pico.spatial.core.ecs.ModelComponent
import com.pico.spatial.core.ecs.resource.MeshResource
import com.pico.spatial.core.ecs.resource.UnlitMaterial
import com.pico.spatial.core.math.Color4
import com.pico.spatial.core.math.EulerAngles
import com.pico.spatial.core.math.Vector3
import com.pico.spatial.core.math.Quat
import com.example.blinkblinkisland.dev.domain.usecase.TrainingVisual
import com.example.blinkblinkisland.dev.domain.usecase.TargetPaths
import com.example.blinkblinkisland.dev.domain.usecase.Point3
import com.example.blinkblinkisland.dev.domain.usecase.TrackedPose
import com.example.blinkblinkisland.dev.domain.usecase.ScenePlacementUseCase
import com.example.blinkblinkisland.dev.domain.usecase.ButterflyFlightUseCase
import com.example.blinkblinkisland.dev.domain.usecase.TargetJourney
import com.example.blinkblinkisland.dev.domain.usecase.TargetJourneyUseCase
import com.example.blinkblinkisland.dev.domain.usecase.JourneyStage
import com.example.blinkblinkisland.dev.domain.usecase.Rotation4
import com.example.blinkblinkisland.dev.domain.model.Activity
import kotlin.math.*
import com.pico.spatial.ui.foundation.content.SpatialView
import kotlinx.coroutines.*
import kotlin.math.sin
import kotlin.math.max

data class IslandSceneFrame(val target:TrainingVisual?,val head:TrackedPose?,val journey:TargetJourney=TargetJourney(),val active:Boolean=true)

/** Approved Tripo GLBs; retain fixed Stage placement and real target clocks. */
@Composable
fun IslandScene(panel:AttachmentPanelComponent,motionAllowed:Boolean,panelScale:Float=1f,onPlacementApplied:()->Unit={},onScene:(Boolean,String?)->Unit,frame:(Entity,Long,TrackedPose?)->IslandSceneFrame){
 val root=remember{Entity()}
 val panelPlacement=remember{Entity()}
 val controls=remember{Entity().apply{components[AttachmentPanelComponent::class.java]=panel}}
 val allowed by rememberUpdatedState(motionAllowed)
 val report by rememberUpdatedState(onScene)
 val tick by rememberUpdatedState(frame)
 val placementApplied by rememberUpdatedState(onPlacementApplied)
 val mesh=remember{MeshResource.createSphere(.03f)}
 val material=remember{UnlitMaterial.create().apply{setBaseColor(Color4(.96f,.64f,.28f,1f))}}
 val scope=rememberCoroutineScope()
 val wingbeats=remember{mutableListOf<SkeletalWingbeat>()}
 var flap:Job? by remember{mutableStateOf(null)}
 SpatialView(initial={content,_->
  root.addChild(panelPlacement);panelPlacement.addChild(controls)
  controls.components[TransformComponent::class.java]?.apply{setEulerAngles(EulerAngles(0f,38.4f,0f));setScaleVector(Vector3(panelScale,panelScale,panelScale))}
  panelPlacement.components[TransformComponent::class.java]?.setPosition(Vector3(-.95f,1.6f,-1.2f))
  content.addEntity(root)
  try{
   val scenery=Entity();root.addChild(scenery)
   val model=Entity.loadSuspend("asset://models/island-scene.glb")
   scenery.addChild(model)
   fitCenteredModel(model,scenery,1.56f)
   scenery.components[TransformComponent::class.java]?.setPosition(Vector3(1.55f,1.6f,-2.4f))
   Log.i("BlinkIsland","scenery_local_bounds=${model.getVisualBounds(scenery)}")
   val companionPlacement=Entity();scenery.addChild(companionPlacement)
   companionPlacement.components[TransformComponent::class.java]?.setPosition(Vector3(.20f,.25f,.35f))
   val companionAttitude=Entity();companionPlacement.addChild(companionAttitude)
   val companion=Entity.loadSuspend("asset://models/butterfly.glb");companionAttitude.addChild(companion)
   fitCenteredModel(companion,companionAttitude,.18f)
   companionAttitude.components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(52f,0f,-18f))
   val companionWings=SkeletalWingbeat.create(companion,"companion");wingbeats.add(companionWings)
   val marker=Entity().apply{components.set(ModelComponent(mesh,material));enabled=false};root.addChild(marker)
   val flight=Entity();root.addChild(flight)
   val attitude=Entity();flight.addChild(attitude)
   val butterfly=Entity.loadSuspend("asset://models/butterfly.glb");attitude.addChild(butterfly)
   fitCenteredModel(butterfly,attitude,.18f)
   val flightWings=SkeletalWingbeat.create(butterfly,"pursuit");wingbeats.add(flightWings)
   flight.enabled=false
   Log.i("BlinkIsland","training_target_loaded bounds=${butterfly.getVisualBounds(attitude)}")
   val sun=Entity().apply{components.set(DirectionalLightComponent(Color4(1f,.97f,.92f,1f),2000f,false));components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(-40f,30f,0f))}
   root.addChild(sun)
   Log.i("BlinkIsland","scene_loaded glb=true bounds=${model.getVisualBounds(root)}")
   report(true,null)
   Log.i("BlinkIsland","wing_nodes skeletal=true instances=2")
   val placement=ScenePlacementUseCase()
   var placed=false
   var fixedReference:TrackedPose?=null
   var lastAmbientAt=SystemClock.elapsedRealtime()
   var ambientMs=0L
   var lastStage=JourneyStage.ISLAND
   var lastActivity:Activity?=null
   var lastDisplayed:Point3?=null
   var arcStart:Point3?=null
   var arcEnd:Point3?=null
   var lastJourneyElapsed=-1L
   var lastRotation:Rotation4?=null
   flap=scope.launch{while(isActive){
    val now=SystemClock.elapsedRealtime();val scene=tick(root,now,fixedReference);val target=scene.target
    val fixed=placement.resolve(scene.head,now)
    if(!placed&&fixed!=null){
     val anchor=fixed.reference;fixedReference=anchor
     val r=anchor.rotation;val rotation=Quat(r.x.toFloat(),r.y.toFloat(),r.z.toFloat(),r.w.toFloat())
     val panelPoint=fixed.panel
     panelPlacement.components[TransformComponent::class.java]?.apply{setPosition(Vector3(panelPoint.x.toFloat(),panelPoint.y.toFloat(),panelPoint.z.toFloat()));setQuaternion(rotation)}
     val islandPoint=fixed.island
     scenery.components[TransformComponent::class.java]?.apply{setPosition(Vector3(islandPoint.x.toFloat(),islandPoint.y.toFloat(),islandPoint.z.toFloat()));setQuaternion(rotation)}
     placed=true
     placementApplied()
     Log.i("BlinkIsland","layout_locked_once panel_left=true scenery_right=true eye_level=true scenery_bounds=${model.getVisualBounds(root)}")
    }
    val journey=scene.journey;val stage=journey.stage
    val inTransit=stage in listOf(JourneyStage.ENTERING,JourneyStage.HANDOFF,JourneyStage.RETURNING)
    val participating=stage!=JourneyStage.ISLAND&&target!=null
    companionPlacement.enabled=!(participating&&journey.activity==Activity.BUTTERFLY)
    marker.enabled=participating&&journey.activity==Activity.GAZE
    flight.enabled=participating&&journey.activity==Activity.BUTTERFLY
    if(participating){
     val anchor=fixedReference?:target.reference
     val ball=journey.activity==Activity.GAZE
     // This is the actual companion anchor under the fixed island transform.
     val islandOffset=if(ball)Point3(1.50,.17,-2.12) else Point3(1.75,.25+.018*sin(ambientMs/1000.0*PI*.5),-2.05)
     val islandPoint=TargetPaths.fromHead(islandOffset,anchor)
     if(stage!=lastStage||journey.activity!=lastActivity){
      if(stage==JourneyStage.ENTERING){arcStart=islandPoint;arcEnd=target.point}
      if(stage in listOf(JourneyStage.RETURNING,JourneyStage.HANDOFF)){arcStart=lastDisplayed?:target.point;arcEnd=islandPoint}
      Log.i("BlinkIsland","target_journey stage=$stage activity=${journey.activity}")
     }
     val start=arcStart?:islandPoint;val end=arcEnd?:target.point
     val p=if(inTransit)TargetJourneyUseCase.arc(start,end,journey.progress,ball) else if(stage==JourneyStage.WAITING_END)lastDisplayed?:target.point else target.point
     val r=target.reference.rotation
     val position=Vector3(p.x.toFloat(),p.y.toFloat(),p.z.toFloat())
     marker.components[TransformComponent::class.java]?.setPosition(position)
     flight.components[TransformComponent::class.java]?.apply{setPosition(position);setQuaternion(Quat(r.x.toFloat(),r.y.toFloat(),r.z.toFloat(),r.w.toFloat()))}
     val pose=ButterflyFlightUseCase.pose(target.motionMs)
     val q=if(inTransit){
      // Express arc tangent in the same fixed reference used by the model owner.
      val ahead=TargetJourneyUseCase.arc(start,end,(journey.progress+.003).coerceAtMost(1.0),ball)
      val behind=TargetJourneyUseCase.arc(start,end,(journey.progress-.003).coerceAtLeast(0.0),ball)
      val delta=Point3(ahead.x-behind.x,ahead.y-behind.y,ahead.z-behind.z)
      val local=TargetPaths.fromHead(delta,TrackedPose(Point3(0.0,0.0,0.0),Rotation4(-r.x,-r.y,-r.z,r.w),0))
      val heading=atan2(local.y,local.x)-PI/2
      val travel=Rotation4(sin(PI*50/360)*cos(heading/2),sin(PI*50/360)*sin(heading/2),cos(PI*50/360)*sin(heading/2),cos(PI*50/360)*cos(heading/2))
      // Ease into the scored flight attitude near the central landing point.
      if(stage==JourneyStage.ENTERING)blendRotation(travel,pose.rotation,((journey.progress-.78)/.22).coerceIn(0.0,1.0))
      else blendRotation(lastRotation?:pose.rotation,travel,(journey.progress/.22).coerceIn(0.0,1.0))
     }else if(stage==JourneyStage.WAITING_END)lastRotation?:pose.rotation else pose.rotation
     attitude.components[TransformComponent::class.java]?.setQuaternion(Quat(q.x.toFloat(),q.y.toFloat(),q.z.toFloat(),q.w.toFloat()))
     val flying=if(inTransit)scene.active&&journey.elapsedMs!=lastJourneyElapsed else stage==JourneyStage.TRAINING&&allowed&&!target.frozen
     flightWings.update(if(inTransit)journey.elapsedMs else target.motionMs,flying&&!ball)
     lastDisplayed=p
     if(stage !in listOf(JourneyStage.RETURNING,JourneyStage.HANDOFF))lastRotation=q
    }else flightWings.hold()
    if(stage==JourneyStage.ISLAND&&lastStage!=stage){lastDisplayed=null;arcStart=null;arcEnd=null;lastRotation=null;Log.i("BlinkIsland","target_journey stage=ISLAND")}
    lastStage=stage;lastActivity=journey.activity;lastJourneyElapsed=journey.elapsedMs
    if(allowed&&!(participating&&journey.activity==Activity.BUTTERFLY))ambientMs+=(now-lastAmbientAt).coerceIn(0,100)
    lastAmbientAt=now
    companionWings.update(ambientMs,allowed)
    companionAttitude.components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(52f+2f*sin(ambientMs/2300.0).toFloat(),8f*sin(ambientMs/3100.0).toFloat(),-18f+4f*sin(ambientMs/2700.0).toFloat()))
    companionPlacement.components[TransformComponent::class.java]?.setPosition(Vector3(.20f,.25f+.018f*sin(ambientMs/1000.0*Math.PI*.5).toFloat(),.35f))
    delay(33)
   }}
  }catch(e:Exception){if(e is CancellationException)throw e;Log.e("BlinkIsland","scene_load_failed",e);report(false,"3D load failed: ${e.javaClass.simpleName}")}
 })
 DisposableEffect(root){onDispose{flap?.cancel();wingbeats.forEach{it.close()};wingbeats.clear();root.destroy();mesh.close();material.close()}}
}

/** Fit and center an imported asset within its independent attitude owner. */
private fun fitCenteredModel(model:Entity,owner:Entity,longestAxisMeters:Float){
 val transform=checkNotNull(model.components[TransformComponent::class.java])
 transform.setPosition(Vector3.ZERO);transform.setScaleVector(Vector3(1f,1f,1f))
 val original=model.getVisualBounds(owner);val h=original.halfExtent
 val longest=2f*max(h.x,max(h.y,h.z))
 check(longest.isFinite()&&longest>0f){"Imported model has no finite visual bounds"}
 val scale=longestAxisMeters/longest;transform.setScaleVector(Vector3(scale,scale,scale))
 val fitted=model.getVisualBounds(owner)
 transform.setPosition(Vector3(-fitted.center.x,-fitted.center.y,-fitted.center.z))
 Log.i("BlinkIsland","asset_fit extent=$longestAxisMeters scale=$scale bounds=${model.getVisualBounds(owner)}")
}

/** Shortest normalized quaternion blend; q and -q describe the same orientation. */
private fun blendRotation(a:Rotation4,b:Rotation4,progress:Double):Rotation4 {
 val t=progress*progress*(3-2*progress)
 val sign=if(a.x*b.x+a.y*b.y+a.z*b.z+a.w*b.w<0)-1 else 1
 val x=a.x*(1-t)+b.x*t*sign;val y=a.y*(1-t)+b.y*t*sign
 val z=a.z*(1-t)+b.z*t*sign;val w=a.w*(1-t)+b.w*t*sign
 val n=sqrt(x*x+y*y+z*z+w*w)
 return Rotation4(x/n,y/n,z/n,w/n)
}
