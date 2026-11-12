package com.example.blinkblinkisland.dev.platform
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.pico.spatial.ui.platform.stub.SpatialLaunchActivity
class LaunchActivity:SpatialLaunchActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);route(intent);Log.i("BlinkIsland","launch sdk=6.1.9 native_app=true")}
 override fun onNewIntent(intent:Intent){super.onNewIntent(intent);setIntent(intent);route(intent)}
 private fun route(intent:Intent?){intent?.getStringExtra("island_route")?.takeIf{it in listOf("start","later","today")}?.let{AppRoute.current.value=it;Log.i("BlinkIsland","notification_route=$it")}}
}
