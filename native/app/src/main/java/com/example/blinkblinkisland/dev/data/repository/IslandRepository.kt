package com.example.blinkblinkisland.dev.data.repository
import com.example.blinkblinkisland.dev.domain.model.*
interface IslandRepository {
 fun records():List<Record>
 fun save(record:Record)
 fun deleteRecords()
 fun settings():Settings
 fun saveSettings(settings:Settings)
}
