package com.example.mushroomapp

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils.replace
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mushroomapp.database.DBConnector
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import org.pytorch.LiteModuleLoader
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.sql.Date
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var home : MenuItem
    private var userId = -1
    val REQUEST_IMAGE_CAPTURE = 1
    val CAMERA_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {

        val preferences = getSharedPreferences("mushroomapp", MODE_PRIVATE)
        val userId = preferences.getInt("USERID", -1)

        var useDarkTheme = false
        if (userId != -1){
            useDarkTheme = preferences.getBoolean(userId.toString(), false)
        }

        if (useDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val card = findViewById<CardView>(R.id.mainCard)
        val fab = findViewById<FloatingActionButton>(R.id.aparatFAB)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)
        val topAppBar = findViewById<MaterialToolbar>(R.id.mainAppBar)
        val navView = findViewById<NavigationView>(R.id.navView)
        val mushImg = findViewById<ImageView>(R.id.mainCardImg)
        val mushName = findViewById<TextView>(R.id.mainCardText)
        val imgCount = findViewById<TextView>(R.id.mainSpeciesFound)
        val menu = navView.menu
        home = menu.getItem(0)
        val atlas = menu.getItem(1)
        val history = menu.getItem(2)
        val settings = menu.getItem(3)
        val about = menu.getItem(4)
        val logout = menu.getItem(5)

        home.isChecked = true

        val noInternet = Toast.makeText(applicationContext, "Brak dostępu do Internetu", Toast.LENGTH_LONG)

        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }


        val username = preferences.getString("USERNAME", "")

        topAppBar.title = "Witaj, $username!"

        atlas.setOnMenuItemClickListener {
            val i = Intent(this, AtlasActivity::class.java)
            i.putExtra("TYPE", "Atlas")
            startActivity(i)
            finish()
            true
        }

        history.setOnMenuItemClickListener {
            if(!isNetworkAvailable()){
                noInternet.show()
            }
            else {
                val i = Intent(this, AtlasActivity::class.java)
                i.putExtra("TYPE", "History")
                startActivity(i)
                finish()
            }
            true
        }

        about.setOnMenuItemClickListener {
            val i = Intent(this, AboutActivity::class.java)
            startActivity(i)
            true
        }

        settings.setOnMenuItemClickListener {
            val i = Intent(this, SettingsActivity::class.java)
            startActivity(i)
            true
        }

        logout.setOnMenuItemClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            val editor = preferences.edit()
            editor.putInt("USERID", -1)
            editor.putString("USERNAME", "")
            editor.putBoolean("LOGGED", false)
            editor.apply()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            true
        }

        fab.setOnClickListener {
            if (checkPermission()) {
                dispatchTakePictureIntent()
            }
        }

        if(!isNetworkAvailable()){
            card.isVisible = false
            card.isEnabled = false
            card.isClickable = false
            card.isFocusable = false
            imgCount.text = "Brak połączenia z Internetem"
        }
        else {
            val stats = DBConnector.getUserStats(DBConnector(), userId)

            if (stats.first != null){
                mushImg.setImageBitmap(stats.first!!.image)
                mushName.text = stats.first!!.mushroom_name
                when(stats.second) {
                    1 -> imgCount.text = imgCount.text.replace(Regex("gatunków"), "gatunek")
                    2, 3, 4 -> imgCount.text = imgCount.text.replace(Regex("gatunków"), "gatunki")
                }
                imgCount.text = imgCount.text.replace(Regex("N"), stats.second.toString())

                card.setOnClickListener {
                    val intent = Intent(this, HistoryDetailActivity::class.java)
                    intent.putExtra("ENTRYID", stats.first!!.mushroom_id)
                    intent.putExtra("ENTRYNAME", stats.first!!.mushroom_name)
                    startActivity(intent)
                }
            }
            else {
                card.isVisible = false
                card.isEnabled = false
                card.isClickable = false
                card.isFocusable = false
                imgCount.text = "Nie dodano jeszcze żadnego zdjęcia"
            }
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
            val date = Date()
            val sqlDate = Date(date.time)

            val resultView = Intent (this, ResultActivity::class.java)

            val image = data!!.extras!!.get("data") as Bitmap

            val stream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()

            resultView.putExtra("imagedata", byteArray)

            var classifier = Classifier(Utils.assetFilePath(this, "model.ptl"))

            val pred = classifier.predict(image)

            resultView.putExtra("ids", pred.first)
            resultView.putExtra("mostProb", pred.second)
            resultView.putExtra("userId", userId)
            resultView.putExtra("date", sqlDate)

            startActivity(resultView)
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        home.isChecked = true
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