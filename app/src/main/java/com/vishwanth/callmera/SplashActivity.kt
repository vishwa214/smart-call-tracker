package com.vishwanth.callmera

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.vishwanth.callmera.security.BiometricHelper
import com.vishwanth.callmera.security.SecurityManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_splash
        )

        Handler(
            Looper.getMainLooper()
        ).postDelayed({

            if (
                !SecurityManager.isPinCreated(this)
            ) {

                startActivity(
                    Intent(
                        this,
                        SetupPinActivity::class.java
                    )
                )

                finish()
                return@postDelayed
            }

            BiometricHelper.authenticate(

                this,

                onSuccess = {

                    startActivity(
                        Intent(
                            this,
                            MainActivity::class.java
                        )
                    )

                    finish()
                },

                onFailure = {

                    startActivity(
                        Intent(
                            this,
                            LoginActivity::class.java
                        )
                    )

                    finish()
                }
            )

        }, 1800)
    }
}