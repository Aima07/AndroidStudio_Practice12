package id.sttbandung.practice12

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class DefaultActivity : AppCompatActivity() {
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_default)
        mAuth = FirebaseAuth.getInstance()

        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)

        loginButton.setOnClickListener {
            startActivity(Intent(this@DefaultActivity, LoginActivity::class.java))
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this@DefaultActivity, RegisterActivity::class.java))
        }
        checkUserSession()
    }
    private fun checkUserSession() {
        val currentUser: FirebaseUser? = mAuth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this@DefaultActivity, MainActivity::class.java))
            finish()
        }
    }
}