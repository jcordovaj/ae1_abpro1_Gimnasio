package com.mod6.ae1_abpro1_gimnasio.presentation

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mod6.ae1_abpro1_gimnasio.data.repository.TimerRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import com.mod6.ae1_abpro1_gimnasio.data.model.CicloVidaEvento // 🚨 NECESARIO

// Constantes
private const val TIMER_INTERVAL_MS = 1000L // 1 segundo
private const val DEFAULT_DURATION_SECONDS = 60L // 1 minuto por defecto

class TimerViewModel(private val repository: TimerRepository) : ViewModel() {

    // --- LiveData de Estado y Tiempo ---
    private val _tiempoActualSegundos = MutableLiveData(0L)
    val tiempoActualSegundos: LiveData<Long> = _tiempoActualSegundos

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    // Indica si el modo actual es Temporizador (true) o Cronómetro (false)
    private val _isTimerMode = MutableLiveData(false)
    val isTimerMode: LiveData<Boolean> = _isTimerMode

    private val _auditLog = MutableLiveData<List<CicloVidaEvento>>()
    val auditLog: LiveData<List<CicloVidaEvento>> = _auditLog

    // Tiempo total configurado para el modo Temporizador
    private var configuredDurationSeconds: Long = DEFAULT_DURATION_SECONDS

    // --- Mecanismos de Conteo ---
    private var countDownTimer: CountDownTimer? = null
    private var chronometerJob: Job? = null

    // Almacena el tiempo restante al pausar (esencial para retomar)
    private var timeWhenPausedSeconds: Long = 0L

    init {
        // Inicializa el tiempo actual basado en la duración por defecto (60s)
        _tiempoActualSegundos.value = DEFAULT_DURATION_SECONDS
    }

    // --- Control de Modo ---
    fun toggleMode(isTimer: Boolean) {
        if (_isRunning.value == true) return

        _isTimerMode.value = isTimer

        if (isTimer) {
            _tiempoActualSegundos.value = configuredDurationSeconds
        } else {
            _tiempoActualSegundos.value = 0L
        }
    }

    fun loadAuditLog() {
        viewModelScope.launch {
            val log = repository.obtenerAuditoria().take(20)
            _auditLog.postValue(log)
        }
    }

    fun setTimerDuration(durationInSeconds: Long) {
        if (_isRunning.value == true) return
        configuredDurationSeconds = durationInSeconds
        _tiempoActualSegundos.value = durationInSeconds
        _isTimerMode.value = true
    }

    // --- Control de Ejecución ---
    fun toggleTimer() {
        if (_isRunning.value == true) {
            pauseTimer()
        } else {
            startOrResumeTimer()
        }
    }

    fun startOrResumeTimer() {
        if (_isRunning.value == true) return

        _isRunning.value = true
        timeWhenPausedSeconds = _tiempoActualSegundos.value ?: 0L

        if (_isTimerMode.value == true) {
            startCountDownTimer(timeWhenPausedSeconds)
        } else {
            startChronometer(timeWhenPausedSeconds)
        }
    }

    fun pauseTimer() {
        if (_isTimerMode.value == true) {
            countDownTimer?.cancel()
        } else {
            chronometerJob?.cancel()
        }
        _isRunning.value = false
    }

    fun resetTimer() {
        pauseTimer()

        val duracionFinal = if (_isTimerMode.value == true) {
            configuredDurationSeconds
        } else {
            _tiempoActualSegundos.value ?: 0L
        }

        if (duracionFinal > 0) {
            guardarEjercicio(duracionFinal)
        }

        _tiempoActualSegundos.value = if (_isTimerMode.value == true) {
            configuredDurationSeconds
        } else {
            0L
        }
        timeWhenPausedSeconds = _tiempoActualSegundos.value ?: 0L
    }

    // --- Lógica Específica de Conteo ---
    private fun startCountDownTimer(startTimeSeconds: Long) {
        countDownTimer?.cancel()

        val millisInFuture = startTimeSeconds * 1000L

        countDownTimer = object : CountDownTimer(millisInFuture, TIMER_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                _tiempoActualSegundos.value = millisUntilFinished / 1000L
            }

            override fun onFinish() {
                _tiempoActualSegundos.value = 0L
                _isRunning.value = false
                auditarEventoCicloVida("TEMPORIZADOR_FINALIZADO")
                guardarEjercicio(configuredDurationSeconds)
            }
        }.start()
    }

    private fun startChronometer(startTimeSeconds: Long) {
        chronometerJob?.cancel()
        _tiempoActualSegundos.value = startTimeSeconds

        chronometerJob = viewModelScope.launch {
            var current = startTimeSeconds
            while (_isRunning.value == true) {
                delay(TIMER_INTERVAL_MS)
                current += 1
                _tiempoActualSegundos.value = current
            }
        }
    }

    // --- Lógica de Persistencia de Datos (Room) ---
    private fun guardarEjercicio(duracion: Long) {
        if (duracion > 0) {
            viewModelScope.launch {
                repository.guardarEjercicio(duracion)
                Log.d("ROOM", "Ejercicio guardado con duración: $duracion segundos.")
            }
        }
    }

    fun auditarEventoCicloVida(evento: String) {
        viewModelScope.launch {
            repository.auditarEvento(evento)
            Log.d("ROOM", "Evento de ciclo de vida auditado: $evento")
            loadAuditLog() // Refresca el log en la UI inmediatamente
        }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        chronometerJob?.cancel()
        auditarEventoCicloVida("onCleared")
    }
}