package com.example.blinkblinkisland.dev.domain.usecase
import com.example.blinkblinkisland.dev.domain.model.Record

data class HistoryPage(val items:List<Record>,val index:Int,val pageCount:Int)
object HistoryPaginationUseCase {
 fun page(records:List<Record>,requested:Int):HistoryPage {
  val count=maxOf(1,(records.size+1)/2)
  val index=requested.coerceIn(0,count-1)
  return HistoryPage(records.drop(index*2).take(2),index,count)
 }
}
