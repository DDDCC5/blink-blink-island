package com.example.blinkisland.capabilityprobe.platform
import android.app.Activity
import android.util.Log
/** Standard Khronos loader only; no face session and no sensitive sensor read. */
object OpenXrSupport {
 private external fun queryNative(activity:Activity,faceAllowed:Boolean):String
 fun query(activity:Activity,finished:()->Unit={}){
  Thread({
   try {System.loadLibrary("blink_openxr_support");Log.i(LaunchActivity.TAG,"face_permission_granted="+(activity.checkSelfPermission("com.picovr.permission.FACE_TRACKING")==android.content.pm.PackageManager.PERMISSION_GRANTED));Log.i(LaunchActivity.TAG,"openxr_support="+queryNative(activity,activity.checkSelfPermission("com.picovr.permission.FACE_TRACKING")==android.content.pm.PackageManager.PERMISSION_GRANTED))}
   catch(e:LinkageError){Log.w(LaunchActivity.TAG,"openxr_support unavailable=${e.javaClass.simpleName}")}
   catch(e:Exception){Log.w(LaunchActivity.TAG,"openxr_support unavailable=${e.javaClass.simpleName}")}
   finally {activity.runOnUiThread{finished()}}
  },"blink-openxr-support").start()
 }
}
