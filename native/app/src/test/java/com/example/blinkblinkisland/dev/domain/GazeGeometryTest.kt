package com.example.blinkblinkisland.dev.domain

import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
import com.example.blinkblinkisland.dev.domain.model.GazeIssue

/** Hand-derived poses: identity faces -Z; Y quarter-turn faces -X. */
class GazeGeometryTest {
 @Test fun diagnosesHeadRotationSeparatelyFromPosition(){val h=TrackedPose(Point3(0.0,0.0,0.0),Rotation4(.0348994967025,0.0,0.0,.999390827019),1000);val e=TrackedPose(Point3(0.0,0.0,0.0),Rotation4(0.0,0.0,0.0,1.0),1000);val r=GazeGeometry.evaluate(e,h,e,Point3(0.0,0.0,-2.0),1000,GazeRules(3.0,3.0,.05,200,100));assertEquals(GazeIssue.HEAD_ROTATION,r.issue);assertEquals(1,r.headRotationBand);assertEquals(0,r.headShiftBand)}
 @Test fun diagnosesHeadPositionWithoutRawPoseLogging(){val b=TrackedPose(Point3(0.0,0.0,0.0),Rotation4(0.0,0.0,0.0,1.0),1000);val h=b.copy(position=Point3(.08,0.0,0.0));val r=GazeGeometry.evaluate(h,h,b,Point3(0.0,0.0,-2.0),1000,GazeRules(3.0,3.0,.05,200,100));assertEquals(GazeIssue.HEAD_POSITION,r.issue);assertEquals(0,r.headRotationBand);assertEquals(1,r.headShiftBand)}
 private val zero=Point3(0.0,0.0,0.0)
 private val identity=Rotation4(0.0,0.0,0.0,1.0)
 private val head=TrackedPose(zero,identity,1000)
 private val rules=GazeRules(3.0,10.0,.1,250,100)
 private fun evaluate(eye:TrackedPose=head,target:Point3=Point3(0.0,0.0,-2.0),currentHead:TrackedPose=head,now:Long=1000)=GazeGeometry.evaluate(eye,currentHead,head,target,now,rules)
 @Test fun looksAlongNegativeZAndRejectsBehind(){assertTrue(evaluate().targetHit);assertFalse(evaluate(target=Point3(0.0,0.0,2.0)).targetHit)}
 @Test fun rotatedAndTranslatedGazeUsesWorldCoordinates(){val eye=TrackedPose(Point3(2.0,1.0,0.0),Rotation4(0.0,.7071067811865476,0.0,.7071067811865476),1000);assertTrue(evaluate(eye,Point3(0.0,1.0,0.0)).targetHit);assertFalse(evaluate(eye).targetHit)}
 @Test fun nearTargetIsInsufficientOutsideAngularTolerance(){assertFalse(evaluate(target=Point3(.2,0.0,-2.0)).targetHit);assertTrue(evaluate(target=Point3(.05,0.0,-2.0)).targetHit)}
 @Test fun quaternionScaleAndSignDoNotChangePose(){assertTrue(evaluate(eye=head.copy(rotation=Rotation4(0.0,0.0,0.0,-2.0)),currentHead=head.copy(rotation=Rotation4(0.0,0.0,0.0,-1.0))).headStable);assertTrue(evaluate(eye=head.copy(rotation=Rotation4(0.0,0.0,0.0,2.0))).targetHit)}
 @Test fun headRotationOrTranslationPreventsSuccess(){val yaw=head.copy(rotation=Rotation4(0.0,.17364817766693033,0.0,.984807753012208));assertFalse(evaluate(currentHead=yaw).headStable);assertFalse(evaluate(currentHead=head.copy(position=Point3(.2,0.0,0.0))).headStable)}
 @Test fun staleFutureOrUnsynchronisedSamplesAreInvalid(){assertFalse(evaluate(now=1300).quality);assertFalse(evaluate(now=999).quality);assertFalse(evaluate(eye=head.copy(atMs=850)).quality)}
 @Test fun invalidNumbersCannotCountAsTargetHits(){for(eye in listOf(head.copy(rotation=Rotation4(0.0,0.0,0.0,0.0)),head.copy(position=Point3(Double.NaN,0.0,0.0)))){val r=evaluate(eye=eye);assertFalse(r.quality);assertFalse(r.targetHit);assertFalse(r.headStable)};assertFalse(evaluate(target=zero).quality)}
 @Test fun lookingAtTargetWhileMovingHeadDoesNotPass(){val r=evaluate(currentHead=head.copy(position=Point3(.2,0.0,0.0)));assertTrue(r.quality);assertFalse(r.targetHit)}
}
