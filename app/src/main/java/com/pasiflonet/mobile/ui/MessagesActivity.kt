package com.pasiflonet.mobile.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pasiflonet.mobile.R
import com.pasiflonet.mobile.model.MessageRow

class MessagesActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private val adapter = MessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        rv = findViewById(R.id.rvMessages)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        // TODO: replace with real data from TDLib stream.
        // For now, shows empty list but UI & ThumbBind wiring is correct.
        adapter.submit(emptyList<MessageRow>())
    }
}
