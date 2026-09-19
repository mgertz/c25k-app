package dk.michael.c25k.ui

import dk.michael.c25k.data.model.Program

fun formatDuration(seconds: Int): String =
    if (seconds % 60 == 0) "${seconds / 60} min" else "${seconds}s"

fun Program.summaryText(): String =
    intervals.joinToString(", ") { step ->
        val kind = if (step.type == "run") "løb" else "gå"
        "${formatDuration(step.seconds)} $kind"
    }
