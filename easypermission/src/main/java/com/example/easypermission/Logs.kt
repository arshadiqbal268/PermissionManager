package com.example.easypermission

import android.util.Log

fun Any.LogE(message: String) {
    Log.e(this::class.java.simpleName, message)
}

fun Any.LogD(message: String) {
    Log.d(this::class.java.simpleName, message)
}

fun Any.LogI(message: String) {
    Log.i(this::class.java.simpleName, message)
}