package com.example.blinkblinkisland.dev
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.blinkblinkisland.dev.data.repository.SqliteIslandRepository
import com.example.blinkblinkisland.dev.domain.model.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.UUID
@RunWith(AndroidJUnit4::class)
class StorageInstrumentedTest {
 @Test fun recordsSurviveRepositoryRecreationAndDuplicateSavesAreIdempotent(){
  val c=InstrumentationRegistry.getInstrumentation().targetContext;val db="test_${UUID.randomUUID()}.db"
  try{val row=Record("offline",1,2,"BLINK",Outcome.SKIPPED,1,0,0,1,0,0,0);SqliteIslandRepository(c,db).use{r->r.save(row);r.save(row);assertEquals(1,r.records().size)};SqliteIslandRepository(c,db).use{r->assertEquals(row,r.records().single())}}finally{c.deleteDatabase(db)}
 }
 @Test fun deletionPersistsAndRetainsAudioLanguageSettings(){
  val c=InstrumentationRegistry.getInstrumentation().targetContext;val db="test_${UUID.randomUUID()}.db"
  try{SqliteIslandRepository(c,db).use{r->r.save(Record("r",1,2,"GAZE",Outcome.EXITED,0,0,0,0,0,0,0));r.saveSettings(Settings(language="en",voice=.5f,onboarding=true));r.deleteRecords()};SqliteIslandRepository(c,db).use{r->assertTrue(r.records().isEmpty());assertEquals("en",r.settings().language);assertEquals(.5f,r.settings().voice);assertTrue(r.settings().onboarding)}}finally{c.deleteDatabase(db)}
 }
}
