package com.example.mushroomapp

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.mushroomapp.database.DBConnector
import com.example.mushroomapp.database.DBConnector.Companion

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val registerButton = findViewById<Button>(R.id.RegisterButton)
        val loginButton = findViewById<Button>(R.id.LoginButton)
        val username = findViewById<EditText>(R.id.logUsername)
        val passwd = findViewById<EditText>(R.id.logPassword)
        val atlas = findViewById<Button>(R.id.AtlasButton)

        val wrongInput = Toast.makeText(applicationContext, "Nazwa użytkownika i hasło nie mogą zawierać znaków specjalnych", Toast.LENGTH_LONG)
        val notFound = Toast.makeText(applicationContext, "Użytkownik nie znaleziony", Toast.LENGTH_LONG)
        val wrongPasswd = Toast.makeText(applicationContext, "Zły login lub hasło", Toast.LENGTH_LONG)
        val success = Toast.makeText(applicationContext, "Zalogowano", Toast.LENGTH_LONG)
        val noInternet = Toast.makeText(applicationContext, "Brak dostępu do Internetu", Toast.LENGTH_LONG)

        loginButton.setOnClickListener {
            if (isNetworkAvailable()) {
                if (username.text.toString().trim().isNotEmpty() && passwd.text.toString().trim().isNotEmpty()) {
                    if (!username.text.matches("^[a-zA-Z0-9]*$".toRegex()) || !passwd.text.matches("^[a-zA-Z0-9]*$".toRegex())) {
                        wrongInput.show()
                    } else {
                            if (Companion.checkIfUserExists(DBConnector(), username.text.toString()) == 1) {
                                val userId = Companion.checkUserPassword(DBConnector(), username.text.toString(), passwd.text.toString())
                                if (userId > -1) {
                                    success.show()
                                    val sp = getSharedPreferences("mushroomapp", MODE_PRIVATE)
                                    val editor = sp.edit()
                                    editor.putInt("USERID", userId)
                                    editor.putString("USERNAME", username.text.toString())
                                    editor.putBoolean("LOGGED", true)
                                    editor.apply()
                                    val i = Intent(this, MainActivity::class.java)
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    startActivity(i)
                                    finish()
                                } else {
                                    wrongPasswd.show()
                                }
                            } else {
                                notFound.show()
                            }
                    }
                }
            } else {
                noInternet.show()
            }
        }

        registerButton.setOnClickListener {
            if (isNetworkAvailable()) {
                val i = Intent(this, RegisterActivity::class.java)
                startActivity(i)
            } else {
                noInternet.show()
            }
        }

        atlas.setOnClickListener {
            val i = Intent(this, AtlasActivity::class.java)
            i.putExtra("TYPE", "Atlas")
            startActivity(i)
        }

    }

        override fun onBackPressed() {
            finishAndRemoveTask()
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
