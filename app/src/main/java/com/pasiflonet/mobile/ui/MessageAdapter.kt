package com.pasiflonet.mobile.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pasiflonet.mobile.R
import com.pasiflonet.mobile.model.MessageRow
import com.pasiflonet.mobile.util.ThumbBind

class MessageAdapter(
    private val items: MutableList<MessageRow> = mutableListOf()
) : RecyclerView.Adapter<MessageAdapter.VH>() {

    fun submit(newItems: List<MessageRow>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iv: ImageView = itemView.findViewById(R.id.ivThumb)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvSub: TextView = itemView.findViewById(R.id.tvSub)

        fun bind(row: MessageRow) {
            tvTitle.text = "chatId=${row.chatId}"
            tvSub.text = row.text ?: ""

            // ✅ minithumbnail placeholder -> ✅ sharp thumbnail when downloaded
            ThumbBind.bindPreview(iv, row.miniThumbBase64, row.thumbLocalPath, row.thumbFileId)
        }
    }
}
