package com.rt.manger.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rt.manger.data.AppScanner
import com.rt.manger.data.IconPackScanner
import com.rt.manger.data.ProjectStore
import com.rt.manger.model.IconPack
import com.rt.manger.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val scanner = AppScanner(app)
    private val packsScanner = IconPackScanner(app)
    val projects = ProjectStore(app)

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val apps: StateFlow<List<InstalledApp>> = _apps

    private val _packs = MutableStateFlow<List<IconPack>>(emptyList())
    val packs: StateFlow<List<IconPack>> = _packs

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    init { refresh() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.Default) {
            _loading.value = true
            _apps.value = scanner.scanLauncherApps()
            _packs.value = packsScanner.scan()
            _loading.value = false
        }
    }
}
