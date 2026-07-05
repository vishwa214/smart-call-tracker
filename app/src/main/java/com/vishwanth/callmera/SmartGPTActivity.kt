package com.vishwanth.callmera

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vishwanth.callmera.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
class SmartGPTActivity : AppCompatActivity() {

    private lateinit var edtQuestion: EditText
    private lateinit var btnAsk: Button
    private lateinit var txtAnswer: TextView
    private lateinit var btnQuestions: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_smart_gpt)

        edtQuestion = findViewById(R.id.edtQuestion)
        btnAsk = findViewById(R.id.btnAsk)
        txtAnswer = findViewById(R.id.txtAnswer)
        btnQuestions =
            findViewById(R.id.btnQuestions)

        btnAsk.setOnClickListener {

            val question =
                edtQuestion.text
                    .toString()
                    .trim()
                    .lowercase()

            answerQuestion(question)
        }
    }

    private fun answerQuestion(question: String) {

        val prefs =
            getSharedPreferences(
                "app_settings",
                Context.MODE_PRIVATE
            )

        val monthMap = mapOf(
            "january" to "01",
            "february" to "02",
            "march" to "03",
            "april" to "04",
            "may" to "05",
            "june" to "06",
            "july" to "07",
            "august" to "08",
            "september" to "09",
            "october" to "10",
            "november" to "11",
            "december" to "12"
        )
        var selectedYear: String? = null

        listOf(
            "2023",
            "2024",
            "2025",
            "2026"
        ).forEach {

            if (question.contains(it)) {

                selectedYear = it
            }
        }

        var selectedMonth: String? = null
        var selectedMonthName = ""

        for ((name, number) in monthMap) {

            if (question.contains(name)) {

                selectedMonth = number
                selectedMonthName =
                    name.replaceFirstChar {
                        it.uppercase()
                    }

                break
            }
        }
        btnQuestions.setOnClickListener {

            val questions = arrayOf(

                "💰 Total Amount",

                "💰 Total Amount in January",
                "💰 Total Amount in February",
                "💰 Total Amount in March",
                "💰 Total Amount in April",
                "💰 Total Amount in May",
                "💰 Total Amount in June",
                "💰 Total Amount in July",
                "💰 Total Amount in August",
                "💰 Total Amount in September",
                "💰 Total Amount in October",
                "💰 Total Amount in November",
                "💰 Total Amount in December",

                "✅ Counted Days",
                "✅ Counted Days in May",

                "❌ Not Counted Days",
                "❌ Not Counted Days in May",

                "⚠️ Show Evening Missing in May 2026",

                "📋 Last Week Call Logs",

                "📞 Friend Number",

                "💸 Cost Per Day",

                "📱 App Name",

                "📅 Current Month",

                "📚 Total Records",

                "💰 Money This Year",

                "🏆 Best Month",

                "📉 Worst Month",

                "🔥 Current Streak",

                "🏆 Longest Streak",

                "📅 Days Remaining This Month",

                "💰 How Much Money Will I Earn This Month",

                "📋 Today's Report",

                "📋 Yesterday Report",

                "📊 This Month Report",

                "📈 Average Earnings Per Month",

                "📈 Average Counted Days Per Month",

                "🏆 Month With Most Counted Days",

                "❌ Month With Most Missed Days",

                "📞 Friend Calls Today",

                "🌅 Morning Time",

                "🌇 Evening Time",

                "📅 Today Status",

                "💰 Today's Amount",

                "✅ Today Counted",

                "❓ Help"
            )

            AlertDialog.Builder(this)

                .setTitle("🤖 Select Question")

                .setItems(questions) { _, which ->

                    edtQuestion.setText(
                        questions[which]
                    )
                }

                .show()
        }

        lifecycleScope.launch(Dispatchers.IO) {

            val database =
                AppDatabase.getDatabase(
                    this@SmartGPTActivity
                )

            val records =
                database.callDao()
                    .getAllCalls()

            var answer =
                "🤖 Sorry, I don't understand that question."
            val month =
                selectedMonth ?: ""

            when {

                // TOTAL AMOUNT

                question.contains("total amount") ||
                        question.contains("earnings") -> {

                    if (selectedMonth != null) {

                        val total =
                            records
                                .filter {
                                    it.callDate.split("-")[1] == month
                                }
                                .sumOf {
                                    it.amount
                                }

                        answer =
                            "💰 Total Amount in $selectedMonthName = ₹$total"

                    } else {

                        val total =
                            records.sumOf {
                                it.amount
                            }

                        answer =
                            "💰 Total Amount = ₹$total"
                    }
                }
                // NOT COUNTED DAYS

                question.contains("not counted") ||
                        question.contains("missed") -> {

                    if (selectedMonth != null) {

                        val missed =
                            records
                                .filter {
                                    it.callDate.split("-")[1] == month
                                }
                                .count {
                                    !it.countedDay
                                }

                        answer =
                            "❌ Not Counted Days in $selectedMonthName = $missed"

                    } else {

                        val missed =
                            records.count {
                                !it.countedDay
                            }

                        answer =
                            "❌ Total Not Counted Days = $missed"
                    }
                }

                // COUNTED DAYS

                question.contains("counted") -> {

                    if (selectedMonth != null) {

                        val counted =
                            records
                                .filter {
                                    it.callDate.split("-")[1] == month
                                }
                                .count {
                                    it.countedDay
                                }

                        answer =
                            "✅ Counted Days in $selectedMonthName = $counted"

                    } else {

                        val counted =
                            records.count {
                                it.countedDay
                            }

                        answer =
                            "✅ Total Counted Days = $counted"
                    }
                }



                // LAST WEEK CALL LOGS

                question.contains("last week") ||
                        question.contains("call log") -> {

                    val lastRecords =
                        records.sortedByDescending {
                            SimpleDateFormat(
                                "dd-MM-yyyy",
                                Locale.getDefault()
                            ).parse(it.callDate)
                        }.take(7)

                    answer =
                        if (lastRecords.isEmpty()) {

                            "No records found."

                        } else {

                            buildString {

                                append("📋 Last Week Call Logs\n\n")

                                lastRecords.forEach {

                                    append(
                                        "${it.callDate}\n" +
                                                "Amount : ₹${it.amount}\n" +
                                                "Counted : ${it.countedDay}\n\n"
                                    )
                                }
                            }
                        }
                }

                // FRIEND NUMBER

                question.contains("friend") &&
                        question.contains("number") -> {

                    answer =
                        "📞 Friend Number = ${
                            prefs.getString(
                                "friend_number",
                                "9398228116"
                            )
                        }"
                }

                // COST PER DAY

                question.contains("cost") ||
                        question.contains("price") ||
                        question.contains("amount per day") -> {

                    answer =
                        "💸 Cost Per Day = ₹${
                            prefs.getInt(
                                "cost_per_day",
                                60
                            )
                        }"
                }

                // APP NAME

                question.contains("app name") -> {

                    answer =
                        "📱 App Name = Smart Call Tracker"
                }

                // CURRENT MONTH

                question.contains("current month") ||
                        question.contains("which month") -> {

                    answer =
                        "📅 Current Month = ${
                            SimpleDateFormat(
                                "MMMM yyyy",
                                Locale.getDefault()
                            ).format(Date())
                        }"
                }

                // TOTAL RECORDS

                question.contains("records") ||
                        question.contains("entries") -> {

                    answer =
                        "📚 Total Records = ${records.size}"
                }

                // MONEY THIS YEAR

                question.contains("this year") &&
                        question.contains("money") -> {

                    val total =
                        records.sumOf {
                            it.amount
                        }

                    answer =
                        "💰 Total Earnings This Year = ₹$total"
                }

                // BEST MONTH

                question.contains("best month") ||
                        question.contains("earned the most") -> {

                    val best =
                        records.groupBy {
                            it.callDate.substring(3, 5)
                        }
                            .maxByOrNull {
                                it.value.sumOf { r -> r.amount }
                            }

                    answer =
                        if (best != null) {

                            "🏆 Best Month = ${best.key}\n" +
                                    "Amount = ₹${
                                        best.value.sumOf {
                                            it.amount
                                        }
                                    }"

                        } else {

                            "No data found."
                        }
                }

                // WORST MONTH

                question.contains("worst month") ||
                        question.contains("earned the least") -> {

                    val worst =
                        records.groupBy {
                            it.callDate.substring(3, 5)
                        }
                            .minByOrNull {
                                it.value.sumOf { r -> r.amount }
                            }

                    answer =
                        if (worst != null) {

                            "📉 Worst Month = ${worst.key}\n" +
                                    "Amount = ₹${
                                        worst.value.sumOf {
                                            it.amount
                                        }
                                    }"

                        } else {

                            "No data found."
                        }

                }
                question.contains("today status") -> {

                    val today =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                        ).format(Date())

                    val todayRecord =
                        records.find {
                            it.callDate == today
                        }

                    answer =
                        if (todayRecord != null) {

                            """
📅 Today Status

Morning : ${todayRecord.morning}

Evening : ${todayRecord.evening}

Counted : ${todayRecord.countedDay}

Amount : ₹${todayRecord.amount}
            """.trimIndent()

                        } else {

                            "No calls recorded today."
                        }
                }
                question.contains("morning time") -> {

                    val latest =
                        records.lastOrNull()

                    answer =
                        latest?.morningTime
                            ?.let {
                                "🌅 Morning Time = $it"
                            }
                            ?: "No data found."
                }
                question.contains("evening time") -> {

                    val latest =
                        records.lastOrNull()

                    answer =
                        latest?.eveningTime
                            ?.let {
                                "🌇 Evening Time = $it"
                            }
                            ?: "No data found."
                }
                question.contains("today amount") -> {

                    val today =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                        ).format(Date())

                    val amount =
                        records.find {
                            it.callDate == today
                        }?.amount ?: 0

                    answer =
                        "💰 Today's Amount = ₹$amount"
                }
                question.contains("today counted") -> {

                    val today =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                        ).format(Date())

                    val counted =
                        records.find {
                            it.callDate == today
                        }?.countedDay ?: false

                    answer =
                        if (counted)
                            "✅ Today is Counted"
                        else
                            "❌ Today is Not Counted"
                }
                question.contains("friend calls today") -> {

                    val today =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                        ).format(Date())

                    val record =
                        records.find {
                            it.callDate == today
                        }

                    answer =
                        if (record != null)
                            "📞 Morning=${record.morning} Evening=${record.evening}"
                        else
                            "No calls today"
                }
                question.contains("current streak") -> {

                    val sorted =
                        records.sortedBy {
                            it.callDate
                        }

                    var streak = 0

                    for (record in sorted.reversed()) {

                        if (record.countedDay)
                            streak++
                        else
                            break
                    }

                    answer =
                        "🔥 Current Streak = $streak Days"
                }
                question.contains("longest streak") -> {

                    var longest = 0
                    var current = 0

                    records.sortedBy {
                        it.callDate
                    }.forEach {

                        if (it.countedDay) {

                            current++

                            if (current > longest)
                                longest = current

                        } else {

                            current = 0
                        }
                    }

                    answer =
                        "🏆 Longest Streak = $longest Days"
                }
                question.contains("remaining this month") -> {

                    val cal =
                        java.util.Calendar.getInstance()

                    val today =
                        cal.get(
                            java.util.Calendar.DAY_OF_MONTH
                        )

                    val totalDays =
                        cal.getActualMaximum(
                            java.util.Calendar.DAY_OF_MONTH
                        )

                    val remaining =
                        totalDays - today

                    answer =
                        "📅 Days Remaining = $remaining"
                }
                question.contains("earn this month") ||
                        question.contains("money will i earn") -> {

                    val cost =
                        prefs.getInt(
                            "cost_per_day",
                            60
                        )

                    val cal =
                        java.util.Calendar.getInstance()

                    val today =
                        cal.get(
                            java.util.Calendar.DAY_OF_MONTH
                        )

                    val totalDays =
                        cal.getActualMaximum(
                            java.util.Calendar.DAY_OF_MONTH
                        )

                    val remaining =
                        totalDays - today

                    val projected =
                        records.sumOf {
                            it.amount
                        } +
                                (remaining * cost)

                    answer =
                        "💰 Projected Earnings = ₹$projected"
                }
                question.contains("most counted") -> {

                    val best =
                        records
                            .filter {
                                it.countedDay
                            }
                            .groupBy {
                                it.callDate.substring(3,5)
                            }
                            .maxByOrNull {
                                it.value.size
                            }

                    answer =
                        if (best != null)
                            "🏆 Month ${best.key} has ${best.value.size} counted days"
                        else
                            "No data found"
                }
                question.contains("most missed") -> {

                    val worst =
                        records
                            .filter {
                                !it.countedDay
                            }
                            .groupBy {
                                it.callDate.substring(3,5)
                            }
                            .maxByOrNull {
                                it.value.size
                            }

                    answer =
                        if (worst != null)
                            "❌ Month ${worst.key} has ${worst.value.size} missed days"
                        else
                            "No data found"
                }
                question.contains("today report") ||
                        question.contains("today full report") -> {

                    val today =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                        ).format(Date())

                    val report =
                        records.find {
                            it.callDate == today
                        }

                    answer =
                        if (report != null) {

                            """
📅 Today's Report

Morning : ${report.morning}

Morning Time : ${report.morningTime}

Evening : ${report.evening}

Evening Time : ${report.eveningTime}

Counted : ${report.countedDay}

Amount : ₹${report.amount}
            """.trimIndent()

                        } else {

                            "No report found for today."
                        }
                }
                question.contains("yesterday report") -> {

                    val cal =
                        java.util.Calendar.getInstance()

                    cal.add(
                        java.util.Calendar.DAY_OF_MONTH,
                        -1
                    )

                    val yesterday =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
                            Locale.getDefault()
                        ).format(cal.time)

                    val report =
                        records.find {
                            it.callDate == yesterday
                        }

                    answer =
                        if (report != null) {

                            """
📅 Yesterday Report

Counted : ${report.countedDay}

Amount : ₹${report.amount}
            """.trimIndent()

                        } else {

                            "No report found."
                        }
                }
                question.contains("this month report") -> {

                    val currentMonth =
                        SimpleDateFormat(
                            "MM",
                            Locale.getDefault()
                        ).format(Date())

                    val monthRecords =
                        records.filter {
                            it.callDate.split("-")[1] == month
                        }

                    answer =
                        """
📊 This Month Report

Records : ${monthRecords.size}

Counted Days : ${
                            monthRecords.count {
                                it.countedDay
                            }
                        }

Missed Days : ${
                            monthRecords.count {
                                !it.countedDay
                            }
                        }

Amount : ₹${
                            monthRecords.sumOf {
                                it.amount
                            }
                        }
        """.trimIndent()
                }
                question.contains("average earnings") -> {

                    val months =
                        records.groupBy {
                            it.callDate.substring(3,5)
                        }

                    val avg =
                        if (months.isNotEmpty())
                            records.sumOf {
                                it.amount
                            } / months.size
                        else
                            0

                    answer =
                        "📈 Average Earnings Per Month = ₹$avg"
                }

                question.contains("average counted") -> {

                    val months =
                        records.groupBy {
                            it.callDate.substring(3,5)
                        }

                    val avg =
                        if (months.isNotEmpty())
                            records.count {
                                it.countedDay
                            } / months.size
                        else
                            0

                    answer =
                        "📈 Average Counted Days Per Month = $avg"
                }
                question.contains("morning detected") ||
                        question.contains("evening missing") ||
                        question.contains("morning done") ||
                        question.contains("pending calls") -> {

                    val currentYear =
                        java.util.Calendar
                            .getInstance()
                            .get(
                                java.util.Calendar.YEAR
                            ).toString()

                    val filteredRecords =

                        records.filter {

                            val parts =
                                it.callDate.split("-")

                            val month =
                                parts[1]

                            val year =
                                parts[2]

                            val monthMatch =

                                if (selectedMonth != null)
                                    month == selectedMonth
                                else
                                    true

                            val yearMatch =

                                if (selectedYear != null)
                                    year == selectedYear
                                else
                                    year == currentYear

                            monthMatch &&
                                    yearMatch &&
                                    it.morning &&
                                    !it.evening
                        }

                    answer =

                        if (filteredRecords.isEmpty()) {

                            if (
                                selectedMonth != null &&
                                selectedYear != null
                            ) {

                                "🎉 No pending evening calls found in $selectedMonthName $selectedYear."

                            } else {

                                "🎉 No pending evening calls found."
                            }

                        } else {

                            buildString {

                                append(
                                    "⚠️ Morning Done, Evening Missing\n\n"
                                )

                                if (
                                    selectedMonth != null
                                ) {

                                    append(
                                        "Month : $selectedMonthName\n"
                                    )

                                    append(
                                        "Year : ${
                                            selectedYear
                                                ?: currentYear
                                        }\n\n"
                                    )
                                }

                                append(
                                    "Total Days : ${
                                        filteredRecords.size
                                    }\n\n"
                                )

                                filteredRecords.forEach {

                                    append(
                                        "📅 ${it.callDate}\n" +
                                                "🌅 Morning : ${it.morningTime}\n" +
                                                "🌇 Evening : Not Found\n\n"
                                    )
                                }
                            }
                        }
                }


                // HELP

                question.contains("help") -> {

                    answer =
                        """
🤖 SmartCallTrackerGPT

• Total Amount
• Total Amount in May

• Counted Days
• Counted Days in May

• Not Counted Days
• Not Counted Days in May

• Last Week Call Logs

• Friend Number

• Cost Per Day

• App Name

• Current Month

• Total Records

• Money This Year

• Best Month

• Worst Month
            """.trimIndent()
                }
            }
            runOnUiThread {
                txtAnswer.text = answer
            }
        }
    }
}