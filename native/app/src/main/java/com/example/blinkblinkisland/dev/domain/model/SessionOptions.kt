package com.example.blinkblinkisland.dev.domain.model

enum class SessionPlan { QUICK, FULL }
enum class Pace(val speed:Double,val excursion:Double) { COMFORTABLE(.75,.8), STANDARD(1.0,1.0) }
