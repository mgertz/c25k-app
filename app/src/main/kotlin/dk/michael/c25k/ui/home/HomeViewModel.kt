package dk.michael.c25k.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dk.michael.c25k.data.ProgramRepository
import dk.michael.c25k.data.db.AppDatabase
import dk.michael.c25k.data.db.RunSessionEntity
import dk.michael.c25k.data.model.Program
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ProgramRepository(app)
    private val dao = AppDatabase.get(app).runSessionDao()

    val programs: List<Program> = repository.programs

    val sessions: StateFlow<List<RunSessionEntity>> =
        dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
