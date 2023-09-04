package com.example.yandexmaps.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat


object BitmapConverter {
    fun vectorToBitmap(
        context: Context,
        @DrawableRes id: Int,
    ): Bitmap {
        val vectorDrawable = ResourcesCompat.getDrawable(context.resources, id, null)!!

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return bitmap
    }
}