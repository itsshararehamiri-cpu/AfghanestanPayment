package com.danesh.core

import android.graphics.Bitmap

interface Printer {
    suspend fun print(bitmap: Bitmap)
}