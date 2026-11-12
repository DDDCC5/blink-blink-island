package com.example.blinkblinkisland.dev.ui.island
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.blinkblinkisland.dev.data.repository.SqliteIslandRepository
import com.example.blinkblinkisland.dev.domain.usecase.IslandUseCase
class IslandViewModelFactory(context:Context):ViewModelProvider.Factory {
 private val appContext=context.applicationContext
 @Suppress("UNCHECKED_CAST") override fun <T:ViewModel> create(modelClass:Class<T>):T=IslandViewModel(IslandUseCase(SqliteIslandRepository(appContext))) as T
}
