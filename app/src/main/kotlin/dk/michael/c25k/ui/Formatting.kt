package dk.michael.c25k.ui

import dk.michael.c25k.data.model.Program

fun formatDuration(seconds: Int): String =
    if (seconds % 60 == 0) "${seconds / 60} min" else "${seconds}s"

fun formatClock(seconds: Int): String {
    val minutes = seconds / 60
    val rest = seconds % 60
    return "%d:%02d".format(minutes, rest)
}

fun Program.totalSeconds(): Int = warmupSeconds + cooldownSeconds + intervals.sumOf { it.seconds }

fun Program.runSeconds(): Int = intervals.filter { it.type == "run" }.sumOf { it.seconds }

fun Program.walkSeconds(): Int = intervals.filter { it.type != "run" }.sumOf { it.seconds }

fun Program.summaryText(): String =
    intervals.joinToString(", ") { step ->
        val kind = if (step.type == "run") "løb" else "gå"
        "${formatDuration(step.seconds)} $kind"
    }
