package com.example.mushroomapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomapp.modal.InnerMushroom
import com.example.mushroomapp.R

class AtlasAdapter(private val onClick: (InnerMushroom) -> Unit, private var mMushrooms: List<InnerMushroom>) : RecyclerView.Adapter<AtlasAdapter.MushroomViewHolder>(), AdaptersInterface {
    var getUserModelListFilter: List<InnerMushroom> = mMushrooms

    inner class MushroomViewHolder(itemView: View, val onClick: (InnerMushroom) -> Unit) : RecyclerView.ViewHolder(itemView) {

        private val speciesText = itemView.findViewById<TextView>(R.id.mushroomCardText)
        private val mushroomImage = itemView.findViewById<ImageView>(R.id.mushroomCardImg)
        private var currMushroom : InnerMushroom? = null

        init {
            itemView.setOnClickListener {
                currMushroom?.let {
                    onClick(it)
                }
            }
        }

        fun bind(mushroom: InnerMushroom) {
            currMushroom = mushroom

            speciesText.text = currMushroom!!.species
            mushroomImage.setImageBitmap(Bitmap.createScaledBitmap(currMushroom!!.iconImg!!, 250, 250, false))
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MushroomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mushroom_card, parent, false)
        return MushroomViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: MushroomViewHolder, position: Int) {
        holder.bind(mMushrooms[position])
    }

    override fun getItemCount(): Int {
        return mMushrooms.size
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
                    var userModels = ArrayList<InnerMushroom>()
                    for (model in getUserModelListFilter) {
                        if(model.species!!.toLowerCase().contains(searchStr)) {
                            userModels.add(model)
                        }
                    }
                    filterResults.values = userModels
                    filterResults.count = userModels.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mMushrooms = results!!.values as List<InnerMushroom>
                notifyDataSetChanged()
            }
        }
    }

}