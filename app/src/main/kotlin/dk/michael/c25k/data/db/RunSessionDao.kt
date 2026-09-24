package dk.michael.c25k.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RunSessionDao {

    @Insert
    suspend fun insert(session: RunSessionEntity): Long

    @Update
    suspend fun update(session: RunSessionEntity)

    @Query("SELECT * FROM run_sessions ORDER BY dateTimeEpochMillis DESC")
    fun observeAll(): Flow<List<RunSessionEntity>>

    @Query("SELECT * FROM run_sessions ORDER BY dateTimeEpochMillis DESC LIMIT 2")
    suspend fun lastTwo(): List<RunSessionEntity>

    @Query("SELECT * FROM run_sessions ORDER BY dateTimeEpochMillis DESC")
    suspend fun all(): List<RunSessionEntity>

    @Query("SELECT * FROM run_sessions WHERE id = :id")
    suspend fun byId(id: Long): RunSessionEntity?
}
