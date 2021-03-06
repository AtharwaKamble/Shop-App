package com.example.shopapp.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton

class SARadioButton (context: Context, attributeSet: AttributeSet): AppCompatRadioButton(context, attributeSet) {


    init {
        applyFont()
    }

    private fun applyFont() {

        val typeface: Typeface =
                Typeface.createFromAsset(context.assets, "Montserrat-Regular.ttf")
        setTypeface(typeface)
    }

}