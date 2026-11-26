package com.example.blinkisland.capabilityprobe
import org.junit.Assert.*
import org.junit.Test
class EyeSignalWindowTest {
 @Test fun repeatedAndBackwardSourceTimestampsAreVisible(){val w=EyeSignalWindow();w.sample(100,0,true,false);w.sample(100,10,true,true);w.sample(90,20,false,true);val s=w.snapshot(30);assertTrue(s.contains("received=3"));assertTrue(s.contains("repeated_timestamp=1"));assertTrue(s.contains("reversed_timestamp=1"));assertTrue(s.contains("nonfinite_pose=1"));assertTrue(s.contains("since_arrival_ms=10"))}
 @Test fun noSamplesRemainMissingAcrossWindows(){val w=EyeSignalWindow();assertTrue(w.snapshot(100).contains("since_arrival_ms=-1"));w.sample(5,100,true,false);w.snapshot(110);val s=w.snapshot(1000);assertTrue(s.contains("received=0"));assertTrue(s.contains("since_arrival_ms=900"))}
 @Test fun windowResetDoesNotLoseContinuity(){val w=EyeSignalWindow();w.sample(5,100,true,false);w.snapshot(110);w.sample(5,900,true,true);val s=w.snapshot(910);assertTrue(s.contains("max_arrival_gap_ms=800"));assertTrue(s.contains("repeated_timestamp=1"));assertTrue(s.contains("received=1"))}
}
