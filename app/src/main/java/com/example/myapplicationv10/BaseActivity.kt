package com.example.myapplicationv10

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplicationv10.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val languageCode = LocaleHelper.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply the locale when activity is created
        val languageCode = LocaleHelper.getLanguage(this)
        LocaleHelper.setLocale(this, languageCode)
    }
}