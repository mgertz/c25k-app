package dk.michael.c25k.service

import dk.michael.c25k.data.model.Program
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ActiveRunInfo(
    val programIndex: Int,
    val title: String,
    val currentStep: String,
    val remainingSeconds: Int,
    val progress: Float
)

object ActiveRunRegistry {
    private val _activeRun = MutableStateFlow<ActiveRunInfo?>(null)
    val activeRun: StateFlow<ActiveRunInfo?> = _activeRun.asStateFlow()

    private var program: Program? = null
    private var totalSeconds: Int = 0

    fun start(program: Program, steps: List<RunStepUi>) {
        this.program = program
        totalSeconds = steps.sumOf { it.seconds }.coerceAtLeast(1)
    }

    fun update(state: RunUiState) {
        val activeProgram = program ?: return
        if (!state.phase.isActiveRunPhase()) return

        val remaining = remainingSeconds(state)
        val completed = totalSeconds - remaining
        _activeRun.value = ActiveRunInfo(
            programIndex = activeProgram.index,
            title = "Uge ${activeProgram.week} · dag ${activeProgram.day}",
            currentStep = state.steps.getOrNull(state.currentStepIndex)?.label ?: "Gør klar",
            remainingSeconds = remaining,
            progress = (completed.toFloat() / totalSeconds).coerceIn(0f, 1f)
        )
    }

    fun clear() {
        program = null
        totalSeconds = 0
        _activeRun.value = null
    }

    private fun RunPhase.isActiveRunPhase(): Boolean = when (this) {
        RunPhase.WARMUP, RunPhase.RUNNING_INTERVAL, RunPhase.COOLDOWN -> true
        RunPhase.IDLE, RunPhase.FINISHED, RunPhase.CANCELLED -> false
    }

    private fun remainingSeconds(state: RunUiState): Int {
        var remaining = 0
        state.steps.forEachIndexed { index, step ->
            when {
                index in state.completedStepIndices -> Unit
                index == state.currentStepIndex -> remaining += (step.seconds - state.elapsedInStepSeconds).coerceAtLeast(0)
                index > state.currentStepIndex -> remaining += step.seconds
            }
        }
        return remaining
    }
}
