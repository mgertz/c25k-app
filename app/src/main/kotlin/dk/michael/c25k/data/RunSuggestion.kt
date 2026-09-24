package dk.michael.c25k.data

import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity

/**
 * The four options on the "choose run" screen.
 * lastTwo[0] is the most recent session, lastTwo[1] the one before that.
 */
object RunSuggestion {

    fun next(sessions: List<RunSessionEntity>, lastIndex: Int): Int =
        sessions
            .filter { it.outcome == RunOutcome.COMPLETED }
            .maxOfOrNull { it.programIndex }
            ?.let { (it + 1).coerceAtMost(lastIndex) }
            ?: 0

    fun sameAsLast(lastTwo: List<RunSessionEntity>): Int? = lastTwo.getOrNull(0)?.programIndex

    fun sameAsOneBefore(lastTwo: List<RunSessionEntity>): Int? = lastTwo.getOrNull(1)?.programIndex
}
