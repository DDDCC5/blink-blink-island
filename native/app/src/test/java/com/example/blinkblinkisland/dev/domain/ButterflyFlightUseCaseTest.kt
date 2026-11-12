package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*
class ButterflyFlightUseCaseTest {
 @Test fun bodyHeadFollowsTheActualPathTangentInsteadOfStayingVertical(){
  for(t in 0L..15950L step 50){
   val pose=ButterflyFlightUseCase.pose(t);val q=pose.rotation
   val head=TargetPaths.fromHead(Point3(0.0,0.0,-1.0),TrackedPose(Point3(0.0,0.0,0.0),q,0))
   val a=TargetPaths.pursuit(t);val b=TargetPaths.pursuit(t+1);val dx=b.x-a.x;val dy=b.y-a.y
   assertTrue((head.x*dx+head.y*dy)/(hypot(head.x,head.y)*hypot(dx,dy))>.98)
   assertEquals(1.0,q.x*q.x+q.y*q.y+q.z*q.z+q.w*q.w,1e-8)
   assertTrue(head.z<-.05);assertTrue(abs(pose.bankDegrees)<=18.0)
  }
 }
 @Test fun motionClockMakesPausedPoseRepeatAndWingsHaveOppositeHinges(){
  val a=ButterflyFlightUseCase.pose(1234);assertEquals(a,ButterflyFlightUseCase.pose(1234))
  var low=90.0;var high=-90.0
  for(t in 0L..1000L step 10){val p=ButterflyFlightUseCase.pose(t);assertEquals(-p.leftWingDegrees,p.rightWingDegrees,1e-8);low=min(low,p.leftWingDegrees);high=max(high,p.leftWingDegrees)}
  assertTrue(high-low>65);assertTrue(low>=-10);assertTrue(high<=80)
 }
}
