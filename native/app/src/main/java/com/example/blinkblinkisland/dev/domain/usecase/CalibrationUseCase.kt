package com.example.blinkblinkisland.dev.domain.usecase
import com.example.blinkblinkisland.dev.domain.model.Observation
class CalibrationUseCase {
 var index=0;private set
 var heldMs=0L;private set
 val complete get()=index>=5
 private var last:Long?=null
 fun observe(o:Observation){
  if(complete)return
  val dt=last?.let{o.atMs-it}?:0;last=o.atMs
  if(!o.quality||!o.headStable||!o.targetHit||!o.targetReady||dt !in 0..250){heldMs=0;return}
  heldMs+=dt
  if(heldMs>=800){index++;heldMs=0;last=null}
 }
}
