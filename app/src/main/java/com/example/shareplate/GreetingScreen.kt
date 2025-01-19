package com.example.shareplate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shareplate.utils.AppwriteService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*

class GreetingScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            var username by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
                // Get username from Appwrite
                val user = AppwriteService.getCurrentUser()
                username = user?.name ?: "User"
            }
            
            if (username.isNotEmpty()) {  // Only show content when username is loaded
                GreetingContent(
                    username = username,
                    onTimeout = {
                        val intent = Intent(this, HomePage::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        finish()
                    }
                )
            }
        }
    }

    // Add custom animation durations
    override fun overridePendingTransition(enterAnim: Int, exitAnim: Int) {
        super.overridePendingTransition(enterAnim, exitAnim)
        // Reduce animation duration
        window.decorView.animate().duration = 400 // Reduced from default to 400ms
    }
}

@Composable
fun GreetingContent(
    username: String,
    onTimeout: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    var thankYouText by remember { mutableStateOf("") }
    var usernameText by remember { mutableStateOf("") }
    var showMissionText by remember { mutableStateOf(false) }
    var showZeroHunger by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // First second: Typing animations
        val fullThankYouText = "Thank you!"
        for (i in fullThankYouText.indices) {
            thankYouText = fullThankYouText.substring(0, i + 1)
            delay(100)
        }
        delay(200)
        
        // Username typing
        for (i in username.indices) {
            usernameText = username.substring(0, i + 1)
            delay(100)
        }
        delay(300)
        
        // Second second: Fade-in animations
        showMissionText = true
        delay(600)
        
        // Last second: Show Zero Hunger and prepare for transition
        showZeroHunger = true
        delay(1500)
        
        // Transition to HomePage
        onTimeout()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222F21)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(5.dp))  // Reduced from 16.dp to 8.dp
            
            // Thank you text
            Text(
                text = thankYouText,
                fontSize = 25.sp,
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            
            Spacer(modifier = Modifier.height(8.dp))  // Reduced from 16.dp to 8.dp
            
            // Username
            Text(
                text = usernameText,
                fontSize = 38.sp,
                color = Color(0xFFDCE93A),
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))  // Reduced from 16.dp to 8.dp
            
            // Mission text with fade animation
            AnimatedVisibility(
                visible = showMissionText,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 800,
                        easing = LinearEasing
                    )
                )
            ) {
                Text(
                    text = "for being a part of our mission",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))  // Reduced from 16.dp to 8.dp
            
            // Zero Hunger text with fade animation
            AnimatedVisibility(
                visible = showZeroHunger,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearEasing
                    )
                )
            ) {
                Text(
                    text = "Zero Hunger",
                    fontSize = 40.sp,
                    color = Color(0xFFDCE93A),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 