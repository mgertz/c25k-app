package dk.michael.c25k.data

import android.content.Context
import dk.michael.c25k.data.model.Program
import dk.michael.c25k.data.model.ProgramsFile
import kotlinx.serialization.json.Json

/**
 * Loads the 27-program C25K plan from assets/programs.json.
 * Drop a new programs.json into assets to change the plan without touching code.
 */
class ProgramRepository(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val appContext = context.applicationContext

    val programs: List<Program> by lazy {
        val text = appContext.assets.open("programs.json").bufferedReader().use { it.readText() }
        json.decodeFromString<ProgramsFile>(text).programs.sortedBy { it.index }
    }

    fun byIndex(index: Int): Program? = programs.firstOrNull { it.index == index }

    val lastIndex: Int get() = programs.size - 1
}
