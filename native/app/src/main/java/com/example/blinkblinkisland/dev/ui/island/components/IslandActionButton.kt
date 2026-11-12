package com.example.blinkblinkisland.dev.ui.island.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.Button
import com.pico.spatial.ui.design.ButtonDefaults
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.example.blinkblinkisland.dev.ui.theme.IslandColors

/** One native input/hover surface. Dark brand fill makes the system highlight visible. */
@Composable
fun IslandActionButton(label:String,enabled:Boolean=true,onClick:()->Unit){
 val input=remember{MutableInteractionSource()}
 LaunchedEffect(input,label){input.interactions.collect{event->when(event){
  is PressInteraction.Press->Log.i("BlinkIsland","ui_press=$label")
  is PressInteraction.Release->Log.i("BlinkIsland","ui_release=$label")
  is PressInteraction.Cancel->Log.i("BlinkIsland","ui_cancel=$label")
 }}}
  Button(onClick={Log.i("BlinkIsland","ui_action=$label");onClick()},enabled=enabled,
   interactionSource=input,
   modifier=Modifier.heightIn(min=84.dp).panelFitProbe("button"),
   colors=ButtonDefaults.buttonColors(containerColor=IslandColors.Forest,contentColor=IslandColors.Cream),
   contentPadding=PaddingValues(horizontal=24.dp,vertical=16.dp)){
   Text(label,style=PicoTheme.typography.labelLarge.copy(fontSize=32.sp,lineHeight=42.sp))
  }
}
