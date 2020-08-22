package com.devingotaswitch.youruserpools

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.andrognito.flashbar.Flashbar
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.hideKeyboard
import com.devingotaswitch.youruserpools.CUPHelper.currUser
import com.devingotaswitch.youruserpools.CUPHelper.formatException
import com.devingotaswitch.youruserpools.CUPHelper.pool
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ChangePasswordActivity : AppCompatActivity() {
    private var currPassword: EditText? = null
    private var newPassword: EditText? = null
    private var userDialog: AlertDialog? = null
    private var waitDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val toolbar = findViewById<Toolbar>(R.id.toolbarChangePass)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        val main_title = findViewById<TextView>(R.id.change_password_toolbar_title)
        main_title.text = "Change password"
        init()
    }

    private fun init() {
        currPassword = findViewById(R.id.editTextChangePassCurrPass)
        currPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewChangePassCurrPassLabel)
                    label.text = currPassword!!.hint
                    currPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewChangePassCurrPassMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewChangePassCurrPassLabel)
                    label.text = ""
                }
            }
        })
        newPassword = findViewById(R.id.editTextChangePassNewPass)
        newPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewChangePassNewPassLabel)
                    label.text = newPassword!!.hint
                    newPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewChangePassNewPassMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewChangePassNewPassLabel)
                    label.text = ""
                }
            }
        })
        val changeButton = findViewById<Button>(R.id.change_pass_button)
        changeButton.setOnClickListener { changePassword() }
    }

    private fun changePassword() {
        val cPass = currPassword!!.text.toString()
        if (cPass.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewChangePassCurrPassMessage)
            label.text = currPassword!!.hint.toString() + " cannot be empty"
            currPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            return
        }
        val nPass = newPassword!!.text.toString()
        if (nPass.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewChangePassNewPassMessage)
            label.text = newPassword!!.hint.toString() + " cannot be empty"
            newPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            return
        }
        showWaitDialog()
        pool!!.getUser(currUser).changePasswordInBackground(cPass, nPass, callback)
    }

    private val callback: GenericHandler = object : GenericHandler {
        override fun onSuccess() {
            closeWaitDialog()
            val act: Activity = this@ChangePasswordActivity
            hideKeyboard(act)
            generateTextOnlyFlashbar(act, "Success!", "Password changed", Flashbar.Gravity.BOTTOM).show()
            clearInput()
        }

        override fun onFailure(exception: Exception) {
            closeWaitDialog()
            newPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            currPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            showDialogMessage(formatException(exception))
        }
    }

    private fun clearInput() {
        currPassword!!.setText("")
        newPassword!!.setText("")
    }

    private fun showDialogMessage(body: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Password change failed").setMessage(body).setNeutralButton("OK") {
            _: DialogInterface?, _: Int ->
            try {
                userDialog!!.dismiss()
            } catch (e: Exception) {
                onBackPressed()
            }
        }
        userDialog = builder.create()
        userDialog!!.show()
    }

    private fun showWaitDialog() {
        closeWaitDialog()
        waitDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Changing password...")
                .create()
        waitDialog!!.show()
    }

    private fun closeWaitDialog() {
        try {
            waitDialog!!.dismiss()
        } catch (e: Exception) {
            // Wait dialog is already closed or does not exist
        }
    }
}