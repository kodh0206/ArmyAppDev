package com.amnapp.milimeter.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.amnapp.milimeter.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        findViewById<Button>(R.id.debugBt).setOnClickListener {//디버그 창으로 가는 코드
            val intent = Intent(this, DebugActivity::class.java)
            startActivity(intent)
        }
    }
}