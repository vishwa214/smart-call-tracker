package com.vishwanth.callmera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vishwanth.callmera.security.SecurityManager

class LoginActivity : AppCompatActivity() {

    private lateinit var edtPin: EditText
    private lateinit var btnLogin: Button

    private var attempts = 0

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_login
        )

        edtPin =
            findViewById(R.id.edtPin)

        btnLogin =
            findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {

            val pin =
                edtPin.text
                    .toString()
                    .trim()

            if (
                SecurityManager.verifyPin(
                    this,
                    pin
                )
            ) {

                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )

                finish()

            } else {

                attempts++

                Toast.makeText(
                    this,
                    "Wrong PIN",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}