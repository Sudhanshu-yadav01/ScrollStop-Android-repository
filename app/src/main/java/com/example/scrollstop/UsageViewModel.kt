
package com.example.scrollstop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class UsageViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dao = ScrollStopDatabase
        .getDatabase(application)
        .dailyUsageDao()

    val allUsage = dao.getAllUsage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
