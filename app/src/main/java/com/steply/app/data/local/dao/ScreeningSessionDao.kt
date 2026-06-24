package com.steply.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.steply.app.data.local.entities.ScreeningSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreeningSessionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: ScreeningSessionEntity)

    @Query("SELECT * FROM screening_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): ScreeningSessionEntity?

    @Query("SELECT * FROM screening_sessions WHERE id = :id LIMIT 1")
    fun observeSessionById(id: String): Flow<ScreeningSessionEntity?>

    @Query(
        """
        SELECT * FROM screening_sessions
        WHERE userId = :userId
        ORDER BY completedAt DESC
        """,
    )
    fun observeSessionsForUser(userId: String): Flow<List<ScreeningSessionEntity>>

    @Query("DELETE FROM screening_sessions WHERE userId = :userId")
    suspend fun deleteSessionsForUser(userId: String)

    @Query("DELETE FROM screening_sessions")
    suspend fun deleteAllSessions()
}
