package com.example.mushroomapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.mushroomapp.database.DBConnector
import com.example.mushroomapp.database.DBConnector.Companion
import com.example.mushroomapp.modal.User

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val returnToLogin = findViewById<Button>(R.id.ReturnToLogin)
        val username = findViewById<EditText>(R.id.regUsername)
        val email = findViewById<EditText>(R.id.regEmail)
        val passwd = findViewById<EditText>(R.id.regPassword)
        val passwd2 = findViewById<EditText>(R.id.regPassword2)
        val registerButton = findViewById<Button>(R.id.registerButton)

        val wrongInput = Toast.makeText(applicationContext, "Nazwa użytkownika i hasło nie mogą zawierać znaków specjalnych", Toast.LENGTH_LONG)
        val userExists = Toast.makeText(applicationContext, "Użytkownik o podanej nazwie już istnieje", Toast.LENGTH_LONG)
        val registered = Toast.makeText(applicationContext, "Zarejestrowano", Toast.LENGTH_LONG)
        val registerError = Toast.makeText(applicationContext, "Nie udało się zarejestrować", Toast.LENGTH_LONG)
        val differentPasswords = Toast.makeText(applicationContext, "Hasła nie są takie same", Toast.LENGTH_LONG)

        registerButton.setOnClickListener {
            if (username.text.toString().trim().isNotEmpty() && email.text.toString().trim().isNotEmpty()
                && passwd.text.toString().trim().isNotEmpty() && passwd2.text.toString().trim().isNotEmpty()
            ) {
                if (!username.text.matches("^[a-zA-Z0-9]*$".toRegex()) ||
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches() ||
                    !passwd.text.matches("^[a-zA-Z0-9]*$".toRegex()) ||
                    !passwd2.text.matches("^[a-zA-Z0-9]*$".toRegex())
                ) {
                    wrongInput.show()
                } else {
                    if (Companion.checkIfUserExists(DBConnector(), username.text.toString()) == 1) {
                        userExists.show()
                    } else {
                        if (passwd.text.toString() == passwd2.text.toString()) {
                            var user = User(username.text.toString(), email.text.toString(), passwd.text.toString())
                            if (Companion.addUser(DBConnector(), user) == 1) {
                                registered.show()
                                val i = Intent(this, LoginActivity::class.java)
                                startActivity(i)
                            } else {
                                registerError.show()
                            }
                        } else {
                            differentPasswords.show()
                        }
                    }
                }
            }
        }

        returnToLogin.setOnClickListener {
            super.onBackPressed()
            finish()
        }
    }
}