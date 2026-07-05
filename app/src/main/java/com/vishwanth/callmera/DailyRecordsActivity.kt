package com.vishwanth.callmera

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vishwanth.callmera.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyRecordsActivity : AppCompatActivity() {

    private lateinit var txtRecords: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daily_records)

        txtRecords = findViewById(R.id.txtRecords)

        loadRecords()
    }

    private fun loadRecords() {

        lifecycleScope.launch(Dispatchers.IO) {

            val database =
                AppDatabase.getDatabase(
                    this@DailyRecordsActivity
                )

            val records =
                database.callDao()
                    .getAllCalls()

            val sortedRecords =
                records.sortedByDescending {

                    java.text.SimpleDateFormat(
                        "dd-MM-yyyy",
                        java.util.Locale.getDefault()
                    ).parse(it.callDate)
                }

            val builder =
                StringBuilder()

            for (record in sortedRecords) {

                builder.append(
                    """
                Date : ${record.callDate}

                Morning : ${record.morningTime}

                Evening : ${record.eveningTime}

                Counted : ${record.countedDay}

                Amount : ₹${record.amount}

                ----------------------------

                """.trimIndent()
                )

                builder.append("\n\n")
            }

            runOnUiThread {

                txtRecords.text =
                    builder.toString()
            }
        }
    }
}