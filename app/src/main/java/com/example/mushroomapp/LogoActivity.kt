package com.example.mushroomapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.example.mushroomapp.database.DBConnector
import com.example.mushroomapp.database.DBHelper
import java.lang.Thread.sleep

class LogoActivity : AppCompatActivity() {

    private val db = DBHelper(this)

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
        setContentView(R.layout.activity_logo)

        //db.dropAll(this)

        Thread {

            if(!db.checkDB(this)){
                DBConnector.getAllMushrooms(DBConnector()).forEach{
                    db.addMushroom(it)
                }
            }

            sleep(2000)

            if(!preferences.getBoolean("LOGGED", false)){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                if(!networkAvailable(this)){
                    val intent = Intent(this, AtlasActivity::class.java)
                    intent.putExtra("TYPE", "Atlas")
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }.start()



    }

    private fun networkAvailable(context: Context): Boolean {
        var result: Boolean
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }
}