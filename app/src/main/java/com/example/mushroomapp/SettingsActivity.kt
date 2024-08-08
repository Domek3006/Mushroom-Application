package com.example.mushroomapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial


class SettingsActivity : AppCompatActivity() {
    private lateinit var settings : MenuItem
    private var userId: Int = -1
    private val PREF_DARK_THEME = "dark_theme"

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
        setContentView(R.layout.activity_settings)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerSettings)
        val topAppBar = findViewById<MaterialToolbar>(R.id.settingsAppBar)
        val navView = findViewById<NavigationView>(R.id.settingsNavView)
        val themeButton = findViewById<SwitchMaterial>(R.id.themeSwitch)
        val menu = navView.menu
        val home = menu.getItem(0)
        val atlas = menu.getItem(1)
        val history = menu.getItem(2)
        settings = menu.getItem(3)
        val about = menu.getItem(4)
        val logout = menu.getItem(5)


        settings.isChecked = true
        themeButton.isChecked = useDarkTheme;
        themeButton.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                val editor = getSharedPreferences("mushroomapp", MODE_PRIVATE).edit()
                editor.putBoolean(userId.toString(), true)
                editor.apply()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                val editor = getSharedPreferences("mushroomapp", MODE_PRIVATE).edit()
                editor.putBoolean(userId.toString(), false)
                editor.apply()
            }
        }

        topAppBar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val noInternet = Toast.makeText(applicationContext, "Brak dostępu do Internetu", Toast.LENGTH_LONG)

        home.setOnMenuItemClickListener {
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
            true
        }

        atlas.setOnMenuItemClickListener {
            val i = Intent(this, AtlasActivity::class.java)
            i.putExtra("logged", true)
            i.putExtra("TYPE", "Atlas")
            i.putExtra("ID", userId)
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