package com.example.blinkblinkisland.dev.domain
import com.example.blinkblinkisland.dev.domain.model.*
import com.example.blinkblinkisland.dev.domain.usecase.*
import org.junit.Assert.*
import org.junit.Test
class HistoryPaginationUseCaseTest {
 private fun rows(n:Int)=(0 until n).map{Record("$it",0,1,"GAZE",Outcome.EXITED,0,0,0,0,0,0,0)}
 @Test fun everyRecordIsReachableOnceWithoutAnUnboundedPanel(){val records=rows(97);val seen=mutableListOf<String>();for(i in 0 until 49){val p=HistoryPaginationUseCase.page(records,i);assertTrue(p.items.size<=2);assertEquals(49,p.pageCount);seen+=p.items.map{it.id}};assertEquals(records.map{it.id},seen)}
 @Test fun pageClampsAfterDeletionAndEmptyListStillHasOnePage(){val p=HistoryPaginationUseCase.page(rows(3),48);assertEquals(1,p.index);assertEquals(listOf("2"),p.items.map{it.id});val empty=HistoryPaginationUseCase.page(emptyList(),48);assertEquals(0,empty.index);assertEquals(1,empty.pageCount);assertTrue(empty.items.isEmpty())}
}
