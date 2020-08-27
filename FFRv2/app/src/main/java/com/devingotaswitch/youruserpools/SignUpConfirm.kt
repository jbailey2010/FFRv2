package com.devingotaswitch.youruserpools

import android.content.DialogInterface
import android.content.Intent
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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.youruserpools.CUPHelper.formatException
import com.devingotaswitch.youruserpools.CUPHelper.pool

class SignUpConfirm : AppCompatActivity() {
    private var username: EditText? = null
    private var confCode: EditText? = null
    private var userName: String? = null
    private var userDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_confirm)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        val main_title = findViewById<TextView>(R.id.confirm_toolbar_title)
        main_title.text = "Confirm"
        init()
        val extraDoResend = intent.extras!!["resend"]
        if (extraDoResend != null && extraDoResend as Boolean) {
            val userName = intent.extras!!["username"] as String?
            username!!.setText(userName)
            reqConfCode()
        }
    }

    private fun init() {
        val extras = intent.extras
        if (extras != null) {
            if (extras.containsKey("name")) {
                userName = extras.getString("name")
                username = findViewById(R.id.editTextConfirmUserId)
                username!!.setText(userName)
                confCode = findViewById(R.id.editTextConfirmCode)
                confCode!!.requestFocus()
                if (extras.containsKey("destination")) {
                    val dest = extras.getString("destination")
                    val delMed = extras.getString("deliveryMed")
                    val screenSubtext = findViewById<TextView>(R.id.textViewConfirmSubtext_1)
                    if (dest != null && delMed != null && dest.isNotEmpty() && delMed.isNotEmpty()) {
                        screenSubtext.text = "A confirmation code was sent to $dest via $delMed"
                    } else {
                        screenSubtext.text = "A confirmation code was sent"
                    }
                }
            } else {
                val screenSubtext = findViewById<TextView>(R.id.textViewConfirmSubtext_1)
                screenSubtext.text = "Request for a confirmation code or confirm with the code you already have."
            }
        }
        username = findViewById(R.id.editTextConfirmUserId)
        username!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewConfirmUserIdLabel)
                    label.text = username!!.hint
                    username!!.background = ContextCompat.getDrawable(applicationContext,
                            R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewConfirmUserIdMessage)
                label.text = " "
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewConfirmUserIdLabel)
                    label.text = ""
                }
            }
        })
        confCode = findViewById(R.id.editTextConfirmCode)
        confCode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewConfirmCodeLabel)
                    label.text = confCode!!.hint
                    confCode!!.background = ContextCompat.getDrawable(applicationContext,
                            R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewConfirmCodeMessage)
                label.text = " "
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewConfirmCodeLabel)
                    label.text = ""
                }
            }
        })
        val confirm = findViewById<Button>(R.id.confirm_button)
        confirm.setOnClickListener { v: View? -> sendConfCode() }
        val reqCode = findViewById<TextView>(R.id.resend_confirm_req)
        reqCode.setOnClickListener { v: View? -> reqConfCode() }
    }

    private fun sendConfCode() {
        userName = username!!.text.toString()
        val confirmCode = confCode!!.text.toString()
        if (userName == null || userName!!.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewConfirmUserIdMessage)
            label.text = username!!.hint.toString() + " cannot be empty"
            username!!.background = ContextCompat.getDrawable(applicationContext,
                    R.drawable.text_border_error)
            return
        }
        if (confirmCode.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewConfirmCodeMessage)
            label.text = confCode!!.hint.toString() + " cannot be empty"
            confCode!!.background = ContextCompat.getDrawable(applicationContext,
                    R.drawable.text_border_error)
            return
        }
        pool!!.getUser(userName).confirmSignUpInBackground(confirmCode, true, confHandler)
    }

    private fun reqConfCode() {
        userName = username!!.text.toString()
        if (userName == null || userName!!.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewConfirmUserIdMessage)
            label.text = username!!.hint.toString() + " cannot be empty"
            username!!.background = ContextCompat.getDrawable(applicationContext,
                    R.drawable.text_border_error)
            return
        }
        pool!!.getUser(userName).resendConfirmationCodeInBackground(resendConfCodeHandler)
    }

    private val confHandler: GenericHandler = object : GenericHandler {
        override fun onSuccess() {
            showDialogMessage("Success!", "$userName has been confirmed!", true)
        }

        override fun onFailure(exception: Exception) {
            var label = findViewById<TextView>(R.id.textViewConfirmUserIdMessage)
            label.text = "Confirmation failed!"
            username!!.background = ContextCompat.getDrawable(applicationContext,
                    R.drawable.text_border_error)
            label = findViewById(R.id.textViewConfirmCodeMessage)
            label.text = "Confirmation failed!"
            confCode!!.background = ContextCompat.getDrawable(applicationContext,
                    R.drawable.text_border_error)
            showDialogMessage("Confirmation failed", formatException(exception), false)
        }
    }
    private val resendConfCodeHandler: VerificationHandler = object : VerificationHandler {
        override fun onSuccess(cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails) {
            val mainTitle = findViewById<TextView>(R.id.textViewConfirmTitle)
            mainTitle.text = "Confirm your account"
            confCode = findViewById(R.id.editTextConfirmCode)
            confCode!!.requestFocus()
            showDialogMessage("Confirmation code sent.", "Code sent to " + cognitoUserCodeDeliveryDetails.destination + " via " + cognitoUserCodeDeliveryDetails.deliveryMedium + ".", false)
        }

        override fun onFailure(exception: Exception) {
            val label = findViewById<TextView>(R.id.textViewConfirmUserIdMessage)
            label.text = "Confirmation code resend failed"
            username!!.background = ContextCompat.getDrawable(applicationContext,
                    R.drawable.text_border_error)
            showDialogMessage("Confirmation code request has failed", formatException(exception), false)
        }
    }

    private fun showDialogMessage(title: String, body: String, exitActivity: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(body).setNeutralButton("OK") { _: DialogInterface?, _: Int ->
            try {
                userDialog!!.dismiss()
                if (exitActivity) {
                    exit()
                }
            } catch (e: Exception) {
                exit()
            }
        }
        userDialog = builder.create()
        userDialog!!.show()
    }

    private fun exit() {
        val intent = Intent()
        if (userName == null) userName = ""
        intent.putExtra("name", userName)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        private const val TAG = "SignUpConfirm"
    }
}