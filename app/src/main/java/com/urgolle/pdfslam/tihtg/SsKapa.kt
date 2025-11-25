package com.urgolle.pdfslam.tihtg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.urgolle.pdfslam.slap.PredaMaltor
import kotlin.jvm.java

class SsKapa:  AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, PredaMaltor::class.java))
        finish()
    }
}