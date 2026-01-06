package com.pasiflonet.mobile.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: פה יהיה מסך "פרטים" אמיתי:
        // - תצוגה מקדימה
        // - טשטוש ידני (Rect overlay)
        // - לוגו מותאם אישית מהמ settings
        // - כפתור יצוא/שליחה
        val tv = TextView(this).apply {
            text = "Details (stub) - TODO: blur + logo overlay + export/send"
            textSize = 18f
        }
        setContentView(tv)
    }
}
