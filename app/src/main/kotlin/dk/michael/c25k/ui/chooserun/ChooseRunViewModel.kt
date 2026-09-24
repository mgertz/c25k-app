package dk.michael.c25k.ui.chooserun

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dk.michael.c25k.data.ProgramRepository
import dk.michael.c25k.data.RunSuggestion
import dk.michael.c25k.data.db.AppDatabase
import dk.michael.c25k.data.model.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChooseRunUiState(
    val loaded: Boolean = false,
    val nextIndex: Int = 0,
    val sameAsLastIndex: Int? = null,
    val sameAsOneBeforeIndex: Int? = null
)

class ChooseRunViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = ProgramRepository(app)
    private val dao = AppDatabase.get(app).runSessionDao()

    val programs: List<Program> = repository.programs

    private val _uiState = MutableStateFlow(ChooseRunUiState())
    val uiState: StateFlow<ChooseRunUiState> = _uiState

    init {
        viewModelScope.launch {
            val sessions = dao.all()
            val lastTwo = sessions.take(2)
            _uiState.value = ChooseRunUiState(
                loaded = true,
                nextIndex = RunSuggestion.next(sessions, repository.lastIndex),
                sameAsLastIndex = RunSuggestion.sameAsLast(lastTwo),
                sameAsOneBeforeIndex = RunSuggestion.sameAsOneBefore(lastTwo)
            )
        }
    }

    fun program(index: Int): Program? = repository.byIndex(index)
}
