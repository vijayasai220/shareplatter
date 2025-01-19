package com.example.shareplate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.EaseOutCirc
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.example.shareplate.auth.AuthActivity
import com.example.shareplate.utils.AppwriteService
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.launch

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize AppwriteService
        AppwriteService.initialize(this)
        
        setContent {
            SplashScreenContent { 
                // Check for existing session
                lifecycleScope.launch {
                    try {
                        // Try to get current session
                        AppwriteService.getAccount().get()
                        // If successful, user is logged in, navigate to HomePage
                        startActivity(Intent(this@SplashScreen, HomePage::class.java))
                    } catch (e: AppwriteException) {
                        // If failed, user needs to log in
                        startActivity(Intent(this@SplashScreen, AuthActivity::class.java))
                    }
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreenContent(onTimeout: () -> Unit) {
    var isTextVisible by remember { mutableStateOf(false) }

    val textAlpha by animateFloatAsState(
        targetValue = if (isTextVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "text_alpha"
    )

    LaunchedEffect(key1 = true) {
        isTextVisible = true
        delay(2000)
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF222F21)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(170.dp)
                    .align(Alignment.Center)
            )

            Text(
                text = "SharePlate",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
                    .alpha(textAlpha),
                color = Color.White
            )
        }
    }
}