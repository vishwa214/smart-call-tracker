package com.vishwanth.callmera

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.provider.CallLog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.vishwanth.callmera.database.AppDatabase
import android.graphics.Color
import android.widget.ProgressBar
class StatisticsActivity : AppCompatActivity() {

    private lateinit var txtCountedDays: TextView
    private lateinit var txtMissedDays: TextView
    private lateinit var txtAmount: TextView
    private lateinit var txtStreak: TextView
    private lateinit var txtCompletion: TextView
    private lateinit var txtInsights: TextView

    private lateinit var progressCompletion: ProgressBar
    private lateinit var barChart: BarChart
    private var allCalls =
        emptyList<com.vishwanth.callmera.database.CallEntity>()
    private var countedDays = 0
    private var missedDays = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_statistics)

        txtCountedDays =
            findViewById(R.id.txtCountedDays)

        txtMissedDays =
            findViewById(R.id.txtMissedDays)

        txtAmount =
            findViewById(R.id.txtAmount)

        txtStreak =
            findViewById(R.id.txtStreak)

        txtCompletion =
            findViewById(R.id.txtCompletion)

        txtInsights =
            findViewById(R.id.txtInsights)

        progressCompletion =
            findViewById(R.id.progressCompletion)

        barChart =
            findViewById(R.id.barChart)

        loadStatistics()
    }

    private fun loadStatistics() {

        lifecycleScope.launch(Dispatchers.IO) {

            val database =
                AppDatabase.getDatabase(this@StatisticsActivity)

            val currentMonth =
                Calendar.getInstance()
                    .get(Calendar.MONTH) + 1

            val currentYear =
                Calendar.getInstance()
                    .get(Calendar.YEAR)
                    .toString()

            allCalls =
                database.callDao()
                    .getAllCalls()
                    .filter {

                        val parts =
                            it.callDate.split("-")

                        parts[1].toInt() ==
                                currentMonth &&

                                parts[2] ==
                                currentYear
                    }

            val countedDays =
                allCalls.count {
                    it.countedDay
                }

            val missedDays =
                allCalls.count {
                    !it.countedDay
                }

            val totalAmount =
                allCalls.sumOf {
                    it.amount
                }

            val totalDays =
                countedDays + missedDays

            val completion =
                if (totalDays > 0)
                    (countedDays * 100) / totalDays
                else
                    0

            val streak =
                getCurrentStreak(allCalls)

            runOnUiThread {

                txtCountedDays.text =
                    countedDays.toString()

                txtMissedDays.text =
                    missedDays.toString()

                txtAmount.text =
                    "₹$totalAmount"

                txtStreak.text =
                    streak.toString()

                txtCompletion.text =
                    "$completion% Completed"

                progressCompletion.progress =
                    completion

                txtInsights.text =
                    """
🏆 Counted Days : $countedDays

❌ Missed Days : $missedDays

🔥 Current Streak : $streak Days

💰 Total Earnings : ₹$totalAmount

📈 Completion Rate : $completion%

🎯 Goal Progress :
${100 - completion}% Remaining
    """.trimIndent()

                setupBarChart()
            }

        }
    }
    private fun getCurrentStreak(
        records: List<com.vishwanth.callmera.database.CallEntity>
    ): Int {

        val sorted =
            records
                .filter {
                    it.countedDay
                }
                .sortedByDescending {
                    it.callDate
                }

        return sorted.size
    }


    private fun setupBarChart() {

        val monthlyMap =
            mutableMapOf<Int, Int>()

        for (month in 1..12) {
            monthlyMap[month] = 0
        }


        allCalls.forEach {

            if (it.countedDay) {

                val month =
                    it.callDate
                        .split("-")[1]
                        .toInt()

                monthlyMap[month] =
                    monthlyMap[month]!! + 1
            }
        }

        val entries =
            ArrayList<BarEntry>()

        for (month in 1..12) {

            entries.add(

                BarEntry(
                    month.toFloat(),
                    monthlyMap[month]!!.toFloat()
                )
            )

        }


        val dataSet =
            BarDataSet(
                entries,
                "Counted Days"
            )

        dataSet.color =
            Color.parseColor("#8B5CF6")

        val data =
            BarData(dataSet)

        data.setValueTextColor(
            Color.WHITE
        )

        barChart.description.isEnabled =
            false

        barChart.axisLeft.textColor =
            Color.WHITE

        barChart.axisRight.textColor =
            Color.WHITE

        barChart.xAxis.textColor =
            Color.WHITE

        barChart.legend.textColor =
            Color.WHITE

        barChart.data =
            data

        barChart.animateY(1500)

        barChart.invalidate()
    }
}