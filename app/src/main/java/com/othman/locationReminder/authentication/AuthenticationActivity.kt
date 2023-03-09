package com.othman.locationReminder.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.othman.locationReminder.R
import com.othman.locationReminder.databinding.ActivityAuthenticationBinding
import com.othman.locationReminder.locationreminders.RemindersActivity


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null
    lateinit var binding: ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
//         TO DO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
            binding.login.setOnClickListener {
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                        listOf(
                            EmailBuilder().build(),
                            GoogleBuilder().build()
                        )
                    )
                    .build()

                startActivityForResult(signInIntent, signInCode)
            }
        } else {
            startRemindersActivity()
//          TO DO: If the user was authenticated, send him to RemindersActivity
        }
//          TO DO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signInCode && resultCode == RESULT_OK) {
            val response = IdpResponse.fromResultIntent(data)
            // User successfully signed in
            Log.w("RESPONSE",""+response?.email)
            startRemindersActivity()
        }
    }

    fun startRemindersActivity(){
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

    companion object {
        const val signInCode = 6000;
    }
}
