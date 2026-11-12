package com.example.blinkblinkisland.dev.domain.usecase
import com.example.blinkblinkisland.dev.domain.model.Move
import kotlin.math.*

data class PathPoint(val point:Point3,val ready:Boolean)
/** App-owned trajectory in head-reference meters, not accommodation or a physical focus distance. */
object TargetPaths {
 private val rx=1.8*tan(10*PI/180)
 private val ry=1.8*tan(6*PI/180)
 val center=Point3(0.0,0.0,-1.8)
 fun destination(move:Move,index:Int):Point3=when(move){
  Move.HORIZONTAL->Point3(if(index%2==0)-rx else rx,0.0,-1.8)
  Move.VERTICAL->Point3(0.0,if(index%2==0)ry else -ry,-1.8)
  Move.CLOCKWISE,Move.COUNTERCLOCKWISE->{val a=index*PI/6*(if(move==Move.CLOCKWISE)1 else -1);Point3(rx*sin(a),ry*cos(a),-1.8)}
  else->center
 }
 fun pursuit(ms:Long):Point3{val a=ms.coerceAtLeast(0)*2*PI/16000;return Point3(rx*sin(a),ry*sin(2*a),-1.8)}
 fun gaze(move:Move,index:Int,ms:Long):PathPoint {
  val end=destination(move,index);val start=if(index==0)center else destination(move,index-1)
  val f=(ms/2000.0).coerceIn(0.0,1.0);val ease=f*f*(3-2*f)
  return PathPoint(Point3(start.x+(end.x-start.x)*ease,start.y+(end.y-start.y)*ease,-1.8),ms>=2000)
 }
 fun calibration(index:Int)=when(index){1->Point3(-.25,0.0,-1.8);2->Point3(.25,0.0,-1.8);3->Point3(0.0,.19,-1.8);4->Point3(0.0,-.19,-1.8);else->center}
 fun fromHead(offset:Point3,head:TrackedPose):Point3 {
  val r=head.rotation;val n=sqrt(r.x*r.x+r.y*r.y+r.z*r.z+r.w*r.w)
  if(!n.isFinite()||n<1e-12)return Point3(Double.NaN,Double.NaN,Double.NaN)
  val x=r.x/n;val y=r.y/n;val z=r.z/n;val w=r.w/n
  val tx=2*(y*offset.z-z*offset.y);val ty=2*(z*offset.x-x*offset.z);val tz=2*(x*offset.y-y*offset.x)
  return Point3(head.position.x+offset.x+w*tx+y*tz-z*ty,head.position.y+offset.y+w*ty+z*tx-x*tz,head.position.z+offset.z+w*tz+x*ty-y*tx)
 }
}
