package dk.michael.c25k.service

enum class RunPhase { IDLE, WARMUP, RUNNING_INTERVAL, COOLDOWN, FINISHED, CANCELLED }

enum class StepKind { WARMUP, RUN, WALK, COOLDOWN }

data class RunStepUi(
    val label: String,
    val seconds: Int,
    val sound: String,
    val kind: StepKind
)

data class RunUiState(
    val phase: RunPhase = RunPhase.IDLE,
    val steps: List<RunStepUi> = emptyList(),
    val currentStepIndex: Int = -1,
    val elapsedInStepSeconds: Int = 0,
    val completedStepIndices: Set<Int> = emptySet()
) {
    fun fillFraction(index: Int): Float {
        if (index != currentStepIndex) return 0f
        val total = steps.getOrNull(index)?.seconds ?: return 0f
        if (total <= 0) return 0f
        return (elapsedInStepSeconds.toFloat() / total).coerceIn(0f, 1f)
    }
}
