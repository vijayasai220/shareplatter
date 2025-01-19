package com.example.shareplate.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.shareplate.MainActivity
import com.example.shareplate.MapActivity
import com.example.shareplate.GreetingScreen
import com.example.shareplate.HomePage
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showSignIn by remember { mutableStateOf(true) }
            val scope = rememberCoroutineScope()
            
            if (showSignIn) {
                SignInScreen(
                    onSignInSuccess = { username ->
                        // Navigate to GreetingScreen immediately
                        val intent = Intent(this, GreetingScreen::class.java).apply {
                            putExtra("username", username)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onSignUpClick = {
                        showSignIn = false
                    }
                )
            } else {
                SignUpScreen(
                    onSignUpSuccess = { },
                    onSignInClick = {
                        showSignIn = true
                    }
                )
            }
        }
    }
} 