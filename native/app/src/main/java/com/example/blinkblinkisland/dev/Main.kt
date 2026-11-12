package com.example.blinkblinkisland.dev
import com.example.blinkblinkisland.dev.ui.island.IslandScreen
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultStage
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope
fun mainApp(scope:SpatialAppScope)=with(scope){DefaultStage{PicoTheme{IslandScreen()}}}
