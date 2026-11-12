package com.example.blinkblinkisland.dev.domain.usecase
import kotlin.math.*

data class ButterflyFlightPose(val rotation:Rotation4,val bankDegrees:Double,val leftWingDegrees:Double,val rightWingDegrees:Double)
/** GLB head is local -Z; its wings span XZ and hinge about local Z. */
object ButterflyFlightUseCase {
 fun pose(ms:Long):ButterflyFlightPose {
  val t=((ms%16000)+16000)%16000
  val p=TargetPaths.pursuit(t);val before=TargetPaths.pursuit((t+15990)%16000);val after=TargetPaths.pursuit((t+10)%16000)
  val vx=(after.x-before.x)/.02;val vy=(after.y-before.y)/.02
  val ax=(after.x-2*p.x+before.x)/.0001;val ay=(after.y-2*p.y+before.y)/.0001
  val turn=(vx*ay-vy*ax)/(vx*vx+vy*vy)
  val bank=18*tanh(-turn*.6)
  val pitch=(70+4*sin(t*2*PI/16000))*PI/180
  val heading=atan2(vy,vx)-PI/2
  fun multiply(a:Rotation4,b:Rotation4)=Rotation4(a.w*b.x+a.x*b.w+a.y*b.z-a.z*b.y,a.w*b.y-a.x*b.z+a.y*b.w+a.z*b.x,a.w*b.z+a.x*b.y-a.y*b.x+a.z*b.w,a.w*b.w-a.x*b.x-a.y*b.y-a.z*b.z)
  val q=multiply(multiply(Rotation4(0.0,0.0,sin(heading/2),cos(heading/2)),Rotation4(0.0,sin(bank*PI/360),0.0,cos(bank*PI/360))),Rotation4(sin(pitch/2),0.0,0.0,cos(pitch/2)))
  val wing=35+40*sin(ms.coerceAtLeast(0)*2*PI*4.2/1000)
  return ButterflyFlightPose(q,bank,wing,-wing)
 }
}
