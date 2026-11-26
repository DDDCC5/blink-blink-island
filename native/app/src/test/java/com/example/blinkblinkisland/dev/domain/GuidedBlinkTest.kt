package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.SessionUseCase
import org.junit.Assert.*
import org.junit.Test

class GuidedBlinkTest {
 @Test fun guidedRestStartsWithoutAnyTrackingPermission(){
  val s=SessionUseCase(listOf(Activity.BLINK),Capabilities())
  assertEquals(Phase.PREPARING,s.phase)
  for(t in 0L..5000 step 50)s.observe(Observation(t,false,false))
  assertEquals(Phase.PREPARING,s.phase)
  assertEquals(0,s.completed)
 }
}
