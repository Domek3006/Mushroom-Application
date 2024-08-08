package com.example.mushroomapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomapp.R
import com.example.mushroomapp.modal.HistoryEntry

class HistoryEntryAdapter(private val onClick: (HistoryEntry) -> Unit, private var mEntries: List<HistoryEntry>) : RecyclerView.Adapter<HistoryEntryAdapter.HistoryEntryViewHolder>(), AdaptersInterface {
    var getUserModelListFilter: List<HistoryEntry> = mEntries

    inner class HistoryEntryViewHolder(itemView: View, val onClick: (HistoryEntry) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.mushroomCardText)
        private val mushroomImage = itemView.findViewById<ImageView>(R.id.mushroomCardImg)
        private var currEntry : HistoryEntry? = null

        init {
            itemView.setOnClickListener {
                currEntry?.let {
                    onClick(it)
                }
            }
        }

        fun bind(entry: HistoryEntry) {
            currEntry = entry

            nameText.text = currEntry!!.mushroom_name
            mushroomImage.setImageBitmap(Bitmap.createScaledBitmap(currEntry!!.image!!, 250, 250, false))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryEntryAdapter.HistoryEntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mushroom_card, parent, false)
        return HistoryEntryViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: HistoryEntryAdapter.HistoryEntryViewHolder, position: Int) {
        holder.bind(mEntries[position])
    }

    override fun getItemCount(): Int {
        return mEntries.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                var filterResults: FilterResults = FilterResults()
                if ((constraint == null) || (constraint.isEmpty())) {
                    filterResults.values = getUserModelListFilter
                    filterResults.count = getUserModelListFilter.size
                } else {
                    var searchStr = constraint.toString().lowercase()
                    var userModels = ArrayList<HistoryEntry>()
                    for (model in getUserModelListFilter) {
                        if(model.mushroom_name!!.toLowerCase().contains(searchStr)) {
                            userModels.add(model)
                        }
                    }
                    filterResults.values = userModels
                    filterResults.count = userModels.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mEntries = results!!.values as List<HistoryEntry>
                notifyDataSetChanged()
            }
        }
    }

}