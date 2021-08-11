package com.amnapp.milimeter.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amnapp.milimeter.R
import com.amnapp.milimeter.databinding.ActivitySpecialThemeSettingBinding

class SpecialThemeSettingActivity : AppCompatActivity() {

    val binding by lazy { ActivitySpecialThemeSettingBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 창닫기 (바로 설정창으로)
        binding.cancelIb.setOnClickListener {
            val intentBack = Intent(this, SettingActivity::class.java)
            startActivity(intentBack)
            finish()
        }
        // 뒤로가기 (테마창으로)
        binding.backIb.setOnClickListener {
            finish()
        }

    }
}