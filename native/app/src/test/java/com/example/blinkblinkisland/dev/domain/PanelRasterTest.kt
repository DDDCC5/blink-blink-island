package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.ui.island.components.PanelRaster
import org.junit.Assert.*
import org.junit.Test
class PanelRasterTest{
 @Test fun swanWindowFrameDoesNotCropBottom(){
  val raster=PanelRaster.forViewport(2f,2560,1440)
  assertTrue(raster.widthPx<=2560&&raster.heightPx<=1408)
  assertEquals(2200f,raster.widthPx*raster.entityScale,2f)
  assertEquals(2240f,raster.heightPx*raster.entityScale,2f)
 }
 @Test fun smallViewportAndHighDensityPreservePhysicalSize(){
  for(host in listOf(1f,1.5f,2f,3f,4f)){
   val raster=PanelRaster.forViewport(host,1280,720)
   assertTrue(raster.widthPx<=1248&&raster.heightPx<=688)
   assertEquals(1100*host,raster.widthPx*raster.entityScale,4f)
   assertEquals(1120*host,raster.heightPx*raster.entityScale,4f)
  }
 }
 @Test fun ampleViewportNeedsNoScaling(){assertEquals(1f,PanelRaster.forViewport(1.5f,2560,2560).entityScale,0f)}
}
