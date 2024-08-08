package com.example.mushroomapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.mushroomapp.adapters.AdaptersInterface
import com.example.mushroomapp.adapters.AtlasAdapter
import com.example.mushroomapp.adapters.HistoryEntryAdapter
import com.example.mushroomapp.database.DBConnector
import com.example.mushroomapp.database.DBHelper
import com.example.mushroomapp.modal.HistoryEntry
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.io.ByteArrayOutputStream
import java.sql.Date
import kotlin.properties.Delegates
import kotlin.system.measureNanoTime

class AtlasActivity : AppCompatActivity() {
    private lateinit var atlas : MenuItem
    private lateinit var history : MenuItem
    private  var activityType : String? = "Atlas"
    private var userId: Int = -1
    private var logged by Delegates.notNull<Boolean>()
    val REQUEST_IMAGE_CAPTURE = 1
    val CAMERA_REQUEST_CODE = 101

    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = getSharedPreferences("mushroomapp", MODE_PRIVATE)
        userId = preferences.getInt("USERID", -1)
        var useDarkTheme = false
        if (userId != -1){
            useDarkTheme = preferences.getBoolean(userId.toString(), false)
        }
        if (useDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        logged = preferences.getBoolean("LOGGED", false)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_atlas)


        val recyclerView = findViewById<RecyclerView>(R.id.atlasRecycler)
        val fab = findViewById<FloatingActionButton>(R.id.atlasFAB)
        val drawerLayout = findViewById<DrawerLayout>(R.id.atlasDrawer)
        val topAppBar = findViewById<MaterialToolbar>(R.id.atlasAppBar)
        val searchBar = findViewById<SearchView>(R.id.appSearchBar)
        val aboutButton = findViewById<ImageButton>(R.id.appInfoButton)
        val navView = findViewById<NavigationView>(R.id.atlasNavView)
        val menu = navView.menu
        val home = menu.getItem(0)
        atlas = menu.getItem(1)
        history = menu.getItem(2)
        val settings = menu.getItem(3)
        val about = menu.getItem(4)
        val logout = menu.getItem(5)


        aboutButton.isVisible = false
        aboutButton.isClickable = false
        aboutButton.isEnabled = false
        aboutButton.isFocusable = false


        val bundle: Bundle? = intent.extras
        if (bundle != null){
            activityType = bundle.getString("TYPE")
        }

        if (!logged){
            fab.isClickable = false
            fab.isEnabled = false
            fab.isVisible = false
            navView.isVisible = false
            navView.isClickable = false
            navView.isEnabled = false
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            topAppBar.navigationIcon = getDrawable(R.drawable.ic_baseline_arrow_back_24)
            aboutButton.isVisible = true
            aboutButton.isClickable = true
            aboutButton.isEnabled = true
            aboutButton.isFocusable = true
            aboutButton.setImageResource(R.drawable.ic_baseline_about_24)
            if(useDarkTheme) {
                aboutButton.setBackgroundColor(R.color.green_color)
            } else {
                aboutButton.setBackgroundColor(R.color.black_color)
            }

        }

//        if (activityType!! == "Atlas") {
//            var recyclerAdapter = AtlasAdapter( { mushroom -> adapterOnClickAtlas(mushroom) }, DBConnector.getAllMushrooms(
//                DBConnector()
//            ))
//            recyclerView.adapter = recyclerAdapter
//        } else {
//            var recyclerAdapter = HistoryEntryAdapter( {entry -> adapterOnClickHistory(entry) },
//                DBConnector.getUserHistory(DBConnector(), userId))
//            recyclerView.adapter = recyclerAdapter
//        }

        val recyclerAdapter = checkAdapter(activityType!!)

        recyclerView.adapter = recyclerAdapter as RecyclerView.Adapter<*>

        recyclerView.adapter?.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        val noInternet = Toast.makeText(applicationContext, "Brak dostępu do Internetu", Toast.LENGTH_LONG)

        if (activityType == "Atlas") {
            topAppBar.title = "Atlas"
        } else {
            topAppBar.title = "Historia"
            Thread {
                addHistory(userId)
            }.start()
        }

        aboutButton.setOnClickListener {
            val i = Intent(this, AboutActivity::class.java)
            startActivity(i)
        }

        topAppBar.setNavigationOnClickListener {
            if (!logged) {
                super.onBackPressed()
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }

        }

        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                recyclerAdapter.filter.filter(query)
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                recyclerAdapter.filter.filter(newText)
                return false
            }
        })

        home.setOnMenuItemClickListener {
            val i = Intent(this, MainActivity::class.java)
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

            if (activityType == "Atlas") {
                history.setOnMenuItemClickListener {
                    val i = Intent(this, AtlasActivity::class.java)
                    i.putExtra("TYPE", "History")
                    startActivity(i)
                    true
                }
            } else {
                atlas.setOnMenuItemClickListener {
                    val i = Intent(this, AtlasActivity::class.java)
                    i.putExtra("TYPE", "Atlas")
                    startActivity(i)
                    true
                }

            }


            settings.setOnMenuItemClickListener {
                val i = Intent(this, SettingsActivity::class.java)
                startActivity(i)
                true
            }

            about.setOnMenuItemClickListener {
                val i = Intent(this, AboutActivity::class.java)
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

        }

    private fun addHistory(userId: Int) {
        val db = DBHelper(this)
        db.getHistoryEntries(userId).forEach {
            DBConnector.addUserHistoryEntry(DBConnector(), it)
        }
        db.deleteHistory(userId)
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
        if(activityType == "Atlas"){
            atlas.isChecked = true
        } else {
            history.isChecked = true
        }

    }

    private fun checkAdapter(activityType: String): AdaptersInterface {
        val db = DBHelper(this)
        return if (activityType == "Atlas") {
            val recyclerAdapter = AtlasAdapter( { mushroom -> adapterOnClickAtlas(mushroom.id!!) }, db.allMushrooms)
            recyclerAdapter
        } else {
            val recyclerAdapter = HistoryEntryAdapter( {entry -> adapterOnClickHistory(entry) },
                DBConnector.getUserHistory(DBConnector(), userId))
            recyclerAdapter
        }
    }

    private fun adapterOnClickAtlas(mushroomId: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("MUSHROOM", mushroomId)
        startActivity(intent)
    }

    private fun adapterOnClickHistory(entry: HistoryEntry) {
        val intent = Intent(this, HistoryDetailActivity::class.java)
        intent.putExtra("ENTRYID", entry.mushroom_id)
        intent.putExtra("ENTRYNAME", entry.mushroom_name)
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