package com.rt.manger.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rt.manger.data.AppScanner
import com.rt.manger.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val scanner = AppScanner(application)
    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.Default) {
            _loading.value = true
            _apps.value = scanner.scanLauncherApps()
            _loading.value = false
        }
    }
}
