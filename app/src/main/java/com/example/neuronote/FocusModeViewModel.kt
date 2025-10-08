package com.example.neuronote

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.max

data class FocusModeState(
    val isFocusing: Boolean = false,
    val remainingSec: Int = 0,
    val selectedPresetMin: Int = 25,
    val customMin: Int = 30,
    val previousInterruptionFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL,
    val nowTime: LocalTime = LocalTime.now()
)

class FocusModeViewModel : ViewModel() {

    private val _state = MutableStateFlow(FocusModeState())
    val state: StateFlow<FocusModeState> = _state

    private var timerJob: Job? = null
    private var clockJob: Job? = null

    init {
        // live clock that keeps running even if you leave the page
        clockJob = viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(nowTime = LocalTime.now())
                delay(1000)
            }
        }
    }

    fun setPreset(minutes: Int) {
        _state.value = _state.value.copy(selectedPresetMin = minutes)
    }

    fun setCustom(minutes: Int) {
        _state.value = _state.value.copy(customMin = minutes)
    }

    fun openDndSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun startWithPreset(context: Context) = startFocus(context, _state.value.selectedPresetMin)
    fun startWithCustom(context: Context) = startFocus(context, _state.value.customMin)

    fun startFocus(context: Context, minutes: Int) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!nm.isNotificationPolicyAccessGranted) {
            openDndSettings(context)
            return
        }

        val previous = nm.currentInterruptionFilter

        // ✅ Use PRIORITY instead of NONE so media playback continues
        // (Silences most notifications without stealing media audio focus)
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)

        val total = minutes * 60
        _state.value = _state.value.copy(
            isFocusing = true,
            remainingSec = total,
            previousInterruptionFilter = previous
        )

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.isFocusing && _state.value.remainingSec > 0) {
                delay(1000)
                _state.value = _state.value.copy(remainingSec = max(_state.value.remainingSec - 1, 0))
                if (_state.value.remainingSec == 0) {
                    stopFocus(context) // auto-restore when finished
                }
            }
        }
    }

    fun reset(context: Context) {
        // stop timer but leave focus off
        stopFocus(context)
        _state.value = _state.value.copy(remainingSec = 0)
    }

    fun stopFocus(context: Context) {
        timerJob?.cancel()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(_state.value.previousInterruptionFilter)
        }
        _state.value = _state.value.copy(isFocusing = false)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        clockJob?.cancel()
    }
}
