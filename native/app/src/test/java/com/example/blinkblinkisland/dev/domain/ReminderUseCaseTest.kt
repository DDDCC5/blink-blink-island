package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.usecase.ReminderUseCase
import org.junit.Assert.*
import org.junit.Test
class ReminderUseCaseTest {
 @Test fun unknownWearNeverCounts(){val r=ReminderUseCase(30);r.sample(0,null,"2026-11-11");r.sample(3600000,null,"2026-11-11");assertEquals(0L,r.wornMs)}
 @Test fun removalsPauseAndWearingAccumulates(){val r=ReminderUseCase(30);r.sample(0,true,"d");r.sample(60000,true,"d");r.sample(60000,false,"d");r.sample(90000,false,"d");r.sample(90000,true,"d");r.sample(150000,true,"d");assertEquals(120000L,r.wornMs)}
 @Test fun trainingSuppressesDueNotice(){val r=ReminderUseCase(30);r.sample(0,true,"d");r.sample(1800000,true,"d");assertFalse(r.due(training=true));assertTrue(r.due(training=false))}
 @Test fun laterNeedsTenAdditionalWornMinutes(){val r=ReminderUseCase(30);r.sample(0,true,"d");r.sample(1800000,true,"d");r.later();assertFalse(r.due(false));r.sample(2400000,true,"d");assertTrue(r.due(false))}
 @Test fun dailySilenceExpiresAtDeviceLocalNextDate(){val r=ReminderUseCase(30);r.sample(0,true,"d1");r.sample(1800000,true,"d1");r.silenceToday();assertFalse(r.due(false));r.sample(1800001,true,"d2");assertTrue(r.due(false))}
 @Test fun intervalClampsToSpec(){assertEquals(30,ReminderUseCase(1).intervalMinutes);assertEquals(120,ReminderUseCase(300).intervalMinutes)}
}
