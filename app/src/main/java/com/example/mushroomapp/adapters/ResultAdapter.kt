package com.example.mushroomapp.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomapp.R
import com.example.mushroomapp.modal.InnerMushroom

class ResultAdapter(private val onClick: (InnerMushroom) -> Unit, private var mMushrooms: List<InnerMushroom>, private var mProbabilities: DoubleArray) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {
    private var iterator: Int = 0
    inner class ResultViewHolder(itemView: View, val onClick: (InnerMushroom) -> Unit) : RecyclerView.ViewHolder(itemView) {

        private val speciesText = itemView.findViewById<TextView>(R.id.mushroomCardText)
        private val probabilityText = itemView.findViewById<TextView>(R.id.mushroomCardProbabilityText)
        private val mushroomImage = itemView.findViewById<ImageView>(R.id.mushroomCardImg)
        private var currMushroom : InnerMushroom? = null


        init {
            itemView.setOnClickListener {
                currMushroom?.let {
                    onClick(it)
                }
            }
        }

        fun bind(mushroom: InnerMushroom, mProbabilities: DoubleArray) {
            currMushroom = mushroom

            speciesText.text = currMushroom!!.species
            probabilityText.text = mProbabilities[iterator].toString() + "%"
            iterator += 1
            mushroomImage.setImageBitmap(Bitmap.createScaledBitmap(currMushroom!!.iconImg!!, 250, 250, false))
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.classified_mushroom_card, parent, false)
        return ResultViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(mMushrooms[position], mProbabilities)
    }

    override fun getItemCount(): Int {
        return mMushrooms.size
    }
}