package com.pasiflonet.mobile.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pasiflonet.mobile.R

class DetailsActivity : AppCompatActivity() {

    private var tvStatus: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Optional status view (we will add it to XML if missing)
        tvStatus = findViewById(R.id.tvStatus)
        tvStatus?.text = "Ready"
    }
}
