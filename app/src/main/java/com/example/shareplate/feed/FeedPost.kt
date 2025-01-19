package com.example.shareplate.feed

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.shareplate.R
import com.example.shareplate.utils.AppwriteService
import kotlinx.coroutines.launch

data class FeedPost(
    val id: String,
    val userId: String,
    val username: String,
    val foodName: String,
    val imageId: String,
    val likes: Int,
    val timestamp: String,
    val isLikedByCurrentUser: Boolean = false
)

@Composable
fun FeedContent(feedService: FeedService) {
    val scope = rememberCoroutineScope()
    val posts = remember { mutableStateListOf<FeedPost>() }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Load posts
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val feedPosts = feedService.getFeedPosts()
            posts.clear()
            posts.addAll(feedPosts)
            error = null
        } catch (e: Exception) {
            error = e.message ?: "Failed to load posts"
            Log.e("FeedContent", "Error loading posts", e)
        } finally {
            isLoading = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF222F21))
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFDCE93A)
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isLoading = true
                                    error = null
                                    val feedPosts = feedService.getFeedPosts()
                                    posts.clear()
                                    posts.addAll(feedPosts)
                                } catch (e: Exception) {
                                    error = e.message ?: "Failed to load posts"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDCE93A)
                        )
                    ) {
                        Text("Retry", color = Color.Black)
                    }
                }
            }
            posts.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No posts available",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.splash_logo),
                                contentDescription = "SharePlate Logo",
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SharePlate",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(posts) { post ->
                        FeedPostCard(
                            post = post,
                            onLikeClick = {
                                scope.launch {
                                    try {
                                        val currentUser = AppwriteService.getCurrentUser()
                                        currentUser?.id?.let { userId ->
                                            if (post.isLikedByCurrentUser) {
                                                feedService.unlikePost(post.id, userId)
                                            } else {
                                                feedService.likePost(post.id, userId)
                                            }
                                            // Refresh posts after like/unlike
                                            val updatedPosts = feedService.getFeedPosts()
                                            posts.clear()
                                            posts.addAll(updatedPosts)
                                        }
                                    } catch (e: Exception) {
                                        error = e.message ?: "Failed to update like"
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedPostCard(
    post: FeedPost,
    onLikeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A392A)
        )
    ) {
        Column {
            // User info header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    tint = Color.White
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Username and food name
                Column {
                    Text(
                        text = post.username,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Donated ${post.foodName}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            
            // Food image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://cloud.appwrite.io/v1/storage/buckets/677d62110026c44f4842/files/${post.imageId}/view?project=677d5c5300122e700877")
                    .addHeader("X-Appwrite-Project", FeedService.PROJECT_ID)
                    .addHeader("X-Appwrite-Session", AppwriteService.getSessionId() ?: "")
                    .build(),
                contentDescription = "Food Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.splash_logo)
            )
            
            // Like button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (post.isLikedByCurrentUser) 
                            Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByCurrentUser) 
                            Color(0xFFDCE93A) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "${post.likes} likes",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
} 