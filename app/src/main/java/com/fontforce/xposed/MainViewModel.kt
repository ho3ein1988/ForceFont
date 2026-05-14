package com.fontforce.xposed

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fontforce.xposed.data.ALL_HOOK_METHODS
import com.fontforce.xposed.data.HookPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MainUiState(
    val isModuleActive: Boolean = false,
    val methodStates: Map<String, Boolean> = ALL_HOOK_METHODS.associate { it.id to it.defaultEnabled }
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = HookPreferences(application)
    private val hookPrefs = application.getSharedPreferences(
        "fontforce_hook_prefs", Context.MODE_PRIVATE
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        // روش درست تشخیص LSPosed: اگر ماژول لود شده باشه، hook یه flag مینویسه
        // اگر flag نبود، یعنی ماژول فعال نیست
        val active = hookPrefs.getBoolean("module_active", false)
        _uiState.value = _uiState.value.copy(isModuleActive = active)

        prefs.allMethodStates()
            .onEach { states ->
                _uiState.value = _uiState.value.copy(methodStates = states)
                writeHookMirror(states)
            }
            .launchIn(viewModelScope)
    }

    fun toggleMethod(id: String, enabled: Boolean) {
        viewModelScope.launch {
            prefs.setMethodEnabled(id, enabled)
        }
    }

    private fun writeHookMirror(states: Map<String, Boolean>) {
        hookPrefs.edit().apply {
            states.forEach { (id, enabled) -> putBoolean("method_$id", enabled) }
            apply()
        }
    }
}
