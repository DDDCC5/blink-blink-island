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
import com.pico.spatial.ui.foundation.content.SpatialView
import kotlinx.coroutines.*
import kotlin.math.sin

data class IslandSceneFrame(val target:TrainingVisual?,val head:TrackedPose?)

/** Existing original GLB, not an Editor bundle. Placement remains subject to comfort QA. */
@Composable
fun IslandScene(panel:AttachmentPanelComponent,motionAllowed:Boolean,panelScale:Float=1f,placementVersion:Int=0,onPlacementApplied:(Int)->Unit={},onScene:(Boolean,String?)->Unit,frame:(Entity,Long,TrackedPose?)->IslandSceneFrame){
 val root=remember{Entity()}
 val panelPlacement=remember{Entity()}
 val controls=remember{Entity().apply{components[AttachmentPanelComponent::class.java]=panel}}
 val allowed by rememberUpdatedState(motionAllowed)
 val report by rememberUpdatedState(onScene)
 val tick by rememberUpdatedState(frame)
 val requestedPlacementVersion by rememberUpdatedState(placementVersion)
 val placementApplied by rememberUpdatedState(onPlacementApplied)
 val mesh=remember{MeshResource.createSphere(.03f)}
 val material=remember{UnlitMaterial.create().apply{setBaseColor(Color4(.96f,.64f,.28f,1f))}}
 val scope=rememberCoroutineScope()
 var flap:Job? by remember{mutableStateOf(null)}
 SpatialView(initial={content,_->
  root.addChild(panelPlacement);panelPlacement.addChild(controls)
  controls.components[TransformComponent::class.java]?.apply{setEulerAngles(EulerAngles(0f,38.4f,0f));setScaleVector(Vector3(panelScale,panelScale,panelScale))}
  panelPlacement.components[TransformComponent::class.java]?.setPosition(Vector3(-.95f,1.6f,-1.2f))
  content.addEntity(root)
  try{
   val scenery=Entity();root.addChild(scenery)
   val model=Entity.loadSuspend("asset://models/island-scene.glb")
   model.components[TransformComponent::class.java]?.apply{setPosition(Vector3.ZERO);setScaleVector(Vector3(.45f,.45f,.45f))}
   scenery.addChild(model)
   // Measure the scaled existing asset in its placement owner before centering at eye level.
   val bounds=model.getVisualBounds(scenery)
   model.components[TransformComponent::class.java]?.setPosition(Vector3(-bounds.center.x,-bounds.center.y,-bounds.center.z))
   scenery.components[TransformComponent::class.java]?.setPosition(Vector3(1.55f,1.6f,-2.4f))
   Log.i("BlinkIsland","scenery_local_bounds=${model.getVisualBounds(scenery)}")
   val marker=Entity().apply{components.set(ModelComponent(mesh,material));enabled=false};root.addChild(marker)
   val flight=Entity();root.addChild(flight);flight.enabled=false
   val butterfly=Entity.loadSuspend("asset://models/butterfly.glb")
   butterfly.components[TransformComponent::class.java]?.apply{setScaleVector(Vector3(.12f,.12f,.12f));setEulerAngles(EulerAngles(90f,0f,0f))}
   flight.addChild(butterfly)
   val flightLeft=butterfly.findEntity("LeftWingPivot");val flightRight=butterfly.findEntity("RightWingPivot")
   Log.i("BlinkIsland","training_target_loaded bounds=${butterfly.getVisualBounds(flight)}")
   val sun=Entity().apply{components.set(DirectionalLightComponent(Color4(1f,.97f,.92f,1f),2000f,false));components[TransformComponent::class.java]?.setEulerAngles(EulerAngles(-40f,30f,0f))}
   root.addChild(sun)
   Log.i("BlinkIsland","scene_loaded glb=true bounds=${model.getVisualBounds(root)}")
   report(true,null)
   val left=model.findEntity("LeftWingPivot");val right=model.findEntity("RightWingPivot")
   Log.i("BlinkIsland","wing_nodes left=${left!=null} right=${right!=null}")
   val placement=ScenePlacementUseCase()
   var placed=false
   var fixedReference:TrackedPose?=null
   var appliedPlacementVersion=requestedPlacementVersion
   flap=scope.launch{while(isActive){
    if(appliedPlacementVersion!=requestedPlacementVersion){
     placement.reset();placed=false;fixedReference=null;appliedPlacementVersion=requestedPlacementVersion
    }
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
     placementApplied(appliedPlacementVersion)
     Log.i("BlinkIsland","layout_locked_once panel_left=true scenery_right=true eye_level=true scenery_bounds=${model.getVisualBounds(root)}")
    }
    marker.enabled=target!=null&&!target.butterfly;flight.enabled=target?.butterfly==true
    if(target!=null){
     val p=target.point;val r=target.reference.rotation
     val position=Vector3(p.x.toFloat(),p.y.toFloat(),p.z.toFloat())
     marker.components[TransformComponent::class.java]?.setPosition(position)
     flight.components[TransformComponent::class.java]?.apply{setPosition(position);setQuaternion(Quat(r.x.toFloat(),r.y.toFloat(),r.z.toFloat(),r.w.toFloat()))}
     // Same motion clock as the scored target: attitude and wing pose freeze on pause/loss.
     val pose=ButterflyFlightUseCase.pose(target.motionMs);val q=pose.rotation
     butterfly.components[TransformComponent::class.java]?.setQuaternion(Quat(q.x.toFloat(),q.y.toFloat(),q.z.toFloat(),q.w.toFloat()))
     flightLeft?.components?.get<TransformComponent>()?.setEulerAngles(EulerAngles(0f,0f,pose.leftWingDegrees.toFloat()))
     flightRight?.components?.get<TransformComponent>()?.setEulerAngles(EulerAngles(0f,0f,pose.rightWingDegrees.toFloat()))
    }
    if(allowed){val angle=(18+26*sin(now/1000.0*Math.PI)).toFloat();left?.components?.get<TransformComponent>()?.setEulerAngles(EulerAngles(0f,0f,angle));right?.components?.get<TransformComponent>()?.setEulerAngles(EulerAngles(0f,0f,-angle))}
    delay(33)
   }}
  }catch(e:Exception){if(e is CancellationException)throw e;Log.e("BlinkIsland","scene_load_failed",e);report(false,"3D load failed: ${e.javaClass.simpleName}")}
 })
 DisposableEffect(root){onDispose{flap?.cancel();root.destroy();mesh.close();material.close()}}
}
