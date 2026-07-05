package com.vishwanth.callmera.database

class Repository(
    private val callDao: CallDao
) {

    suspend fun insertCall(
        call: CallEntity
    ) {
        callDao.insertCall(call)
    }

    suspend fun getAllCalls():
            List<CallEntity> {

        return callDao.getAllCalls()
    }

    suspend fun deleteAll() {

        callDao.deleteAll()
    }
}