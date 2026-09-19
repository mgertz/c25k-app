package dk.michael.c25k.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IntervalStep(
    val type: String,   // "run" or "walk"
    val seconds: Int,
    val sound: String
)

@Serializable
data class Program(
    val index: Int,
    val week: Int,
    val day: Int,
    val warmupSeconds: Int,
    val warmupSound: String,
    val cooldownSeconds: Int,
    val cooldownSound: String,
    val completeSound: String,
    val intervals: List<IntervalStep>
)

@Serializable
data class ProgramsFile(val programs: List<Program>)
