package com.vishwanth.callmera

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs =
            getSharedPreferences(
                "app_settings",
                MODE_PRIVATE
            )

        val edtFriend =
            findViewById<EditText>(
                R.id.edtFriendNumber
            )

        val edtCost =
            findViewById<EditText>(
                R.id.edtCost
            )

        val edtMorningStart =
            findViewById<EditText>(
                R.id.edtMorningStart
            )

        val edtMorningEnd =
            findViewById<EditText>(
                R.id.edtMorningEnd
            )

        val edtEveningStart =
            findViewById<EditText>(
                R.id.edtEveningStart
            )

        val edtEveningEnd =
            findViewById<EditText>(
                R.id.edtEveningEnd
            )

        val btnSave =
            findViewById<Button>(
                R.id.btnSaveSettings
            )

        edtFriend.setText(
            prefs.getString(
                "friend_number",
                "9398228116"
            )
        )

        edtCost.setText(
            prefs.getInt(
                "cost_per_day",
                60
            ).toString()
        )

        edtMorningStart.setText(
            prefs.getString(
                "morning_start",
                "08:00"
            )
        )

        edtMorningEnd.setText(
            prefs.getString(
                "morning_end",
                "09:00"
            )
        )

        edtEveningStart.setText(
            prefs.getString(
                "evening_start",
                "15:30"
            )
        )

        edtEveningEnd.setText(
            prefs.getString(
                "evening_end",
                "16:00"
            )
        )

        btnSave.setOnClickListener {

            prefs.edit()
                .putString(
                    "friend_number",
                    edtFriend.text.toString()
                )
                .putInt(
                    "cost_per_day",
                    edtCost.text.toString().toInt()
                )
                .putString(
                    "morning_start",
                    edtMorningStart.text.toString()
                )
                .putString(
                    "morning_end",
                    edtMorningEnd.text.toString()
                )
                .putString(
                    "evening_start",
                    edtEveningStart.text.toString()
                )
                .putString(
                    "evening_end",
                    edtEveningEnd.text.toString()
                )
                .apply()

            Toast.makeText(
                this,
                "Settings Saved",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }
}