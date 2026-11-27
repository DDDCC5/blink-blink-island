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
 @Test fun upgradingVersionOnePreservesRowsAndAddsPreferences(){
  val c=InstrumentationRegistry.getInstrumentation().targetContext;val name="migration_${UUID.randomUUID()}.db"
  try{
   val path=c.getDatabasePath(name);path.parentFile?.mkdirs()
   android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(path,null).use{db->
    db.execSQL("CREATE TABLE records(id TEXT PRIMARY KEY,started INTEGER NOT NULL,ended INTEGER NOT NULL,kind TEXT NOT NULL,outcome TEXT NOT NULL,attempted INTEGER NOT NULL,completed INTEGER NOT NULL,incomplete INTEGER NOT NULL,skipped INTEGER NOT NULL,interruptions INTEGER NOT NULL,active INTEGER NOT NULL,interrupted INTEGER NOT NULL)")
    db.execSQL("CREATE TABLE settings(id INTEGER PRIMARY KEY CHECK(id=1),language TEXT NOT NULL,interval INTEGER NOT NULL,music REAL NOT NULL,effects REAL NOT NULL,voice REAL NOT NULL,muted INTEGER NOT NULL,onboarding INTEGER NOT NULL)")
    db.execSQL("INSERT INTO records VALUES ('old',1,2,'GAZE','EXITED',1,0,0,0,0,5000,0)")
    db.execSQL("INSERT INTO settings VALUES (1,'en',60,.25,.4,.5,0,1)")
    db.version=1
   }
   SqliteIslandRepository(c,name).use{r->
    assertEquals("old",r.records().single().id);assertEquals("en",r.settings().language)
    assertEquals(Pace.STANDARD,r.settings().pace)
    r.saveSettings(r.settings().copy(pace=Pace.COMFORTABLE,lastPlan=SessionPlan.QUICK))
   }
   SqliteIslandRepository(c,name).use{r->assertEquals(Pace.COMFORTABLE,r.settings().pace);assertEquals(SessionPlan.QUICK,r.settings().lastPlan);assertEquals(5000L,r.records().single().activeMs)}
  }finally{c.deleteDatabase(name)}
 }

}
