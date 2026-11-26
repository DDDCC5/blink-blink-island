package com.example.blinkisland.capabilityprobe.platform
import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.util.Log
/** Separate OpenXR runtime query entry. No Spatial container or borrowed session. */
class OpenXrProbeActivity:Activity(){
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  Log.i(LaunchActivity.TAG,"openxr_query_activity=start")
  if(intent.getBooleanExtra("request_face_permission",false)&&checkSelfPermission("com.picovr.permission.FACE_TRACKING")!=PackageManager.PERMISSION_GRANTED){
   requestPermissions(arrayOf("com.picovr.permission.FACE_TRACKING"),121)
  }else runQuery()
 }
 override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){
  super.onRequestPermissionsResult(requestCode,permissions,grantResults)
  if(requestCode==121){Log.i(LaunchActivity.TAG,"face_permission_result="+grantResults.joinToString());runQuery()}
 }
 private fun runQuery(){OpenXrSupport.query(this){Log.i(LaunchActivity.TAG,"openxr_query_activity=complete");finish()}}
}
