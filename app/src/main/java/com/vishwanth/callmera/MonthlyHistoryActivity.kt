package com.vishwanth.callmera

import android.os.Bundle
import android.provider.CallLog
import android.widget.Button
import com.kizitonwose.calendar.view.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.os.Environment
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import android.content.Intent
import androidx.core.content.FileProvider
import java.time.YearMonth
import java.time.DayOfWeek
import android.view.View
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.MonthDayBinder
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.vishwanth.callmera.database.AppDatabase
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
class MonthlyHistoryActivity : AppCompatActivity() {

    private lateinit var btnSelectMonth: Button
    private lateinit var calendarView: CalendarView

    private lateinit var txtMonth: TextView
    private lateinit var txtDays: TextView
    private lateinit var txtAmount: TextView
    private lateinit var btnExportPdf: Button
    private val FRIEND_NUMBER = "9398228116"
    private val COST_PER_DAY = 60
    private val dayMap =
        mutableMapOf<String, Pair<Boolean, Boolean>>()
    class DayViewContainer(
        view: View
    ) : ViewContainer(view) {

        val textView: TextView =
            view.findViewById(R.id.dayText)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_history)

        btnSelectMonth = findViewById(R.id.btnSelectMonth)
        calendarView = findViewById(R.id.calendarView)

        txtMonth = findViewById(R.id.txtMonth)
        txtDays = findViewById(R.id.txtDays)
        txtAmount = findViewById(R.id.txtAmount)

        val currentMonth =
            DateFormatSymbols().months[
                Calendar.getInstance().get(Calendar.MONTH)
            ]

        btnSelectMonth.text = currentMonth

        btnSelectMonth.setOnClickListener {
            showMonthPicker()
        }
        btnExportPdf =
            findViewById(R.id.btnExportPdf)

        btnExportPdf.setOnClickListener {
            exportPdf()
        }
        val currentYear =
            Calendar.getInstance().get(Calendar.YEAR)

        val currentMonthIndex =
            Calendar.getInstance().get(Calendar.MONTH)

        val currentYearMonth =
            YearMonth.of(
                currentYear,
                currentMonthIndex + 1
            )

        val startMonth =
            YearMonth.of(
                currentYear,
                1
            )

        val endMonth =
            YearMonth.of(
                currentYear + 1,
                12
            )
        calendarView.setup(
            startMonth,
            endMonth,
            DayOfWeek.SUNDAY
        )


//        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
//
//            val selectedDate =
//                String.format(
//                    "%02d-%02d-%04d",
//                    dayOfMonth,
//                    month + 1,
//                    year
//                )
//
//            Toast.makeText(
//                this,
//                selectedDate,
//                Toast.LENGTH_SHORT
//            ).show()
//        }
        calendarView.dayBinder =
            object : MonthDayBinder<DayViewContainer> {

                override fun create(view: View): DayViewContainer {
                    return DayViewContainer(view)

                }

                override fun bind(
                    container: DayViewContainer,
                    data: CalendarDay
                ) {

                    container.textView.text =
                        data.date.dayOfMonth.toString()
                    container.textView.setTextColor(
                        android.graphics.Color.WHITE
                    )

                    container.textView.visibility =
                        View.VISIBLE

                    container.textView.text =
                        data.date.dayOfMonth.toString()

                    container.textView.setTextColor(
                        android.graphics.Color.WHITE
                    )
                    if (data.position != DayPosition.MonthDate) {

                        container.textView.visibility =
                            View.INVISIBLE

                        container.textView.text = ""

                        return
                    }

                    container.textView.visibility =
                        View.VISIBLE

                    val key =
                        String.format(
                            "%02d-%02d-%04d",
                            data.date.dayOfMonth,
                            data.date.monthValue,
                            data.date.year
                        )
                    android.util.Log.d(
                        "CALENDAR_KEY",
                        key
                    )
                    val status =
                        dayMap[key]
                    android.util.Log.d(
                        "DAY",
                        data.date.dayOfMonth.toString()
                    )
                    when {

                        status != null &&
                                status.first &&
                                status.second -> {

                            container.textView.setBackgroundResource(
                                R.drawable.bg_day_green
                            )
                        }

                        status != null &&
                                (status.first || status.second) -> {

                            container.textView.setBackgroundResource(
                                R.drawable.bg_day_yellow
                            )
                        }

                        else -> {

                            container.textView.setBackgroundResource(
                                R.drawable.bg_day_red
                            )
                        }
                    }
                }
            }
        calendarView.scrollToMonth(
            currentYearMonth
        )

        loadMonthData(currentMonth)
    }

    private fun showMonthPicker() {

        val currentMonthIndex =
            Calendar.getInstance().get(Calendar.MONTH)

        val months = arrayOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
        )

        val availableMonths =
            months.sliceArray(0..currentMonthIndex)

        val adapter = ArrayAdapter(
            this,
            R.layout.dialog_month_item,
            availableMonths
        )

        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Select Month")
                .setAdapter(adapter) { _, which ->

                    val selectedMonth =
                        availableMonths[which]

                    btnSelectMonth.text =
                        selectedMonth

                    loadMonth(which)

                    calendarView.notifyCalendarChanged()

                    loadMonthData(selectedMonth)
                }
                .create()

        dialog.show()

        dialog.window?.setBackgroundDrawableResource(
            R.drawable.bg_dialog
        )
    }

    private fun loadMonth(monthIndex: Int) {

        val yearMonth = YearMonth.of(
            2026,
            monthIndex + 1
        )

        calendarView.scrollToMonth(
            yearMonth
        )
    }

    private fun loadMonthData(month: String) {

        lifecycleScope.launch(Dispatchers.IO) {

            val database =
                AppDatabase.getDatabase(
                    this@MonthlyHistoryActivity
                )

            val allCalls =
                database.callDao().getAllCalls()

            val monthNumber =
                when (month) {
                    "January" -> "01"
                    "February" -> "02"
                    "March" -> "03"
                    "April" -> "04"
                    "May" -> "05"
                    "June" -> "06"
                    "July" -> "07"
                    "August" -> "08"
                    "September" -> "09"
                    "October" -> "10"
                    "November" -> "11"
                    else -> "12"
                }

            val currentYear =
                Calendar.getInstance()
                    .get(Calendar.YEAR)
                    .toString()

            dayMap.clear()

            var countedDays = 0
            var totalAmount = 0

            allCalls.forEach { record ->

                val year =
                    record.callDate.substring(6, 10)

                if (
                    record.callDate.substring(3, 5) == monthNumber &&
                    year == currentYear
                ) {

                    android.util.Log.d(
                        "DATABASE_DATE",
                        record.callDate
                    )

                    dayMap[record.callDate] =
                        Pair(
                            record.morning,
                            record.evening
                        )

                    if (record.countedDay) {

                        countedDays++
                        totalAmount += record.amount
                    }
                }
            }

            runOnUiThread {

                txtMonth.text =
                    "Month : $month"

                txtDays.text =
                    countedDays.toString()

                txtAmount.text =
                    "₹$totalAmount"

                calendarView.notifyCalendarChanged()
            }
        }
    }
    private fun exportPdf() {

        lifecycleScope.launch(Dispatchers.IO) {

            try {

                val database =
                    AppDatabase.getDatabase(
                        this@MonthlyHistoryActivity
                    )

                val allCalls =
                    database.callDao()
                        .getAllCalls()

                val selectedMonth =
                    btnSelectMonth.text.toString()

                val monthNumber =
                    when (selectedMonth) {
                        "January" -> "01"
                        "February" -> "02"
                        "March" -> "03"
                        "April" -> "04"
                        "May" -> "05"
                        "June" -> "06"
                        "July" -> "07"
                        "August" -> "08"
                        "September" -> "09"
                        "October" -> "10"
                        "November" -> "11"
                        else -> "12"
                    }

                val currentYear =
                    Calendar.getInstance()
                        .get(Calendar.YEAR)
                        .toString()

                val monthCalls =
                    allCalls.filter {

                        val monthMatch =
                            it.callDate.substring(3, 5) ==
                                    monthNumber

                        val yearMatch =
                            it.callDate.substring(6, 10) ==
                                    currentYear

                        monthMatch && yearMatch
                    }

                val file =
                    MonthlyReport.generatePdf(
                        this@MonthlyHistoryActivity,
                        "$selectedMonth $currentYear",
                        monthCalls
                    )

                runOnUiThread {

                    Toast.makeText(
                        this@MonthlyHistoryActivity,
                        "PDF Created Successfully",
                        Toast.LENGTH_LONG
                    ).show()

                    sharePdf(file)
                }

            } catch (e: Exception) {

                runOnUiThread {

                    Toast.makeText(
                        this@MonthlyHistoryActivity,
                        "ERROR: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                e.printStackTrace()
            }
        }
    }
    private fun sharePdf(pdfFile: File) {

        try {

            val uri =
                FileProvider.getUriForFile(
                    this,
                    "${packageName}.provider",
                    pdfFile
                )

            val intent =
                Intent(Intent.ACTION_SEND)

            intent.type = "application/pdf"

            intent.putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            startActivity(
                Intent.createChooser(
                    intent,
                    "Share Report"
                )
            )

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Share Error: ${e.message}",
                Toast.LENGTH_LONG
            ).show()

            e.printStackTrace()
        }
    }
}