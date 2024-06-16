package com.example.trainerapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var signupRedirectText: TextView
    private lateinit var loginButton: Button
    private lateinit var forgotPassword: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupRedirectText = findViewById(R.id.signUpRedirectText)
        forgotPassword = findViewById(R.id.forgot_password)
        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val pass = loginPassword.text.toString().trim()

            if (isValidEmail(email) && !TextUtils.isEmpty(pass)) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // Close LoginActivity so user cannot go back
                    }.addOnFailureListener {
                        Toast.makeText(this, "Login Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                if (TextUtils.isEmpty(email)) {
                    loginEmail.error = "Email cannot be empty"
                } else if (!isValidEmail(email)) {
                    loginEmail.error = "Please enter a valid email"
                }

                if (TextUtils.isEmpty(pass)) {
                    loginPassword.error = "Password cannot be empty"
                }
            }
        }

        signupRedirectText.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView: View = layoutInflater.inflate(R.layout.activity_forgotpass, null)
        val emailBox = dialogView.findViewById<EditText>(R.id.emailBox)
        builder.setView(dialogView)
        val dialog = builder.create()

        dialogView.findViewById<View>(R.id.btnReset).setOnClickListener {
            val userEmail = emailBox.text.toString().trim()
            if (isValidEmail(userEmail)) {
                auth.sendPasswordResetEmail(userEmail).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Check your email for password reset instructions", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                emailBox.error = "Please enter a valid email"
            }
        }

        dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }

        dialog.show()
    }
}
