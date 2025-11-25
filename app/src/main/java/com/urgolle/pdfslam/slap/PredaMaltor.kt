package com.urgolle.pdfslam.slap

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.urgolle.pdfslam.R
import com.urgolle.pdfslam.databinding.ActivityPredaMaltorBinding
import com.urgolle.pdfslam.tihtg.BuglbMu

//启动页
class PredaMaltor : AppCompatActivity() {

    private val binding: ActivityPredaMaltorBinding by lazy {
        ActivityPredaMaltorBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.root.postDelayed({
            startActivity(Intent(this, BuglbMu::class.java))
            finish()
        }, 2000L)
    }
}