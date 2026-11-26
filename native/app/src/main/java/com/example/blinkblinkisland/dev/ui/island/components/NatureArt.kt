package com.example.blinkblinkisland.dev.ui.island.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.design.Text
import com.example.blinkblinkisland.dev.R
import com.example.blinkblinkisland.dev.ui.theme.IslandColors

/** Accepted watercolor vignette: decoration only, never a gaze target or replacement 3D scene. */
@Composable
internal fun NatureIsland(modifier:Modifier=Modifier){
 Image(painterResource(R.drawable.nature_island),contentDescription=null,modifier=modifier,contentScale=ContentScale.Fit)
}

@Composable
internal fun NatureWelcome(zh:Boolean){
 Row(Modifier.fillMaxWidth().height(240.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(24.dp)){
  Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(8.dp)){
   PanelNote(if(zh)"给眼睛的一段留白" else "A little room to pause")
   Text(if(zh)"在岛上\n歇一会儿。" else "Pause on\nthe island.",modifier=Modifier.panelFitProbe("hero_title"),onTextLayout=panelTextProbe("hero_title"),style=PicoTheme.typography.headlineLarge.copy(fontSize=56.sp,lineHeight=68.sp),color=IslandColors.ForestInk)
   PanelNote(if(zh)"跟随目光，让节奏慢下来。" else "Follow gently. Take your time.")
  }
  NatureIsland(Modifier.width(440.dp).height(220.dp))
 }
}
