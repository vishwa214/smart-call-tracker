package com.vishwanth.callmera.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_history")
data class CallEntity(

    @PrimaryKey
    val callDate: String,

    val phoneNumber: String,

    val morning: Boolean,

    val evening: Boolean,

    val morningTime: String,

    val eveningTime: String,

    val countedDay: Boolean,

    val amount: Int
)