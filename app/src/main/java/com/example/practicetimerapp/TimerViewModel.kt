package com.example.practicetimerapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.Locale

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("setting")

private val totalTimeKey = longPreferencesKey("total_time")

enum class TimerState {
    STOPPED,
    RUNNING,
    PAUSED
}

class TimerViewModel: ViewModel() {

    private var initTime = 60_000L

    private var totalTime by mutableLongStateOf(initTime)

    private var timeLeft by mutableLongStateOf(initTime)

    private var timer: Job? = null

    private var state by mutableStateOf(TimerState.STOPPED)

    val isRunning get() = state == TimerState.RUNNING

    val canPlus1 get() = timeLeft <= 60_000L * 59

    val caMinus1 get() = timeLeft > 60_000L

    val progress get() = timeLeft / totalTime.toFloat()

    var finish by mutableStateOf(false)
        private set

    val timeLeftText: String
        get() {
            val seconds = (timeLeft / 1000) % 60
            val minutes = (timeLeft / 1000) / 60
            return String.format(Locale.JAPANESE, "%02d:%02d", minutes, seconds)
        }

    val totalTimeText: String
        get() {
            val seconds = (totalTime / 1000) % 60
            val minutes = (totalTime / 1000) / 60
            return String.format(Locale.JAPANESE, "%02d:%02d", minutes, seconds)
        }

    fun plus1() {
        timeLeft += 60_000L
        totalTime += 60_000L
    }

    fun minus1() {
        timeLeft -= 60_000L
        totalTime -= 60_000L
    }

    fun startOrPauseTimer () {
        when (state) {
            TimerState.STOPPED, TimerState.PAUSED -> {
                countDown()
            }

            TimerState.RUNNING -> {
               state = TimerState.PAUSED
            }

            else -> {}
        }
    }

    private fun countDown() {
        timer?.cancel()

        state = TimerState.RUNNING
        timer = viewModelScope.launch {
            while (timeLeft > 0 && isRunning) {
                delay(100)
                timeLeft -= 100
            }
            if (timeLeft <= 0) {
                timeLeft = 0
                state = TimerState.STOPPED
                finish = true
            }
        } // launchの閉じかっこ
    } // countDownの閉じかっこ

    fun resetTimer() {
        state = TimerState.STOPPED
        timer?.cancel()
        timer = null
        totalTime = initTime
        timeLeft = initTime
    }

    fun applyFinish () {
        timeLeft = totalTime
        finish = false
    }

    fun saveTotalTime(context: Context) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[totalTimeKey] = totalTime
                initTime = totalTime
            }
        }
    }

    fun loadTotalTime(context: Context) {
        viewModelScope.launch {
            val preferences = context.dataStore.data.first()
            val restored = preferences[totalTimeKey] ?: initTime
            initTime = restored
            resetTimer()
        }
    }
} // TimerViewModelの閉じかっこ