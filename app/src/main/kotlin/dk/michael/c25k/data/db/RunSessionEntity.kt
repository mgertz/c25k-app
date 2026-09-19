package dk.michael.c25k.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RunOutcome { COMPLETED, CANCELLED }

@Entity(tableName = "run_sessions")
data class RunSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programIndex: Int,
    val dateTimeEpochMillis: Long,
    val outcome: RunOutcome,
    val note: String = "",
    val energyBefore: Int = 0,
    val energyAfter: Int = 0
)
