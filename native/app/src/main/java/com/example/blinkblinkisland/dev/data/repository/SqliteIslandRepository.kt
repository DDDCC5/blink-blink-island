package com.example.blinkblinkisland.dev.data.repository
import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.blinkblinkisland.dev.domain.model.*

class SqliteIslandRepository(context:Context,name:String="blink_island.db"):SQLiteOpenHelper(context.applicationContext,name,null,1),IslandRepository {
 override fun onCreate(db:SQLiteDatabase){
  db.execSQL("CREATE TABLE records(id TEXT PRIMARY KEY,started INTEGER NOT NULL,ended INTEGER NOT NULL,kind TEXT NOT NULL,outcome TEXT NOT NULL,attempted INTEGER NOT NULL,completed INTEGER NOT NULL,incomplete INTEGER NOT NULL,skipped INTEGER NOT NULL,interruptions INTEGER NOT NULL,active INTEGER NOT NULL,interrupted INTEGER NOT NULL)")
  db.execSQL("CREATE TABLE settings(id INTEGER PRIMARY KEY CHECK(id=1),language TEXT NOT NULL,interval INTEGER NOT NULL,music REAL NOT NULL,effects REAL NOT NULL,voice REAL NOT NULL,muted INTEGER NOT NULL,onboarding INTEGER NOT NULL)")
 }
 override fun onUpgrade(db:SQLiteDatabase,old:Int,new:Int){error("Database migration required; never silently erase records")}
 override fun records():List<Record> = readableDatabase.rawQuery("SELECT * FROM records ORDER BY started DESC",null).use{c->buildList{while(c.moveToNext()) add(Record(c.getString(0),c.getLong(1),c.getLong(2),c.getString(3),Outcome.valueOf(c.getString(4)),c.getInt(5),c.getInt(6),c.getInt(7),c.getInt(8),c.getInt(9),c.getLong(10),c.getLong(11)))}}
 override fun save(record:Record){val v=ContentValues().apply{put("id",record.id);put("started",record.startedAt);put("ended",record.endedAt);put("kind",record.kind);put("outcome",record.outcome.name);put("attempted",record.attempted);put("completed",record.completed);put("incomplete",record.incomplete);put("skipped",record.skipped);put("interruptions",record.interruptions);put("active",record.activeMs);put("interrupted",record.interruptedMs)};check(writableDatabase.insertWithOnConflict("records",null,v,SQLiteDatabase.CONFLICT_IGNORE)!=-1L||readableDatabase.rawQuery("SELECT id FROM records WHERE id=?",arrayOf(record.id)).use{it.moveToFirst()})}
 override fun deleteRecords(){writableDatabase.delete("records",null,null)}
 override fun settings():Settings=readableDatabase.rawQuery("SELECT language,interval,music,effects,voice,muted,onboarding FROM settings WHERE id=1",null).use{c->if(!c.moveToFirst())Settings() else Settings(c.getString(0),c.getInt(1).coerceIn(30,120),c.getFloat(2).coerceIn(0f,1f),c.getFloat(3).coerceIn(0f,1f),c.getFloat(4).coerceIn(0f,1f),c.getInt(5)!=0,c.getInt(6)!=0)}
 override fun saveSettings(settings:Settings){val s=settings;writableDatabase.insertWithOnConflict("settings",null,ContentValues().apply{put("id",1);put("language",s.language);put("interval",s.interval.coerceIn(30,120));put("music",s.music.coerceIn(0f,1f));put("effects",s.effects.coerceIn(0f,1f));put("voice",s.voice.coerceIn(0f,1f));put("muted",if(s.muted)1 else 0);put("onboarding",if(s.onboarding)1 else 0)},SQLiteDatabase.CONFLICT_REPLACE)}
}
