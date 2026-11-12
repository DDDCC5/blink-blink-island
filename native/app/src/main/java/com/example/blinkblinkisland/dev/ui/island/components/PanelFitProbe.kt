package com.example.blinkblinkisland.dev.ui.island.components

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

internal val LocalPanelArea=compositionLocalOf<Rect?>{null}
internal val LocalPanelPage=compositionLocalOf{"unknown"}

/** Read-only layout evidence; never clips, scrolls or intercepts input. */
@Composable
internal fun Modifier.panelFitProbe(kind:String):Modifier {
 val area=LocalPanelArea.current
 val page=LocalPanelPage.current
 var reported:Boolean? by remember(page,kind){mutableStateOf(null)}
 return onGloballyPositioned{coordinates->
  if(area!=null){
   val b=coordinates.boundsInRoot()
   val fits=b.left>=area.left-1&&b.top>=area.top-1&&b.right<=area.right+1&&b.bottom<=area.bottom+1
   if(fits!=reported){reported=fits;Log.i("BlinkIsland","panel_fit page=$page kind=$kind ok=$fits")}
  }
 }
}
