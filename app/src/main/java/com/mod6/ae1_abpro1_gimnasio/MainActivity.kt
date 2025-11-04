package com.mod6.ae1_abpro1_gimnasio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.NumberPicker // Componente del diálogo
import android.graphics.Color // Para el resaltado de botones (Color.parseColor)
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.appcompat.app.AlertDialog // Para el diálogo
import com.mod6.ae1_abpro1_gimnasio.R
import com.mod6.ae1_abpro1_gimnasio.data.db.TimerDatabase
import com.mod6.ae1_abpro1_gimnasio.data.repository.TimerRepository
import com.mod6.ae1_abpro1_gimnasio.presentation.TimerViewModel
import com.mod6.ae1_abpro1_gimnasio.presentation.TimerViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val TAG = "CicloVidaApp"
    private lateinit var viewModel: TimerViewModel
    private lateinit var textViewTimer: TextView
    private lateinit var buttonToggleTimer: Button
    private lateinit var buttonReset: Button
    private lateinit var textViewAuditLog: TextView

    // Vistas de Control de Modo y Configuración
    private lateinit var buttonSetCronometerMode: Button
    private lateinit var buttonSetTimerMode: Button
    private lateinit var buttonConfigureTime: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Encontrar elementos del layout
        textViewTimer           = findViewById(R.id.textViewTimer)
        buttonToggleTimer       = findViewById(R.id.buttonToggleTimer)
        buttonReset             = findViewById(R.id.buttonReset)
        textViewAuditLog        = findViewById(R.id.textViewAuditLog)

        // Vistas de la nueva interfaz de modo/configuración
        buttonSetCronometerMode = findViewById(R.id.buttonSetCronometerMode)
        buttonSetTimerMode      = findViewById(R.id.buttonSetTimerMode)
        buttonConfigureTime     = findViewById(R.id.buttonConfigureTime)

        // 2. Inicialización de Room y ViewModel
        val db = Room.databaseBuilder(
            applicationContext,
            TimerDatabase::class.java, "timer-db"
        ).build()
        val repository = TimerRepository(db.timerDao())
        val factory = TimerViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory).get(TimerViewModel::class.java)

        // 3. Observar LiveData

        // Tiempo
        viewModel.tiempoActualSegundos.observe(this, Observer { segundos ->
            textViewTimer.text = formatTime(segundos)
        })

        // Estado (Controla Iniciar/Pausar y deshabilita la configuración)
        viewModel.isRunning.observe(this, Observer { isRunning ->
            buttonToggleTimer.text = if (isRunning) "Pausar" else "Iniciar"
            buttonReset.isEnabled = !isRunning

            // Deshabilitar la configuración de modo/duración mientras corre
            buttonSetCronometerMode.isEnabled = !isRunning
            buttonSetTimerMode.isEnabled = !isRunning
            buttonConfigureTime.isEnabled = !isRunning
        })

        // Modo (Controla la visibilidad y el resaltado)
        viewModel.isTimerMode.observe(this, Observer { isTimerMode ->
            // Mostrar/Ocultar el botón de configuración (solo en modo Temporizador)
            buttonConfigureTime.visibility = if (isTimerMode) View.VISIBLE else View.GONE

            // Resaltar el modo activo
            if (isTimerMode) {
                // Modo Temporizador activo
                buttonSetTimerMode.setBackgroundColor(Color.parseColor("#4CAF50"))
                buttonSetCronometerMode.setBackgroundColor(Color.GRAY)
            } else {
                // Modo Cronómetro activo
                buttonSetTimerMode.setBackgroundColor(Color.GRAY)
                buttonSetCronometerMode.setBackgroundColor(Color.parseColor("#4CAF50"))
            }

            // Resetear al cambiar de modo, solo si no está corriendo
            if (viewModel.isRunning.value != true) {
                viewModel.resetTimer()
            }
        })

        // Auditoría
        viewModel.auditLog.observe(this, Observer { eventos ->
            val logText = eventos.joinToString("\n") { evento ->
                val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val time = timeFormatter.format(evento.timestamp)
                "[$time] ${evento.nombreEvento}"
            }
            textViewAuditLog.text = logText
        })

        // Carga inicial del log
        viewModel.loadAuditLog()

        // 4. Configurar Listeners

        // Botón Modo Cronómetro
        buttonSetCronometerMode.setOnClickListener {
            viewModel.toggleMode(false)
            Toast.makeText(this, "Modo: Cronómetro", Toast.LENGTH_SHORT).show()
        }

        // Botón Modo Temporizador
        buttonSetTimerMode.setOnClickListener {
            viewModel.toggleMode(true)
            Toast.makeText(this, "Modo: Temporizador", Toast.LENGTH_SHORT).show()
        }

        // Botón Configurar Tiempo (Abre el diálogo de NumberPicker)
        buttonConfigureTime.setOnClickListener {
            showTimeConfigurationDialog()
        }

        buttonToggleTimer.setOnClickListener {
            viewModel.toggleTimer()
        }

        buttonReset.setOnClickListener {
            viewModel.resetTimer()
        }

        // Establecer el modo por defecto al iniciar (Temporizador)
        viewModel.toggleMode(true)

        logAndAudit("onCreate")
    }

    // --- Métodos de Ciclo de Vida para persistencia segura ---

    override fun onStart() {
        super.onStart()
        logAndAudit("onStart")
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isRunning.value == true) {
            viewModel.startOrResumeTimer()
        }
        logAndAudit("onResume")
    }

    override fun onPause() {
        super.onPause()
        if (viewModel.isRunning.value == true) {
            viewModel.pauseTimer()
        }
        logAndAudit("onPause")
    }

    override fun onStop() {
        super.onStop()
        logAndAudit("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        logAndAudit("onDestroy")
    }

    // --- Métodos Auxiliares ---

    private fun formatTime(totalSegundos: Long): String {
        val horas    = totalSegundos / 3600
        val minutos  = (totalSegundos % 3600) / 60
        val segundos = totalSegundos % 60
        return String.format("%02d:%02d:%02d", horas, minutos, segundos)
    }

    private fun logAndAudit(evento: String) {
        Log.d(TAG, "CONSOLA: Evento del Ciclo de Vida -> $evento")
        viewModel.auditarEventoCicloVida(evento)
    }

    // IMPLEMENTACIÓN DEL DIÁLOGO DE CONFIGURACIÓN
    private fun showTimeConfigurationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_config, null)

        val numberPicker = dialogView.findViewById<NumberPicker>(R.id.numberPickerMinutes)
        numberPicker.minValue = 1
        numberPicker.maxValue = 60
        numberPicker.value = 5 // Valor por defecto

        AlertDialog.Builder(this)
            .setTitle("Configurar Temporizador (Minutos)")
            .setView(dialogView)
            .setPositiveButton("Establecer") { _, _ ->
                val minutes = numberPicker.value.toLong()
                val durationInSeconds = minutes * 60L

                viewModel.setTimerDuration(durationInSeconds)
                viewModel.resetTimer()
                Toast.makeText(this, "Temporizador fijado a $minutes minutos.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}