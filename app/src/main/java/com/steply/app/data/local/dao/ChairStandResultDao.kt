package com.steply.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.steply.app.data.local.entities.ChairStandResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChairStandResultDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertResult(result: ChairStandResultEntity)

    @Query("SELECT * FROM chair_stand_results WHERE id = :id LIMIT 1")
    suspend fun getResultById(id: String): ChairStandResultEntity?

    @Query(
        """
        SELECT * FROM chair_stand_results
        WHERE userId = :userId
        ORDER BY createdAt DESC
        """,
    )
    fun observeResultsForUser(userId: String): Flow<List<ChairStandResultEntity>>

    @Query(
        """
        SELECT * FROM chair_stand_results
        WHERE userId = :userId
        ORDER BY createdAt DESC
        LIMIT 1
        """,
    )
    fun observeLatestResultForUser(userId: String): Flow<ChairStandResultEntity?>

    @Query(
        """
        SELECT * FROM chair_stand_results
        WHERE id = :id AND userId = :userId
        LIMIT 1
        """,
    )
    fun observeResultForUser(userId: String, id: String): Flow<ChairStandResultEntity?>

    @Query("DELETE FROM chair_stand_results WHERE userId = :userId")
    suspend fun deleteResultsForUser(userId: String)

    @Query("DELETE FROM chair_stand_results")
    suspend fun deleteAllResults()
}
