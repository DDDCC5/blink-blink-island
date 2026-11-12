package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.Observation
import com.example.blinkblinkisland.dev.domain.usecase.CalibrationUseCase
import org.junit.Assert.*
import org.junit.Test
class CalibrationUseCaseTest {
 @Test fun elapsedTimeOrBadGazeNeverCalibrates(){val c=CalibrationUseCase();for(t in 0L..10000 step 50)c.observe(Observation(t,true,true,targetHit=false));assertFalse(c.complete);assertEquals(0,c.index)}
 @Test fun requiresFiveSeparatelyHeldTargets(){val c=CalibrationUseCase();var t=0L;repeat(5){val index=c.index;while(c.index==index&&!c.complete){c.observe(Observation(t,true,true,targetHit=true));t+=50};if(it<4)assertFalse(c.complete)};assertTrue(c.complete)}
 @Test fun trackingLossDiscardsPartialHold(){val c=CalibrationUseCase();for(t in 0L..500 step 50)c.observe(Observation(t,true,true,targetHit=true));c.observe(Observation(550,false,false));for(t in 600L..1000 step 50)c.observe(Observation(t,true,true,targetHit=true));assertEquals(0,c.index)}
 @Test fun headMotionOrLargeTimeGapCannotQualify(){val c=CalibrationUseCase();c.observe(Observation(0,true,true,targetHit=true));c.observe(Observation(10000,true,true,targetHit=true));assertEquals(0,c.index);for(t in 10050L..12000 step 50)c.observe(Observation(t,true,false,targetHit=true));assertEquals(0,c.index)}
}
