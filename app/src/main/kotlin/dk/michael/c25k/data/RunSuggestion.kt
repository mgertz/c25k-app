package dk.michael.c25k.data

import dk.michael.c25k.data.db.RunOutcome
import dk.michael.c25k.data.db.RunSessionEntity

/**
 * The four options on the "choose run" screen.
 * lastTwo[0] is the most recent session, lastTwo[1] the one before that.
 */
object RunSuggestion {

    fun next(lastTwo: List<RunSessionEntity>, lastIndex: Int): Int {
        val last = lastTwo.getOrNull(0) ?: return 0
        return if (last.outcome == RunOutcome.CANCELLED) last.programIndex
        else (last.programIndex + 1).coerceAtMost(lastIndex)
    }

    fun sameAsLast(lastTwo: List<RunSessionEntity>): Int? = lastTwo.getOrNull(0)?.programIndex

    fun sameAsOneBefore(lastTwo: List<RunSessionEntity>): Int? = lastTwo.getOrNull(1)?.programIndex
}
