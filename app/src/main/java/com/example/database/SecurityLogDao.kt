package com.example.database

import androidx.room.*
import com.example.model.SecurityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface SecurityLogDao {
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<SecurityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityLog)

    @Query("DELETE FROM security_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM security_logs WHERE id = :logId")
    suspend fun deleteLog(logId: Int)
}
