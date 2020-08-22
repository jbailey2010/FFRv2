package com.devingotaswitch.youruserpools

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.devingotaswitch.ffrv2.R

class ForgotPasswordActivity : AppCompatActivity() {
    private var passwordInput: EditText? = null
    private var codeInput: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { exit(null, null) }
        val main_title = findViewById<TextView>(R.id.forgot_password_toolbar_title)
        main_title.text = "Forgot password"
        init()
    }

    fun forgotPassword(view: View?) {
        code
    }

    private fun init() {
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("destination")) {
                val dest = extras.getString("destination")
                val delMed = extras.getString("deliveryMed")
                val message = findViewById<TextView>(R.id.textViewForgotPasswordMessage)
                val textToDisplay = "Code to set a new password was sent to $dest via $delMed"
                message.text = textToDisplay
            }
        }
        passwordInput = findViewById(R.id.editTextForgotPasswordPass)
        passwordInput!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewForgotPasswordUserIdLabel)
                    label.text = passwordInput!!.hint
                    passwordInput!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewForgotPasswordUserIdMessage)
                label.text = " "
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewForgotPasswordUserIdLabel)
                    label.text = ""
                }
            }
        })
        codeInput = findViewById(R.id.editTextForgotPasswordCode)
        codeInput!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewForgotPasswordCodeLabel)
                    label.text = codeInput!!.hint
                    codeInput!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewForgotPasswordCodeMessage)
                label.text = " "
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewForgotPasswordCodeLabel)
                    label.text = ""
                }
            }
        })
    }

    private val code: Unit
        get() {
            val newPassword = passwordInput!!.text.toString()
            if (newPassword.isEmpty()) {
                val label = findViewById<TextView>(R.id.textViewForgotPasswordUserIdMessage)
                label.text = passwordInput!!.hint.toString() + " cannot be empty"
                passwordInput!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
                return
            }
            val verCode = codeInput!!.text.toString()
            if (verCode.isEmpty()) {
                val label = findViewById<TextView>(R.id.textViewForgotPasswordCodeMessage)
                label.text = codeInput!!.hint.toString() + " cannot be empty"
                codeInput!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
                return
            }
            exit(newPassword, verCode)
        }

    private fun exit(newPass: String?, code: String?) {
        var newPass = newPass
        var code = code
        val intent = Intent()
        if (newPass == null || code == null) {
            newPass = ""
            code = ""
        }
        intent.putExtra("newPass", newPass)
        intent.putExtra("code", code)
        setResult(RESULT_OK, intent)
        finish()
    }
}