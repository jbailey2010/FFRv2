package com.devingotaswitch.youruserpools

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler
import com.andrognito.flashbar.Flashbar
import com.andrognito.flashbar.Flashbar.OnActionTapListener
import com.devingotaswitch.ffrv2.R
import com.devingotaswitch.rankings.RankingsHome
import com.devingotaswitch.utils.FlashbarFactory.generateInfiniteFlashbarWithAction
import com.devingotaswitch.utils.FlashbarFactory.generateTextOnlyFlashbar
import com.devingotaswitch.utils.GeneralUtils.confirmInternet
import com.devingotaswitch.youruserpools.CUPHelper.formatException
import com.devingotaswitch.youruserpools.CUPHelper.init
import com.devingotaswitch.youruserpools.CUPHelper.pool
import com.devingotaswitch.youruserpools.CUPHelper.setUser
import com.devingotaswitch.youruserpools.CUPHelper.userAttributesForFirstTimeLogin
import com.devingotaswitch.youruserpools.RegisterUser
import com.devingotaswitch.youruserpools.SignUpConfirm
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var userDialog: AlertDialog? = null
    private var waitDialog: AlertDialog? = null

    // Screen fields
    private var inUsername: EditText? = null
    private var inPassword: EditText? = null
    private var titleView: TextView? = null
    private var toolbar: Toolbar? = null

    //Continuations
    private var forgotPasswordContinuation: ForgotPasswordContinuation? = null
    private val newPasswordContinuation: NewPasswordContinuation? = null

    // User Details
    private var username: String? = null
    private var password: String? = null

    // Mandatory overrides first
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Set toolbar for this screen
        toolbar = findViewById(R.id.main_toolbar)
        toolbar!!.title = ""
        titleView = findViewById(R.id.main_toolbar_title)
        setSupportActionBar(toolbar)

        // Initialize application
        init(applicationContext)
        setDisplayForLoading()
        initApp()
        findCurrent()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 ->                 // Register user
                if (resultCode == RESULT_OK) {
                    val name = data!!.getStringExtra("name")
                    if (name!!.isNotEmpty()) {
                        inUsername!!.setText(name)
                        inPassword!!.setText("")
                        inPassword!!.requestFocus()
                    }
                    val userPasswd = data.getStringExtra("password")
                    if (userPasswd!!.isNotEmpty()) {
                        inPassword!!.setText(userPasswd)
                    }
                    if (name.isNotEmpty() && userPasswd.isNotEmpty()) {
                        // We have the user details, so sign in!
                        username = name
                        password = userPasswd
                        pool!!.getUser(username).getSessionInBackground(FFRAuthHandler(false))
                    }
                }
            2 ->                 // Confirm register user
                if (resultCode == RESULT_OK) {
                    val name = data!!.getStringExtra("name")
                    if (name!!.isNotEmpty()) {
                        inUsername!!.setText(name)
                        inPassword!!.setText("")
                        inPassword!!.requestFocus()
                    }
                }
            3 ->                 // Forgot password
                if (resultCode == RESULT_OK) {
                    val newPass = data!!.getStringExtra("newPass")
                    val code = data.getStringExtra("code")
                    if (newPass != null && code != null) {
                        if (newPass.isNotEmpty() && code.isNotEmpty()) {
                            showWaitDialog("Setting new password...")
                            forgotPasswordContinuation!!.setPassword(newPass)
                            forgotPasswordContinuation!!.setVerificationCode(code)
                            forgotPasswordContinuation!!.continueTask()
                        }
                    }
                }
            4 ->                 // User
                if (resultCode == RESULT_OK) {
                    clearInput()
                    val name = data!!.getStringExtra("TODO")
                    if (name != null) {
                        if (name.isNotEmpty()) {
                            onBackPressed()
                        }
                    }
                }
            6 -> {
                //New password
                closeWaitDialog()
                var continueSignIn = false
                if (resultCode == RESULT_OK) {
                    continueSignIn = data!!.getBooleanExtra("continueSignIn", false)
                }
                if (continueSignIn) {
                    continueWithFirstTimeSignIn()
                }
            }
        }
    }

    // App methods
    // Register user - start process
    fun signUp(view: View?) {
        signUpNewUser()
    }

    // Login if a user is already present
    fun logIn(view: View?) {
        signInUser()
    }

    // Forgot password processing
    fun forgotPassword(view: View?) {
        forgotpasswordUser()
    }

    fun confirmSignUp(view: View?) {
        confirmUser(false)
    }

    private fun signUpNewUser() {
        val registerActivity = Intent(this, RegisterUser::class.java)
        startActivityForResult(registerActivity, 1)
    }

    private fun signInUser() {
        username = inUsername!!.text.toString()
        if (username == null || username!!.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewUserIdMessage)
            label.text = inUsername!!.hint.toString() + " cannot be empty"
            inUsername!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            return
        }
        setUser(username)
        password = inPassword!!.text.toString()
        if (password == null || password!!.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewUserPasswordMessage)
            label.text = inPassword!!.hint.toString() + " cannot be empty"
            inPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            return
        }
        showWaitDialog("Signing in...")
        pool!!.getUser(username).getSessionInBackground(FFRAuthHandler(false))
    }

    private fun forgotpasswordUser() {
        username = inUsername!!.text.toString()
        if (username == null) {
            val label = findViewById<TextView>(R.id.textViewUserIdMessage)
            label.text = inUsername!!.hint.toString() + " cannot be empty"
            inUsername!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            return
        }
        if (username!!.isEmpty()) {
            val label = findViewById<TextView>(R.id.textViewUserIdMessage)
            label.text = inUsername!!.hint.toString() + " cannot be empty"
            inUsername!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            return
        }
        showWaitDialog("")
        pool!!.getUser(username).forgotPasswordInBackground(forgotPasswordHandler)
    }

    private fun getForgotPasswordCode(forgotPasswordContinuation: ForgotPasswordContinuation) {
        this.forgotPasswordContinuation = forgotPasswordContinuation
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        intent.putExtra("destination", forgotPasswordContinuation.parameters.destination)
        intent.putExtra("deliveryMed", forgotPasswordContinuation.parameters.deliveryMedium)
        startActivityForResult(intent, 3)
    }

    private fun continueWithFirstTimeSignIn() {
        val newAttributes = userAttributesForFirstTimeLogin
        if (newAttributes != null) {
            for ((key, value) in newAttributes) {
                Log.e(TAG, String.format("Adding attribute: %s, %s", key, value))
                newPasswordContinuation!!.setUserAttribute(key, value)
            }
        }
        try {
            newPasswordContinuation!!.continueTask()
        } catch (e: Exception) {
            closeWaitDialog()
            var label = findViewById<TextView>(R.id.textViewUserIdMessage)
            label.text = "Sign-in failed"
            inPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            label = findViewById(R.id.textViewUserIdMessage)
            label.text = "Sign-in failed"
            inUsername!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            showDialogMessage("Sign-in failed", formatException(e))
        }
    }

    private fun confirmUser(doResend: Boolean) {
        val confirmActivity = Intent(this, SignUpConfirm::class.java)
        confirmActivity.putExtra("source", "main")
        confirmActivity.putExtra("resend", doResend)
        confirmActivity.putExtra("username", username)
        startActivityForResult(confirmActivity, 2)
    }

    private fun launchRankings() {
        val rankingsActivity = Intent(this, RankingsHome::class.java)
        rankingsActivity.putExtra("name", username)
        startActivityForResult(rankingsActivity, 4)
    }

    private fun findCurrent() {
        val user = pool!!.currentUser
        username = user.userId
        if (username != null) {
            setUser(username)
            inUsername!!.setText(user.userId)
            user.getSessionInBackground(FFRAuthHandler(true))
        } else {
            setDisplayForSignIn()
        }
    }

    private fun getUserAuthentication(continuation: AuthenticationContinuation, username: String?) {
        if (username != null) {
            this.username = username
            setUser(username)
        }
        if (password == null) {
            inUsername!!.setText(username)
            password = inPassword!!.text.toString()
            if (password == null) {
                val label = findViewById<TextView>(R.id.textViewUserPasswordMessage)
                label.text = inPassword!!.hint.toString() + " enter password"
                inPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
                return
            }
            if (password!!.isEmpty()) {
                val label = findViewById<TextView>(R.id.textViewUserPasswordMessage)
                label.text = inPassword!!.hint.toString() + " enter password"
                inPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
                return
            }
        }
        val authenticationDetails = AuthenticationDetails(this.username, password, null)
        continuation.setAuthenticationDetails(authenticationDetails)
        continuation.continueTask()
    }

    // initialize app
    private fun initApp() {
        inUsername = findViewById(R.id.editTextUserId)
        inUsername!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewUserIdLabel)
                    label.setText(R.string.Username)
                    inUsername!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewUserIdMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewUserIdLabel)
                    label.text = ""
                }
            }
        })
        inPassword = findViewById(R.id.editTextUserPassword)
        inPassword!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewUserPasswordLabel)
                    label.setText(R.string.Password)
                    inPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
                }
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val label = findViewById<TextView>(R.id.textViewUserPasswordMessage)
                label.text = ""
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    val label = findViewById<TextView>(R.id.textViewUserPasswordLabel)
                    label.text = ""
                }
            }
        })
    }

    // Callbacks
    private val forgotPasswordHandler: ForgotPasswordHandler = object : ForgotPasswordHandler {
        override fun onSuccess() {
            closeWaitDialog()
            showDialogMessage("Password successfully changed!", "")
            inPassword!!.setText("")
            inPassword!!.requestFocus()
        }

        override fun getResetCode(forgotPasswordContinuation: ForgotPasswordContinuation) {
            closeWaitDialog()
            getForgotPasswordCode(forgotPasswordContinuation)
        }

        override fun onFailure(e: Exception) {
            closeWaitDialog()
            showDialogMessage("Forgot password failed", formatException(e))
        }
    }

    private inner class FFRAuthHandler(private val isRefresh: Boolean) : AuthenticationHandler {
        override fun onSuccess(cognitoUserSession: CognitoUserSession, device: CognitoDevice?) {
            Log.i(TAG, "Auth Success")
            closeWaitDialog()
            launchRankings()
        }

        override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, username: String) {
            closeWaitDialog()
            Locale.setDefault(Locale.US)
            getUserAuthentication(authenticationContinuation, username)
            setDisplayForSignIn()
        }

        override fun getMFACode(multiFactorAuthenticationContinuation: MultiFactorAuthenticationContinuation) {
            // Doesn't happen
            closeWaitDialog()
        }

        override fun authenticationChallenge(continuation: ChallengeContinuation) {
            Log.d(TAG, "Authentication challenge thrown, should never happen.")
            setDisplayForSignIn()
        }

        override fun onFailure(e: Exception) {
            closeWaitDialog()
            var label = findViewById<TextView>(R.id.textViewUserIdMessage)
            label.text = "Sign-in failed"
            inPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            label = findViewById(R.id.textViewUserIdMessage)
            label.text = "Sign-in failed"
            inUsername!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_error)
            if (!confirmInternet(application)) {
                notifyUserOnInternet(isRefresh)
            } else {
                showDialogMessage("Sign-in failed", formatException(e))
                setDisplayForSignIn()
            }
        }
    }

    private fun notifyUserOnInternet(isRefresh: Boolean) {
        val snackBarListener = object : OnActionTapListener {
            override fun onActionTapped(bar: Flashbar) {
                findCurrent()
            }
        }
        if (isRefresh) {
            generateInfiniteFlashbarWithAction(this, "No can do", "No internet connection", Flashbar.Gravity.BOTTOM,
                    snackBarListener, "Re-connect")
                    .show()
        } else {
            generateTextOnlyFlashbar(this, "No can do", "No internet connection", Flashbar.Gravity.TOP)
                    .show()
        }
    }

    private fun setDisplayForSignIn() {
        val buffer = findViewById<RelativeLayout>(R.id.rankings_splash_buffer)
        buffer.visibility = View.GONE
        val fields = findViewById<RelativeLayout>(R.id.rankings_splash_bottom)
        fields.visibility = View.VISIBLE
        titleView!!.text = "Welcome"
        toolbar!!.visibility = View.VISIBLE
    }

    private fun setDisplayForLoading() {
        val buffer = findViewById<RelativeLayout>(R.id.rankings_splash_buffer)
        buffer.visibility = View.VISIBLE
        val fields = findViewById<RelativeLayout>(R.id.rankings_splash_bottom)
        fields.visibility = View.GONE
        titleView!!.text = ""
        toolbar!!.visibility = View.INVISIBLE
    }

    private fun clearInput() {
        if (inUsername == null) {
            inUsername = findViewById(R.id.editTextUserId)
        }
        if (inPassword == null) {
            inPassword = findViewById(R.id.editTextUserPassword)
        }
        inUsername!!.setText("")
        inUsername!!.requestFocus()
        inUsername!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
        inPassword!!.setText("")
        inPassword!!.background = ContextCompat.getDrawable(applicationContext, R.drawable.text_border_selector)
    }

    private fun showWaitDialog(message: String) {
        closeWaitDialog()
        waitDialog = MaterialAlertDialogBuilder(this)
                .setTitle(message)
                .create()
        waitDialog!!.show()
    }

    private fun showDialogMessage(title: String, body: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title).setMessage(body).setNeutralButton("OK") { _: DialogInterface?, _: Int ->
            try {
                userDialog!!.dismiss()
                if (body.toLowerCase(Locale.US).contains("confirmed")) {
                    confirmUser(true)
                }
            } catch (ignored: Exception) {
            }
        }
        userDialog = builder.create()
        userDialog!!.show()
    }

    private fun closeWaitDialog() {
        try {
            waitDialog!!.dismiss()
        } catch (e: Exception) {
            //
        }
    }
}