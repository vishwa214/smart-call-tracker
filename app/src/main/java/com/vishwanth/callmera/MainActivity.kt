package com.vishwanth.callmera
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.vishwanth.callmera.database.AppDatabase
import com.vishwanth.callmera.database.CallEntity
import com.vishwanth.callmera.database.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.widget.Toast
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.vishwanth.callmera.worker.DailyCallWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import android.widget.ImageButton
import java.util.Date
class MainActivity : AppCompatActivity() {

    private val FRIEND_NUMBER = "9398228116"
    private val COST_PER_DAY = 60

    private lateinit var resultText: TextView
    private lateinit var btnCalculate: Button
    private lateinit var btnMonthlyHistory: Button

    private lateinit var database: AppDatabase
    private lateinit var repository: Repository

    private lateinit var txtTotalDays: TextView
    private lateinit var txtAmount: TextView
    private lateinit var txtCounted: TextView
    private lateinit var txtMissed: TextView
    private lateinit var btnStatistics: Button
    private lateinit var btnSharePdf: Button
    private lateinit var btnRecords: Button
    private lateinit var btnSettings: Button
    private lateinit var txtCurrentMonth: TextView
    private lateinit var txtCompletion: TextView
    private lateinit var txtProgressInfo: TextView
    private lateinit var progressMonth: android.widget.ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        NotificationHelper.createChannel(this)
        NotificationHelper.showNotification(
            this,
            "Test Notification",
            "Smart Call Tracker notifications are working!"
        )

        startDailyWorker()

        resultText =
            findViewById(R.id.resultText)

        btnCalculate =
            findViewById(R.id.btnCalculate)

        btnMonthlyHistory =
            findViewById(R.id.btnMonthlyHistory)

        txtTotalDays =
            findViewById(R.id.txtTotalDays)

        txtAmount =
            findViewById(R.id.txtAmount)

        txtCounted =
            findViewById(R.id.txtCounted)

        txtMissed =
            findViewById(R.id.txtMissed)

        btnStatistics =
            findViewById(R.id.btnStatistics)

        btnSharePdf =
            findViewById(R.id.btnSharePdf)

        btnRecords =
            findViewById(R.id.btnRecords)

        btnSettings =
            findViewById(R.id.btnSettings)

        txtCurrentMonth =
            findViewById(R.id.txtCurrentMonth)

        txtCompletion =
            findViewById(R.id.txtCompletion)

        txtProgressInfo =
            findViewById(R.id.txtProgressInfo)

        progressMonth =
            findViewById(R.id.progressMonth)

        database =
            AppDatabase.getDatabase(this)

        repository =
            Repository(database.callDao())

        checkTodayStatus()

        btnStatistics.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    StatisticsActivity::class.java
                )
            )
        }

        btnCalculate.setOnClickListener {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CALL_LOG
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_CALL_LOG
                    ),
                    101
                )

            } else {

                calculateDays()
                saveCallLogsToDatabase()
            }
        }

        btnMonthlyHistory.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MonthlyHistoryActivity::class.java
                )
            )
        }

        btnSharePdf.setOnClickListener {

            Toast.makeText(
                this,
                "Please use Monthly History → Export PDF",
                Toast.LENGTH_LONG
            ).show()
        }

        btnRecords.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    DailyRecordsActivity::class.java
                )
            )
        }

        val btnMusic =
            findViewById<ImageButton>(
                R.id.btnMusic
            )

        val btnGPT =
            findViewById<ImageButton>(
                R.id.btnGPT
            )

        btnGPT.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SmartGPTActivity::class.java
                )
            )
        }

        btnMusic.setOnClickListener {

            if (MusicManager.isPlaying()) {

                MusicManager.stop()

                btnMusic.setImageResource(
                    android.R.drawable.ic_lock_silent_mode
                )

            } else {

                MusicManager.start(this)

                btnMusic.setImageResource(
                    android.R.drawable.ic_lock_silent_mode_off
                )
            }
        }

        btnSettings.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }

        // Permission check LAST
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            calculateDays()
            saveCallLogsToDatabase()

        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_CALL_LOG
                ),
                101
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (
            requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {

            calculateDays()
            saveCallLogsToDatabase()
        }
    }

    private fun calculateDays() {

        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        val dayMap =
            mutableMapOf<String, Pair<Boolean, Boolean>>()

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
                val hour =
                    calendar.get(
                        Calendar.HOUR_OF_DAY
                    )

                val minute =
                    calendar.get(Calendar.MINUTE)

                val totalMinutes =
                    hour * 60 + minute

                val morning =
                    totalMinutes in 480..540

                val evening =
                    totalMinutes in 930..960

                val previous =
                    dayMap[date]
                        ?: Pair(false, false)

                dayMap[date] =
                    Pair(
                        previous.first || morning,
                        previous.second || evening
                    )
            }
        }

        var totalDays = 0

        val currentMonth =
            Calendar.getInstance().get(Calendar.MONTH)

        val currentYear =
            Calendar.getInstance().get(Calendar.YEAR)

        for ((date, pair) in dayMap) {

            val sdf = SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            )

            val parsedDate = sdf.parse(date)

            val cal = Calendar.getInstance()
            cal.time = parsedDate!!

            if (
                cal.get(Calendar.MONTH) == currentMonth &&
                cal.get(Calendar.YEAR) == currentYear
            ) {

                if (
                    pair.first &&
                    pair.second
                ) {
                    totalDays++
                }
            }
        }

        val amount =
            totalDays * COST_PER_DAY

        txtTotalDays.text = totalDays.toString()
        txtAmount.text = "₹$amount"
        txtCounted.text = totalDays.toString()

        val missedDays =
            dayMap.size - totalDays

        txtMissed.text =
            missedDays.toString()

        val calendar =
            Calendar.getInstance()

        val currentDay =
            calendar.get(
                Calendar.DAY_OF_MONTH
            )

        val totalMonthDays =
            calendar.getActualMaximum(
                Calendar.DAY_OF_MONTH
            )

        val remainingDays =
            totalMonthDays - currentDay

        val completion =
            (totalDays * 100) / totalMonthDays

        txtCurrentMonth.text =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale.getDefault()
            ).format(Date())

        txtCompletion.text =
            "$completion% Completed"

        progressMonth.progress =
            completion

        txtProgressInfo.text =
            "✅ $totalDays Completed  •  ⏳ $remainingDays Days Left This Month"
    }


    private fun saveCallLogsToDatabase() {

        lifecycleScope.launch(Dispatchers.IO) {
            repository.deleteAll()
            val dayMap =
                mutableMapOf<String,
                        Triple<Boolean, Boolean,
                                Pair<String, String>>>()

            val cursor =
                contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    null,
                    null,
                    null,
                    null
                )

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
                    ) continue

                    val timeMillis =
                        it.getLong(
                            it.getColumnIndexOrThrow(
                                CallLog.Calls.DATE
                            )
                        )

                    val cal =
                        Calendar.getInstance()

                    cal.timeInMillis =
                        timeMillis

                    val date =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                        ).format(cal.time)

                    val time =
                        SimpleDateFormat(
                            "hh:mm a",
                            Locale.getDefault()
                        ).format(cal.time)

                    val totalMinutes =
                        cal.get(Calendar.HOUR_OF_DAY) * 60 +
                                cal.get(Calendar.MINUTE)

                    val morning =
                        totalMinutes in 480..540

                    val evening =
                        totalMinutes in 930..960

                    val old =
                        dayMap[date]
                            ?: Triple(
                                false,
                                false,
                                Pair("", "")
                            )

                    val morningTime =
                        if (
                            morning &&
                            old.third.first.isEmpty()
                        ) time
                        else old.third.first

                    val eveningTime =
                        if (
                            evening &&
                            old.third.second.isEmpty()
                        ) time
                        else old.third.second

                    dayMap[date] =
                        Triple(
                            old.first || morning,
                            old.second || evening,
                            Pair(
                                morningTime,
                                eveningTime
                            )
                        )
                    android.util.Log.d(
                        "TEST",
                        "Date=$date Morning=$morning Evening=$evening Time=$time"
                    )
                }
            }

            for ((date, value) in dayMap) {

                val counted =
                    value.first &&
                            value.second

                repository.insertCall(

                    CallEntity(
                        callDate = date,
                        phoneNumber = FRIEND_NUMBER,
                        morning = value.first,
                        evening = value.second,
                        morningTime = value.third.first,
                        eveningTime = value.third.second,
                        countedDay = counted,
                        amount = if (counted) 60 else 0
                    )
                )
            }
        }

    }
    private fun startDailyWorker() {

        val request =
            PeriodicWorkRequestBuilder<DailyCallWorker>(
                24,
                TimeUnit.HOURS
            ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "daily_call_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
    }
    private fun checkTodayStatus() {

        val today =
            SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.getDefault()
            ).format(Date())

        val prefs =
            getSharedPreferences(
                "notification_prefs",
                MODE_PRIVATE
            )

        val currentTime =
            Calendar.getInstance()

        val totalMinutes =
            currentTime.get(Calendar.HOUR_OF_DAY) * 60 +
                    currentTime.get(Calendar.MINUTE)

        lifecycleScope.launch(Dispatchers.IO) {

            val record =
                database.callDao()
                    .getCallByDate(today)

            runOnUiThread {

                val morningNotified =
                    prefs.getBoolean(
                        "morning_$today",
                        false
                    )

                val eveningNotified =
                    prefs.getBoolean(
                        "evening_$today",
                        false
                    )

                // Morning reminder after 9:00 AM

                if (
                    totalMinutes > 540 &&
                    record == null &&
                    !morningNotified
                ) {

                    NotificationHelper.showNotification(
                        this@MainActivity,
                        "⚠️ Morning Call Missing",
                        "No morning call received today."
                    )

                    prefs.edit()
                        .putBoolean(
                            "morning_$today",
                            true
                        )
                        .apply()
                }

                // Evening reminder after 4:30 PM

                if (
                    totalMinutes >= 990 &&
                    !eveningNotified
                ) {

                    when {

                        record == null -> {

                            NotificationHelper.showNotification(
                                this@MainActivity,
                                "❌ Day Not Counted",
                                "Morning ❌ Evening ❌\n₹0 added today."
                            )
                        }

                        record.morning &&
                                !record.evening -> {

                            NotificationHelper.showNotification(
                                this@MainActivity,
                                "⚠️ Evening Call Missing",
                                "Morning ✅ Evening ❌\nDay not counted."
                            )
                        }

                        record.countedDay -> {

                            NotificationHelper.showNotification(
                                this@MainActivity,
                                "🎉 Day Counted",
                                "Morning ✅ Evening ✅\n₹${record.amount} added."
                            )
                        }
                    }

                    prefs.edit()
                        .putBoolean(
                            "evening_$today",
                            true
                        )
                        .apply()
                }
            }
        }
    }
}