package com.antcloud.app.common

class LoginUtils {
    fun loginFormatValidation(email: String, password: String): Int {
        if (email.trim().isNotEmpty()) {
            return if (email.length > 5) {
                if (email.contains("@")) {
                    if (password.trim().isNotEmpty()) {
                        1
                    } else {
                        5
                    }
                }
                else {
                    4
                }
            }
            else {
                3
            }
        }
        else {
            return 2
        }
    }
}