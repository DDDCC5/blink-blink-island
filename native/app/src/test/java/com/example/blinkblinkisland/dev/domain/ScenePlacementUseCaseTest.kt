package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*
class ScenePlacementUseCaseTest {
 private fun head(t:Long,x:Double=0.0)=TrackedPose(Point3(x,1.6,0.0),Rotation4(0.0,0.0,0.0,1.0),t)
 @Test fun layoutLocksOnceAndIgnoresHeadMotionAndNewTrainingReferences(){val p=ScenePlacementUseCase();val initial=p.resolve(head(100),100)!!;val later=p.resolve(head(200,1.0).copy(rotation=Rotation4(0.0,.5,0.0,.866)),200);assertEquals(initial,later);assertEquals(-.95,initial.panel.x,1e-8);assertEquals(-1.2,initial.panel.z,1e-8);assertEquals(1.6,initial.panel.y,1e-8);assertEquals(1.55,initial.island.x,1e-8)}
 @Test fun missingOrStaleHeadCannotLockPlacement(){val p=ScenePlacementUseCase();assertNull(p.resolve(null,0));assertNull(p.resolve(head(0),500));assertNotNull(p.resolve(head(550),550))}
 @Test fun lookingUpOrTiltingAtLaunchDoesNotTiltTheRoomLayout(){
  val yaw=60.0*PI/180;val pitch=25.0*PI/180;val roll=18.0*PI/180
  fun multiply(a:Rotation4,b:Rotation4)=Rotation4(a.w*b.x+a.x*b.w+a.y*b.z-a.z*b.y,a.w*b.y-a.x*b.z+a.y*b.w+a.z*b.x,a.w*b.z+a.x*b.y-a.y*b.x+a.z*b.w,a.w*b.w-a.x*b.x-a.y*b.y-a.z*b.z)
  val q=multiply(multiply(Rotation4(0.0,sin(yaw/2),0.0,cos(yaw/2)),Rotation4(sin(pitch/2),0.0,0.0,cos(pitch/2))),Rotation4(0.0,0.0,sin(roll/2),cos(roll/2)))
  val h=head(100).copy(rotation=q);val p=ScenePlacementUseCase().resolve(h,100)!!
  assertEquals(h.position.y,p.panel.y,1e-8);assertEquals(h.position.y,p.island.y,1e-8)
  assertEquals(0.0,p.reference.rotation.x,1e-8);assertEquals(0.0,p.reference.rotation.z,1e-8)
  val center=TargetPaths.fromHead(TargetPaths.center,p.reference)
  assertEquals(-1.8*sin(yaw),center.x,1e-8);assertEquals(-1.8*cos(yaw),center.z,1e-8)
  val up=TargetPaths.fromHead(Point3(0.0,1.0,0.0),p.reference)
  assertEquals(h.position.x,up.x,1e-8);assertEquals(h.position.y+1,up.y,1e-8);assertEquals(h.position.z,up.z,1e-8)
 }
 @Test fun nearVerticalLaunchGazeWaitsForAUsableHorizontalHeading(){
  val p=ScenePlacementUseCase();val vertical=head(100).copy(rotation=Rotation4(sin(PI/4),0.0,0.0,cos(PI/4)))
  assertNull(p.resolve(vertical,100));assertNotNull(p.resolve(head(150),150))
 }
}
