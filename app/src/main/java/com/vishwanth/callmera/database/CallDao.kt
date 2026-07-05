package com.vishwanth.callmera.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CallDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertCall(
        call: CallEntity
    )

    @Query(
        "SELECT * FROM call_history"
    )
    suspend fun getAllCalls():
            List<CallEntity>

    @Query(
        "SELECT * FROM call_history WHERE countedDay = 1"
    )
    suspend fun getCountedDays():
            List<CallEntity>

    @Query(
        "SELECT SUM(amount) FROM call_history"
    )
    suspend fun getTotalAmount():
            Int?

    @Query(
        "SELECT * FROM call_history WHERE callDate LIKE :monthPattern"
    )
    suspend fun getCallsByMonth(
        monthPattern: String
    ): List<CallEntity>

    @Query(
        "DELETE FROM call_history"
    )
    suspend fun deleteAll()
    @Query(
        "SELECT * FROM call_history WHERE countedDay = 1"
    )
    suspend fun getSuccessfulCalls():
            List<CallEntity>
    @Query(
        "SELECT * FROM call_history WHERE callDate = :date LIMIT 1"
    )
    suspend fun getCallByDate(
        date: String
    ): CallEntity?
}