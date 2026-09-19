package dk.michael.c25k.ui.activerun

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dk.michael.c25k.data.ProgramRepository
import dk.michael.c25k.data.db.AppDatabase
import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity
import dk.michael.c25k.service.RunForegroundService
import dk.michael.c25k.service.RunPhase
import dk.michael.c25k.service.RunUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActiveRunViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ProgramRepository(app)
    private val dao = AppDatabase.get(app).runSessionDao()

    private var service: RunForegroundService? = null
    private var bound = false
    private var started = false
    private var collectJob: Job? = null

    private var programIndex: Int = -1
    private var startTimeMillis: Long = 0L
    private var sessionInserted = false

    private val _state = MutableStateFlow(RunUiState())
    val state: StateFlow<RunUiState> = _state

    private val _savedSessionId = MutableStateFlow<Long?>(null)
    val savedSessionId: StateFlow<Long?> = _savedSessionId

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val svc = (binder as RunForegroundService.LocalBinder).service()
            service = svc
            bound = true
            repository.byIndex(programIndex)?.let { svc.start(it) }
            collectJob = viewModelScope.launch {
                svc.state.collect { s ->
                    _state.value = s
                    if ((s.phase == RunPhase.FINISHED || s.phase == RunPhase.CANCELLED) && !sessionInserted) {
                        sessionInserted = true
                        saveSession(s.phase == RunPhase.FINISHED)
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            service = null
        }
    }

    fun start(index: Int) {
        if (started) return
        started = true
        programIndex = index
        startTimeMillis = System.currentTimeMillis()
        val context = getApplication<Application>()
        val intent = Intent(context, RunForegroundService::class.java)
        context.startForegroundService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun cancel() {
        service?.cancel()
    }

    private fun saveSession(completed: Boolean) {
        viewModelScope.launch {
            val id = dao.insert(
                RunSessionEntity(
                    programIndex = programIndex,
                    dateTimeEpochMillis = startTimeMillis,
                    outcome = if (completed) RunOutcome.COMPLETED else RunOutcome.CANCELLED
                )
            )
            _savedSessionId.value = id
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (bound) {
            getApplication<Application>().unbindService(connection)
            bound = false
        }
    }
}
