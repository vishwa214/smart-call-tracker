package com.vishwanth.callmera

import android.os.Bundle
import android.provider.CallLog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vishwanth.callmera.adapter.HistoryAdapter
import com.vishwanth.callmera.model.CallHistory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistory)

        recycler.layoutManager = LinearLayoutManager(this)

        // Load REAL call history instead of dummy data
        val historyList = loadCallHistory()

        recycler.adapter = HistoryAdapter(historyList)
    }

    private fun loadCallHistory(): List<CallHistory> {

        val friendNumber = "9398228116"

        val dayMap = mutableMapOf<String, Pair<Boolean, Boolean>>()

        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " DESC"
        )

        cursor?.use {

            while (it.moveToNext()) {

                val number =
                    it.getString(
                        it.getColumnIndexOrThrow(
                            CallLog.Calls.NUMBER
                        )
                    )

                // Compare last 10 digits
                if (!number.takeLast(10)
                        .equals(friendNumber.takeLast(10))
                ) {
                    continue
                }

                val timeMillis =
                    it.getLong(
                        it.getColumnIndexOrThrow(
                            CallLog.Calls.DATE
                        )
                    )

                val cal = Calendar.getInstance()
                cal.timeInMillis = timeMillis

                val date =
                    SimpleDateFormat(
                        "dd-MM-yyyy",
                        Locale.getDefault()
                    ).format(cal.time)

                val hour =
                    cal.get(Calendar.HOUR_OF_DAY)

                val minute =
                    cal.get(Calendar.MINUTE)

                val totalMinutes =
                    hour * 60 + minute

                val morning =
                    totalMinutes in 742..744

                val evening =
                    totalMinutes in 745..747

                val previous =
                    dayMap[date] ?: Pair(false, false)

                dayMap[date] =
                    Pair(
                        previous.first || morning,
                        previous.second || evening
                    )
            }
        }

        val result = mutableListOf<CallHistory>()

        for ((date, pair) in dayMap) {

            result.add(
                CallHistory(
                    date = date,
                    morning = pair.first,
                    evening = pair.second
                )
            )
        }

        return result
    }
}