package com.example.audiocutter.functions.resultscreen.screens

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.audiocutter.R

class ResultActivity : AppCompatActivity() {
    val TAG = "giangtd"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
    }
    override fun onBackPressed() {
        super.onBackPressed()

        Log.d(TAG, "onBackPressed: ###### ")
    }
}