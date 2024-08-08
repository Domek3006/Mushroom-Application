package com.example.mushroomapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.*
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomapp.adapters.ResultAdapter
import com.example.mushroomapp.database.DBConnector
import com.example.mushroomapp.database.DBHelper
import com.example.mushroomapp.modal.InnerMushroom
import java.io.ByteArrayOutputStream
import java.sql.Date


class ResultActivity : AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    private var userId = -1

    override fun onCreate(savedInstanceState: Bundle?) {

        val preferences = getSharedPreferences("mushroomapp", MODE_PRIVATE)
        val userId = preferences.getInt("USERID", -1)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val db = DBHelper(this)

        val imageView = findViewById<ImageView>(R.id.image)
        val homeButton = findViewById<Button>(R.id.home_button)
        val addHistoryEntry = findViewById<Button>(R.id.add_history_entry)
        val makePhotoAgain = findViewById<Button>(R.id.make_photo_again)

        val imageByteArray = intent.getByteArrayExtra("imagedata")
        val imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray!!.size)
        val scaledImageBitmap = Bitmap.createScaledBitmap(imageBitmap, 180,240, false)
        val recyclerView = findViewById<RecyclerView>(R.id.resultRecycler)

        val added = Toast.makeText(applicationContext, "Dodano wpis", Toast.LENGTH_LONG)
        val notAdded = Toast.makeText(applicationContext, "Nie dodano wpisu", Toast.LENGTH_LONG)

        var ids = intent.extras!!.get("ids") as IntArray
        var mostProb = intent.extras!!.get("mostProb") as DoubleArray
        var date = intent.extras!!.get("date") as Date

        var mMushrooms = db.getClassifiedMushrooms(ids)

        var sortedMushrooms = ArrayList<InnerMushroom>()

        for (id in ids) {
            for (mushroom in mMushrooms) {
                if (mushroom.id == id) {
                    sortedMushrooms.add(mushroom)
                }
            }
        }

        val recyclerAdapter = ResultAdapter( { mushroom -> adapterOnClickAtlas(mushroom.id!!) }, sortedMushrooms, mostProb)

        recyclerView.adapter = recyclerAdapter

        imageView.setImageBitmap(scaledImageBitmap)

        val positiveButtonClick = { dialog: DialogInterface, which: Int ->
            /*Toast.makeText(applicationContext,
                "Nigdy nie bierz wyników klasyfikacji za pewne", Toast.LENGTH_SHORT).show()*/
        }

        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("UWAGA!!!")
        dialog.setMessage("NIGDY nie bierz wyników klasyfikacji za pewne!\n\nPrzed zjedzeniem grzyba zweryfikuj jego jadalność w innych źródłach!")
        dialog.setPositiveButton("Rozumiem", positiveButtonClick)
        dialog.setIcon(R.drawable.baseline_warning_24)
        dialog.show()


        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        addHistoryEntry.setOnClickListener {
            if(!isNetworkAvailable()){
                val db = DBHelper(this)
                if (db.addHistoryEntry(userId, ids[0], imageByteArray, date)) {
                    added.show()
                } else {
                    notAdded.show()
                }
            }
            else {
                if (DBConnector.addUserHistoryEntry(DBConnector(), userId, ids[0], imageByteArray, date) == 1) {
                    added.show()
                } else {
                    notAdded.show()
                }
            }
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        makePhotoAgain.setOnClickListener {
                dispatchTakePictureIntent()
        }
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val date = java.util.Date()
            val sqlDate = Date(date.time)

            val resultView = Intent (this, ResultActivity::class.java)

            val image = data!!.extras!!.get("data") as Bitmap

            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()

            resultView.putExtra("imagedata", byteArray)

            var classifier = Classifier(Utils.assetFilePath(this, "model.ptl"))

            val pred = classifier.predict(image)

            Log.d(ContentValues.TAG, pred.first.toString())
            Log.d(ContentValues.TAG, pred.second.toString())

            resultView.putExtra("ids", pred.first)
            resultView.putExtra("mostProb", pred.second)
            resultView.putExtra("userId", userId)
            resultView.putExtra("date", sqlDate)

            startActivity(resultView)
        }
    }

    private fun adapterOnClickAtlas(mushroomId: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("MUSHROOM", mushroomId)
        startActivity(intent)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }
}