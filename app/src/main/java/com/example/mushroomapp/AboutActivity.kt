package com.example.mushroomapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial

class AboutActivity : AppCompatActivity() {

    private lateinit var about : MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {

        val preferences = getSharedPreferences("mushroomapp", MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerAbout)
        val topAppBar = findViewById<MaterialToolbar>(R.id.aboutAppBar)
        val navView = findViewById<NavigationView>(R.id.aboutNavView)
        val menu = navView.menu
        val home = menu.getItem(0)
        val atlas = menu.getItem(1)
        val history = menu.getItem(2)
        val settings = menu.getItem(3)
        about = menu.getItem(4)
        val logout = menu.getItem(5)

        about.isChecked = true

        val noInternet = Toast.makeText(applicationContext, "Brak dostępu do Internetu", Toast.LENGTH_LONG)

        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        home.setOnMenuItemClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
            true
        }

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