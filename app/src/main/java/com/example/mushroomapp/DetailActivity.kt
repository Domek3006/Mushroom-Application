package com.example.mushroomapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.mushroomapp.database.DBHelper

class DetailActivity : AppCompatActivity() {



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {

        val preferences = getSharedPreferences("mushroomapp", MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        var mushroomId: Int = -1

        val bundle: Bundle? = intent.extras
        if (bundle != null){
            mushroomId = bundle.getInt("MUSHROOM")
        }

        val mushroomSpecies = findViewById<TextView>(R.id.detailSpecies)
        val mushroomImg = findViewById<ImageView>(R.id.detailImg)
        val mushroomSpeciesLatin = findViewById<TextView>(R.id.detailSpeciesLatin)
        val detailBackground = findViewById<LinearLayout>(R.id.detailLayout)
        val mushroomEdibility = findViewById<ImageView>(R.id.detailEdibility)

        val db = DBHelper(this)

        val currMushroom = db.findMushroom(mushroomId)

        mushroomSpecies.text = currMushroom.species
        mushroomImg.setImageBitmap(Bitmap.createScaledBitmap(currMushroom.image!!, 500, 500, false))
        mushroomSpeciesLatin.text = "Łacińska nazwa: " +  currMushroom.latin_species

        when (currMushroom.edibility) {
            "niejadalny" ->
                mushroomEdibility.setImageResource(R.drawable.ic_inedible_24)
            "jadalny" ->
                mushroomEdibility.setImageResource(R.drawable.ic_edible_24)
            "trujący" ->
            mushroomEdibility.setImageResource(R.drawable.ic_poisonous_24)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }
}