package com.example.blinkblinkisland.dev.ui.island.components
import kotlin.math.roundToInt

/** Attachment windows are bounded by the host display frame, even when their View measures larger. */
data class PanelRaster(val density:Float,val entityScale:Float,val widthPx:Int,val heightPx:Int){
 companion object{
  fun forViewport(hostDensity:Float,availableWidthPx:Int,availableHeightPx:Int):PanelRaster{
   require(hostDensity.isFinite()&&hostDensity>0&&availableWidthPx>32&&availableHeightPx>32)
   val density=minOf(hostDensity,(availableWidthPx-32)/1100f,(availableHeightPx-32)/1120f)
   return PanelRaster(density,hostDensity/density,(1100*density).roundToInt(),(1120*density).roundToInt())
  }
 }
}
