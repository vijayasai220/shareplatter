package com.example.shareplate.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shareplate.R
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.AnimatedVisibility
import io.appwrite.exceptions.AppwriteException
import com.example.shareplate.utils.AppwriteService
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onSignInSuccess: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222F21))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Logo slides in from top
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 1000, easing = EaseOutBack)
                ) + fadeIn(animationSpec = tween(1000))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Welcome text slides in from left
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 1000, delayMillis = 300)
                ) + fadeIn(
                    animationSpec = tween(1000, delayMillis = 300)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hola!",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Text(
                        text = "Welcome back, Sign in to your account",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Form fields slide in from right
            AnimatedVisibility(
                visible = visible,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 1000, delayMillis = 600)
                ) + fadeIn(
                    animationSpec = tween(1000, delayMillis = 600)
                )
            ) {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.3f)
                        )
                    )

                    TextButton(
                        onClick = { /* TODO: Implement forgot password */ },
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(vertical = 0.dp)
                    ) {
                        Text(
                            "Forget Password?",
                            color = Color(0xFFDCE93A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button and bottom text slide in from bottom
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 1000, delayMillis = 900)
                ) + fadeIn(
                    animationSpec = tween(1000, delayMillis = 900)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                try {
                                    // Create email session
                                    val session = AppwriteService.getAccount().createSession(
                                        userId = email,
                                        secret = password
                                    )
                                    
                                    // Get user details
                                    val user = AppwriteService.getAccount().get()
                                    
                                    isLoading = false
                                    onSignInSuccess(user.name)
                                } catch (e: AppwriteException) {
                                    isLoading = false
                                    errorMessage = e.message ?: "An error occurred during sign in"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDCE93A)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.Black)
                        } else {
                            Text(
                                "Sign In",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Not a member?",
                            color = Color.White
                        )
                        TextButton(
                            onClick = onSignUpClick,
                            contentPadding = PaddingValues(start = 4.dp)
                        ) {
                            Text(
                                "Sign Up",
                                color = Color(0xFFDCE93A)
                            )
                        }
                    }
                }
            }
        }
        
        // Add SnackbarHost
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
} 