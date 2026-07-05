package com.vishwanth.callmera.security

import android.content.Context
import java.security.MessageDigest

object SecurityManager {

    fun hashPin(pin: String): String {

        val bytes =
            MessageDigest
                .getInstance("SHA-256")
                .digest(pin.toByteArray())

        return bytes.joinToString("") {
            "%02x".format(it)
        }
    }

    fun savePin(
        context: Context,
        pin: String
    ) {

        context
            .getSharedPreferences(
                "security",
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                "pin_hash",
                hashPin(pin)
            )
            .apply()
    }

    fun verifyPin(
        context: Context,
        pin: String
    ): Boolean {

        val savedHash =
            context
                .getSharedPreferences(
                    "security",
                    Context.MODE_PRIVATE
                )
                .getString(
                    "pin_hash",
                    ""
                )

        return savedHash ==
                hashPin(pin)
    }

    fun isPinCreated(
        context: Context
    ): Boolean {

        return context
            .getSharedPreferences(
                "security",
                Context.MODE_PRIVATE
            )
            .contains("pin_hash")
    }
}