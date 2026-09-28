package dk.michael.c25k.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dk.michael.c25k.MainActivity
import dk.michael.c25k.data.model.Program
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Runs the whole warmup -> intervals -> cooldown -> complete sequence as a foreground
 * service, so the timer keeps going with the screen off. Holds a partial wake lock
 * for the duration of the run so the countdown doesn't drift under Doze.
 */
class RunForegroundService : Service() {

    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var runJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private lateinit var vibrator: Vibrator
    private var completeSound: String = "complete"

    private val _state = MutableStateFlow(RunUiState())
    val state: StateFlow<RunUiState> = _state

    inner class LocalBinder : Binder() {
        fun service(): RunForegroundService = this@RunForegroundService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        createNotificationChannel()
    }

    fun start(program: Program) {
        if (runJob?.isActive == true) return
        completeSound = program.completeSound
        acquireWakeLock()
        startForeground(NOTIFICATION_ID, buildNotification("Løbetur startet"))
        val steps = buildSteps(program)
        ActiveRunRegistry.start(program, steps)
        publishState(RunUiState(phase = RunPhase.WARMUP, steps = steps, currentStepIndex = -1))
        runJob = scope.launch { runSteps(steps) }
    }

    fun cancel() {
        publishState(_state.value.copy(phase = RunPhase.CANCELLED))
        runJob?.cancel()
    }

    private fun publishState(state: RunUiState) {
        _state.value = state
        ActiveRunRegistry.update(state)
    }

    private fun buildSteps(program: Program): List<RunStepUi> {
        val steps = mutableListOf<RunStepUi>()
        steps += RunStepUi("Opvarmning", program.warmupSeconds, program.warmupSound, StepKind.WARMUP)
        program.intervals.forEach { interval ->
            val kind = if (interval.type == "run") StepKind.RUN else StepKind.WALK
            val label = if (interval.type == "run") "Løb" else "Gå"
            steps += RunStepUi(label, interval.seconds, interval.sound, kind)
        }
        steps += RunStepUi("Nedkøling", program.cooldownSeconds, program.cooldownSound, StepKind.COOLDOWN)
        return steps
    }

    private fun phaseFor(kind: StepKind): RunPhase = when (kind) {
        StepKind.WARMUP -> RunPhase.WARMUP
        StepKind.RUN, StepKind.WALK -> RunPhase.RUNNING_INTERVAL
        StepKind.COOLDOWN -> RunPhase.COOLDOWN
    }

    private fun fallbackFor(kind: StepKind): String = when (kind) {
        StepKind.RUN -> "run"
        StepKind.WALK, StepKind.WARMUP -> "walk"
        StepKind.COOLDOWN -> "cooldown"
    }

    private suspend fun runSteps(steps: List<RunStepUi>) {
        try {
            for ((index, step) in steps.withIndex()) {
                playSound(step.sound, fallbackFor(step.kind))
                vibrate()
                updateNotification(step.label)
                publishState(_state.value.copy(
                    phase = phaseFor(step.kind),
                    currentStepIndex = index,
                    elapsedInStepSeconds = 0
                ))
                var elapsed = 0
                while (elapsed < step.seconds) {
                    delay(1000)
                    elapsed++
                    publishState(_state.value.copy(elapsedInStepSeconds = elapsed))
                }
                publishState(_state.value.copy(
                    completedStepIndices = _state.value.completedStepIndices + index
                ))
            }
            playSound(completeSound, "complete")
            vibrate()
            publishState(_state.value.copy(phase = RunPhase.FINISHED))
        } finally {
            withContext(NonCancellable) { finishInternal() }
        }
    }

    private fun playSound(name: String, fallback: String) {
        scope.launch(Dispatchers.IO) {
            try {
                playAsset("sounds/$name.mp3")
            } catch (e: Exception) {
                if (name != fallback) {
                    try {
                        playAsset("sounds/$fallback.mp3")
                    } catch (_: Exception) {
                        // No sound file yet — silently skip, vibration still fires.
                    }
                }
            }
        }
    }

    private fun playAsset(path: String) {
        val afd = assets.openFd(path)
        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
        )
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mp.setOnCompletionListener { it.release() }
        mp.prepare()
        mp.start()
    }

    private fun vibrate() {
        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Løbetur", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("C25K")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(text))
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "C25K::RunWakeLock").apply {
            acquire(90 * 60 * 1000L) // safety timeout, released explicitly when the run ends
        }
    }

    private fun finishInternal() {
        ActiveRunRegistry.clear()
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "c25k_run"
        const val NOTIFICATION_ID = 1
    }
}
