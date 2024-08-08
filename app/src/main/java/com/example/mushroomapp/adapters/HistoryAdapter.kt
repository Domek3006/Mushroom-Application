package com.example.mushroomapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomapp.R
import com.example.mushroomapp.modal.HistoryEntry

class HistoryAdapter(/*private val onClick: (HistoryEntry) -> Unit,*/ private var mEntries: List<HistoryEntry>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View/*, val onClick: (HistoryEntry) -> Unit*/) : RecyclerView.ViewHolder(itemView) {
        private val dateText = itemView.findViewById<TextView>(R.id.historyCardDate)
        private val mushroomImage = itemView.findViewById<ImageView>(R.id.historyCardImg)
        private var currEntry : HistoryEntry? = null

        /*init {
            itemView.setOnClickListener {
                currEntry?.let {
                    onClick(it)
                }
            }
        }*/

        fun bind(entry: HistoryEntry) {
            currEntry = entry

            dateText.text = currEntry!!.date.toString()
            mushroomImage.setImageBitmap(Bitmap.createScaledBitmap(currEntry!!.image!!, 250, 250, false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryAdapter.HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.history_image_card, parent, false)
        return HistoryViewHolder(view/*, onClick*/)
    }

    override fun onBindViewHolder(holder: HistoryAdapter.HistoryViewHolder, position: Int) {
        holder.bind(mEntries[position])
    }

    override fun getItemCount(): Int {
        return mEntries.size
    }

}