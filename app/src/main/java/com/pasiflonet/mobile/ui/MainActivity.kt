package com.pasiflonet.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val btnDetails = Button(this).apply {
            text = "פרטים (Details)"
            setOnClickListener {
                startActivity(Intent(this@MainActivity, DetailsActivity::class.java))
            }
        }

        root.addView(btnDetails)
        setContentView(root)
    }
}
