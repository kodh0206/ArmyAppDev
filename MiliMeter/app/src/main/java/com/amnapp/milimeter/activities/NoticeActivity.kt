package com.amnapp.milimeter.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amnapp.milimeter.databinding.ActivityNoticeBinding

class NoticeActivity : AppCompatActivity() {

    val binding by lazy { ActivityNoticeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 창닫기
        binding.cancelBt.setOnClickListener {
            val intentBack = Intent(this, SettingActivity::class.java)
            startActivity(intentBack)
        }

        binding.noticeCheckSt.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked == true) {
                val intentTimeSet = Intent(this, TimeSettingActivity::class.java)
                startActivity(intentTimeSet)
            }
        }

    }
}