package com.example.blinkblinkisland.dev.domain.usecase
import com.example.blinkblinkisland.dev.data.repository.IslandRepository
import com.example.blinkblinkisland.dev.domain.model.*
class IslandUseCase(private val repository:IslandRepository){
 fun settings()=repository.settings()
 fun records()=repository.records()
 fun save(record:Record)=repository.save(record)
 fun update(settings:Settings)=repository.saveSettings(settings.copy(interval=settings.interval.coerceIn(30,120),music=settings.music.coerceIn(0f,1f),effects=settings.effects.coerceIn(0f,1f),voice=settings.voice.coerceIn(0f,1f)))
 fun deleteRecords()=repository.deleteRecords()
}
