package dk.michael.c25k.ui.postrun

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dk.michael.c25k.data.db.AppDatabase
import dk.michael.c25k.data.db.RunSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostRunViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.get(app).runSessionDao()

    private val _session = MutableStateFlow<RunSessionEntity?>(null)
    val session: StateFlow<RunSessionEntity?> = _session

    fun load(id: Long) {
        viewModelScope.launch { _session.value = dao.byId(id) }
    }

    fun save(id: Long, note: String, energyBefore: Int, energyAfter: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            val existing = dao.byId(id) ?: return@launch
            dao.update(existing.copy(note = note, energyBefore = energyBefore, energyAfter = energyAfter))
            onDone()
        }
    }
}
