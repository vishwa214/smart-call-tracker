package com.vishwanth.callmera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vishwanth.callmera.security.SecurityManager

class SetupPinActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_setup_pin
        )

        val edtPin =
            findViewById<EditText>(
                R.id.edtPin
            )

        val btnSave =
            findViewById<Button>(
                R.id.btnSavePin
            )

        btnSave.setOnClickListener {

            val pin =
                edtPin.text
                    .toString()
                    .trim()

            if (pin.length != 8) {

                Toast.makeText(
                    this,
                    "PIN must be 8 digits",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            SecurityManager.savePin(
                this,
                pin
            )

            Toast.makeText(
                this,
                "PIN Created Successfully",
                Toast.LENGTH_SHORT
            ).show()

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()
        }
    }
}