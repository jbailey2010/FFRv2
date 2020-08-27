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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import com.amazonaws.services.cognitoidentityprovider.model.CodeDeliveryDetailsType
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.youruserpools.CUPHelper.formatException
import com.devingotaswitch.youruserpools.CUPHelper.getSignUpFieldsC2O
import com.devingotaswitch.youruserpools.CUPHelper.pool
import com.devingotaswitch.youruserpools.SignUpConfirm
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RegisterUser : AppCompatActivity() {
    private var username: EditText? = null
    private var password: EditText? = null
    private var givenName: EditText? = null
    private var phone: EditText? = null
    private var email: EditText? = null
    private var userDialog: AlertDialog? = null
    private var waitDialog: AlertDialog? = null
    private var usernameInput: String? = null
    private var userPasswd: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val extras = intent.extras
        if (extras != null) {
            // get back to main screen
            val value = extras.getString("TODO")
            if (value == "exit") {
                onBackPressed()
            }
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar_Register)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val main_title = findViewById<TextView>(R.id.signUp_toolbar_title)
        main_title.text = "Sign up"
        toolbar.setNavigationOnClickListener { v: View? -> onBackPressed() }
        init()
    }

    // This will create the list/form for registration
    private fun init() {
        username = findViewById(R.id.editTextRegUserId)
        username!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegUserIdLabel)
                    label.text = username!!.hint
                    username!!.background = ContextCompat.getDrawable(applicationContext,
                            R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewRegUserIdMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegUserIdLabel)
                    label.text = ""
                }
            }
        })
        //
        password = findViewById(R.id.editTextRegUserPassword)
        password!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegUserPasswordLabel)
                    label.text = password!!.hint
                    password!!.background = ContextCompat.getDrawable(applicationContext,
                            R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewUserRegPasswordMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegUserPasswordLabel)
                    label.text = ""
                }
            }
        })
        //
        givenName = findViewById(R.id.editTextRegGivenName)
        givenName!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegGivenNameLabel)
                    label.text = givenName!!.hint
                    givenName!!.background = ContextCompat.getDrawable(applicationContext,
                            R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewRegGivenNameMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegGivenNameLabel)
                    label.text = ""
                }
            }
        })
        //
        phone = findViewById(R.id.editTextRegPhone)
        phone!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegPhoneLabel)
                    label.text = phone!!.hint
                    phone!!.background = ContextCompat.getDrawable(applicationContext,
                            R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewRegPhoneMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegPhoneLabel)
                    label.text = ""
                }
            }
        })
        //
        email = findViewById(R.id.editTextRegEmail)
        email!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegEmailLabel)
                    label.text = email!!.hint
                    email!!.background = ContextCompat.getDrawable(applicationContext,
                            R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewRegEmailMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewRegEmailLabel)
                    label.text = ""
                }
            }
        })
        val signUp = findViewById<Button>(R.id.signUp)
        signUp.setOnClickListener { v: View? ->
            // Read user data and register
            val userAttributes = CognitoUserAttributes()
            usernameInput = username!!.text.toString()
            if (usernameInput == null || usernameInput!!.isEmpty()) {
                val view = findViewById<TextView>(R.id.textViewRegUserIdMessage)
                view.text = username!!.hint.toString() + " cannot be empty"
                username!!.background = ContextCompat.getDrawable(applicationContext,
                        R.drawable.text_border_error)
                return@setOnClickListener
            }
            val userpasswordInput = password!!.text.toString()
            userPasswd = userpasswordInput
            if (userpasswordInput.isEmpty()) {
                val view = findViewById<TextView>(R.id.textViewUserRegPasswordMessage)
                view.text = password!!.hint.toString() + " cannot be empty"
                password!!.background = ContextCompat.getDrawable(applicationContext,
                        R.drawable.text_border_error)
                return@setOnClickListener
            }
            val userInput = givenName!!.text.toString()
            if (userInput.isNotEmpty()) {
                userAttributes.addAttribute(getSignUpFieldsC2O()!!["Name"], userInput)
            } else {
                val view = findViewById<TextView>(R.id.textViewRegGivenNameMessage)
                view.text = givenName!!.hint.toString() + " cannot be empty"
                givenName!!.background = ContextCompat.getDrawable(applicationContext,
                        R.drawable.text_border_error)
                return@setOnClickListener
            }
            val phoneNumber = phone!!.text.toString()
            if (phoneNumber.isNotEmpty()) {
                userAttributes.addAttribute(getSignUpFieldsC2O()!!["Phone number"], phoneNumber)
            } else {
                val view = findViewById<TextView>(R.id.textViewRegPhoneMessage)
                view.text = phone!!.hint.toString() + " cannot be empty"
                phone!!.background = ContextCompat.getDrawable(applicationContext,
                        R.drawable.text_border_error)
                return@setOnClickListener
            }
            val emailAddress = email!!.text.toString()
            if (emailAddress.isNotEmpty()) {
                userAttributes.addAttribute(getSignUpFieldsC2O()!!["Email"], emailAddress)
            } else {
                val view = findViewById<TextView>(R.id.textViewRegEmailMessage)
                view.text = email!!.hint.toString() + " cannot be empty"
                email!!.background = ContextCompat.getDrawable(applicationContext,
                        R.drawable.text_border_error)
                return@setOnClickListener
            }
            showWaitDialog()
            pool!!.signUpInBackground(usernameInput, userpasswordInput,
                    userAttributes, null, signUpHandler)
        }
    }

    private val signUpHandler: SignUpHandler = object : SignUpHandler {
        override fun onSuccess(user: CognitoUser, signUpResult: SignUpResult) {
            // Check signUpConfirmationState to see if the user is already confirmed
            closeWaitDialog()
            if (signUpResult.isUserConfirmed) {
                // User is already confirmed
                showDialogMessage("Sign up successful!", "$usernameInput has been confirmed", true)
            } else {
                // User is not confirmed
                confirmSignUp(signUpResult.codeDeliveryDetails)
            }
        }

        override fun onFailure(exception: Exception) {
            closeWaitDialog()
            val label = findViewById<TextView>(R.id.textViewRegUserIdMessage)
            label.text = "Sign up failed"
            username!!.background = ContextCompat.getDrawable(applicationContext,
                    R.drawable.text_border_error)
            showDialogMessage("Sign up failed", formatException(exception), false)
        }
    }

    private fun confirmSignUp(cognitoUserCodeDeliveryDetails: CodeDeliveryDetailsType) {
        val intent = Intent(this, SignUpConfirm::class.java)
        intent.putExtra("source", "signup")
        intent.putExtra("name", usernameInput)
        intent.putExtra("destination", cognitoUserCodeDeliveryDetails.destination)
        intent.putExtra("deliveryMed", cognitoUserCodeDeliveryDetails.deliveryMedium)
        intent.putExtra("attribute", cognitoUserCodeDeliveryDetails.attributeName)
        startActivityForResult(intent, 10)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {
                var name: String? = null
                if (data!!.hasExtra("name")) {
                    name = data.getStringExtra("name")
                }
                exit(name, userPasswd)
            }
        }
    }

    private fun showDialogMessage(title: String, body: String, exit: Boolean) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(body).setNeutralButton("OK") { _: DialogInterface?, _: Int ->
            try {
                userDialog!!.dismiss()
                if (exit) {
                    exit(usernameInput)
                }
            } catch (e: Exception) {
                if (exit) {
                    exit(usernameInput)
                }
            }
        }
        userDialog = builder.create()
        userDialog!!.show()
    }

    private fun showWaitDialog() {
        closeWaitDialog()
        waitDialog = MaterialAlertDialogBuilder(this)
                .setTitle("Signing up...")
                .create()
        waitDialog!!.show()
    }

    private fun closeWaitDialog() {
        try {
            waitDialog!!.dismiss()
        } catch (e: Exception) {
            //
        }
    }

    private fun exit(uname: String?, password: String? = null) {
        var uname = uname
        var password = password
        val intent = Intent()
        if (uname == null) {
            uname = ""
        }
        if (password == null) {
            password = ""
        }
        intent.putExtra("name", uname)
        intent.putExtra("password", password)
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        private const val TAG = "RegisterUser"
    }
}