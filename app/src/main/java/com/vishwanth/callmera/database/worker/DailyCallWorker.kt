package com.vishwanth.callmera.worker

import android.provider.CallLog
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import com.vishwanth.callmera.database.AppDatabase
import com.vishwanth.callmera.database.CallEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.vishwanth.callmera.NotificationHelper
import java.text.DateFormatSymbols
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
class DailyCallWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val FRIEND_NUMBER = "9398228116"

    override suspend fun doWork(): Result {

        if (
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return Result.failure()
        }

        val database =
            AppDatabase.getDatabase(
                applicationContext
            )

        val dao = database.callDao()

        val cursor =
            applicationContext.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                null
            )

        val dayMap =
            mutableMapOf<
                    String,
                    Triple<
                            Boolean,
                            Boolean,
                            Pair<String, String>
                            >
                    >()

        cursor?.use {

            while (it.moveToNext()) {


                val number =
                    it.getString(
                        it.getColumnIndexOrThrow(
                            CallLog.Calls.NUMBER
                        )
                    )

                if (
                    number.takeLast(10)
                    != FRIEND_NUMBER.takeLast(10)
                ) {
                    continue
                }

                val timeMillis =
                    it.getLong(
                        it.getColumnIndexOrThrow(
                            CallLog.Calls.DATE
                        )
                    )

                val calendar =
                    Calendar.getInstance()

                calendar.timeInMillis =
                    timeMillis

                val date =
                    SimpleDateFormat(
                        "dd-MM-yyyy",
                        Locale.getDefault()
                    ).format(calendar.time)
                val time =
                    SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(calendar.time)
                val totalMinutes =
                    calendar.get(Calendar.HOUR_OF_DAY) * 60 +
                            calendar.get(Calendar.MINUTE)

                val morning =
                    totalMinutes in 480..540

                val evening =
                    totalMinutes in 930..960

                val previous =
                    dayMap[date]
                        ?: Triple(
                            false,
                            false,
                            Pair("", "")
                        )

                val morningTime =
                    if (
                        morning &&
                        previous.third.first.isEmpty()
                    )
                        time
                    else
                        previous.third.first

                val eveningTime =
                    if (
                        evening &&
                        previous.third.second.isEmpty()
                    )
                        time
                    else
                        previous.third.second

                dayMap[date] =
                    Triple(
                        previous.first || morning,
                        previous.second || evening,
                        Pair(
                            morningTime,
                            eveningTime
                        )
                    )
            }
        }
        val today =
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(java.util.Date())

        val todayRecord = dayMap[today]

        if (todayRecord == null) {

            NotificationHelper.showNotification(
                applicationContext,
                "📞 No Calls Today",
                "No morning or evening call has been detected today."
            )

            return Result.success()
        }

        val counted =
            todayRecord.first && todayRecord.second

        dao.insertCall(

            CallEntity(
                callDate = today,
                phoneNumber = FRIEND_NUMBER,

                morning = todayRecord.first,
                evening = todayRecord.second,

                morningTime = todayRecord.third.first,
                eveningTime = todayRecord.third.second,

                countedDay = counted,

                amount = if (counted) 60 else 0
            )
        )

        val parsedDate =
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).parse(today)

        val cal = Calendar.getInstance()
        cal.time = parsedDate!!

        val dayName =
            DateFormatSymbols().weekdays[
                cal.get(Calendar.DAY_OF_WEEK)
            ]

        if (counted) {

            NotificationHelper.showNotification(
                applicationContext,
                "🎉 Day Counted",
                "$dayName ($today)\nMorning ✅ Evening ✅\n₹60 Added Successfully"
            )

        } else if (
            todayRecord.first &&
            !todayRecord.second
        ) {

            NotificationHelper.showNotification(
                applicationContext,
                "⚠️ Evening Call Missing",
                "$dayName ($today)\nMorning ✅\nEvening ❌"
            )

        } else if (
            !todayRecord.first &&
            todayRecord.second
        ) {

            NotificationHelper.showNotification(
                applicationContext,
                "⚠️ Morning Call Missing",
                "$dayName ($today)\nMorning ❌\nEvening ✅"
            )

        } else {

            NotificationHelper.showNotification(
                applicationContext,
                "📞 No Calls Today",
                "$dayName ($today)\nMorning ❌\nEvening ❌"
            )
        }

        return Result.success()
    }
}