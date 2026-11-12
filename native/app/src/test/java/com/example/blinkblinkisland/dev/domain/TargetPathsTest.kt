package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.Move
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
class TargetPathsTest {
 @Test fun horizontalAndVerticalFollowRequestedOrder(){assertTrue(TargetPaths.destination(Move.HORIZONTAL,0).x<0);assertTrue(TargetPaths.destination(Move.HORIZONTAL,1).x>0);assertEquals(TargetPaths.destination(Move.HORIZONTAL,0),TargetPaths.destination(Move.HORIZONTAL,2));assertTrue(TargetPaths.destination(Move.VERTICAL,0).y>0);assertTrue(TargetPaths.destination(Move.VERTICAL,1).y<0)}
 @Test fun circularPathsCloseAndHaveOppositeDirections(){val start=TargetPaths.destination(Move.CLOCKWISE,0);assertEquals(start.x,TargetPaths.destination(Move.CLOCKWISE,12).x,1e-8);assertEquals(start.y,TargetPaths.destination(Move.CLOCKWISE,12).y,1e-8);assertTrue(TargetPaths.destination(Move.CLOCKWISE,1).x>0);assertTrue(TargetPaths.destination(Move.COUNTERCLOCKWISE,1).x<0)}
 @Test fun pursuitMovesSmoothlyAndStaysAtFixedVirtualDepth(){val a=TargetPaths.pursuit(0);val b=TargetPaths.pursuit(4000);assertNotEquals(a,b);for(t in 0L..60000 step 100){val p=TargetPaths.pursuit(t);assertEquals(-1.8,p.z,1e-8);assertTrue(kotlin.math.abs(p.x)<.4);assertTrue(kotlin.math.abs(p.y)<.25)}}
 @Test fun movementCannotBeJudgedBeforeTargetArrives(){val a=TargetPaths.gaze(Move.HORIZONTAL,1,0);assertFalse(a.ready);assertTrue(a.point.x<0);val b=TargetPaths.gaze(Move.HORIZONTAL,1,2000);assertTrue(b.ready);assertTrue(b.point.x>0)}
}
