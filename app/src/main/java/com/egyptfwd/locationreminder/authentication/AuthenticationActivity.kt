package com.egyptfwd.locationreminder.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.egyptfwd.locationreminder.R
import com.egyptfwd.locationreminder.locationreminders.RemindersActivity
import com.egyptfwd.locationreminder.databinding.ActivityAuthenticationBinding
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val authViewModel: AuthViewModel by viewModels()

    companion object {
        const val TAG = "LoginFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_authentication)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_authentication
        )

//      TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

        authViewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                AuthViewModel.AuthenticationState.INVALID_AUTHENTICATION ->
                    Toast.makeText(this,
                        this.getString(R.string.login_unsuccessful_msg),
                        Toast.LENGTH_LONG
                    ).show()
                AuthViewModel.AuthenticationState.UNAUTHENTICATED ->
                    binding.login.setOnClickListener {
                        launchSignInFlow()
                    }
//      TODO: If the user was authenticated, send him to RemindersActivity
                else ->
                    startActivity(Intent(this, RemindersActivity::class.java))
            }
        })


        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        // You must provide a custom layout XML resource and configure at least one
// provider button ID. It's important that that you set the button ID for every provider
// that you have enabled.


    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

//      TODO: a bonus is to customize the sign in flow to look nice using :

        val customLayout = AuthMethodPickerLayout
            .Builder(R.layout.custom_auth_layout)
            .setGoogleButtonId(R.id.google_btn)
            .setEmailButtonId(R.id.email_btn)
            .build()


        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        startActivityForResult(
            AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}
