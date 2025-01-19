package com.example.shareplate

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.osmdroid.config.Configuration
import android.net.Uri
import com.example.shareplate.data.FoodDonation
import io.appwrite.Client
import io.appwrite.services.Storage
import io.appwrite.services.Databases
import io.appwrite.ID
import io.appwrite.models.InputFile
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import io.appwrite.services.Account
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.shareplate.auth.AuthActivity
import com.example.shareplate.utils.AppwriteService
import io.appwrite.exceptions.AppwriteException
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.ui.graphics.ColorFilter
import kotlinx.coroutines.launch
import com.example.shareplate.feed.FeedService

class MapActivity : ComponentActivity() {
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d("Location", "Permission granted")
            }
            else -> {
                Log.d("Location", "Permission denied")
            }
        }
    }

    private lateinit var client: Client
    private lateinit var storage: Storage
    private lateinit var databases: Databases
    private lateinit var account: Account
    private lateinit var snackbarHostState: SnackbarHostState

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, getPreferences(Context.MODE_PRIVATE))

        // Initialize Appwrite
        client = Client(this)
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject("677d5c5300122e700877")
        
        storage = Storage(client)
        databases = Databases(client)
        account = Account(client)

        // Create anonymous session
        runBlocking {
            try {
                val session = account.createAnonymousSession()
                Log.d("Appwrite", "Anonymous session created: ${session.userId}")
            } catch (e: Exception) {
                Log.e("Appwrite", "Error creating anonymous session", e)
            }
        }

        requestLocationPermissions()

        setContent {
            MaterialTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                this.snackbarHostState = snackbarHostState

                NavigationDrawerContent(
                    drawerState = drawerState,
                    activity = this,
                    snackbarHostState = snackbarHostState,
                    scope = scope
                )
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    fun handleFoodDonation(donation: FoodDonation, imageUri: Uri?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Upload image if provided
                val imageId = imageUri?.let { uri ->
                    // Convert Uri to File
                    val inputStream = contentResolver.openInputStream(uri)
                    val file = File(cacheDir, "temp_image")
                    FileOutputStream(file).use { outputStream ->
                        inputStream?.copyTo(outputStream)
                    }

                    // Upload to Appwrite Storage using InputFile
                    val result = storage.createFile(
                        bucketId = "677d62110026c44f4842",
                        fileId = ID.unique(),
                        file = InputFile.fromFile(file)  // Convert File to InputFile
                    )
                    result.id
                }

                // Create donation document in database
                val donationDoc = databases.createDocument(
                    databaseId = "677d6309001da6cb4ee9",
                    collectionId = "677d6327002fc99d1ff8",
                    documentId = ID.unique(),
                    data = mapOf(
                        "food_name" to donation.food_name,
                        "serving_count" to donation.serving_count,
                        "image_id" to imageId,
                        "latitude" to donation.latitude,
                        "longitude" to donation.longitude,
                        "created_at" to System.currentTimeMillis()
                    )
                )

                // Create a feed post for this donation
                val currentUser = AppwriteService.getCurrentUser()
                if (currentUser != null && imageId != null) {
                    val feedService = FeedService(databases)
                    feedService.createPost(
                        userId = currentUser.id,
                        username = currentUser.name,
                        foodName = donation.food_name,
                        imageId = imageId,
                        servingSize = donation.serving_count
                    )
                }

                // Show success message
                withContext(Dispatchers.Main) {
                    snackbarHostState.showSnackbar(
                        message = "Food donation submitted successfully!",
                        duration = SnackbarDuration.Short
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Show error message
                withContext(Dispatchers.Main) {
                    snackbarHostState.showSnackbar(
                        message = "Error: ${e.localizedMessage ?: "Unknown error occurred"}",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    private fun checkAuthStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val user = AppwriteService.getAccount().get()
                // User is authenticated
            } catch (e: AppwriteException) {
                // User is not authenticated
                startActivity(Intent(this@MapActivity, AuthActivity::class.java))
                finish()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavigationDrawerContent(
    drawerState: DrawerState,
    activity: ComponentActivity,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    val menuItems = listOf(
        MenuItem("Home", Icons.Default.Home),
        MenuItem("SharePlate", null, painterResource(id = R.drawable.splash_logo)),
        MenuItem("Profile", Icons.Default.Person),
        MenuItem("Logout", Icons.Default.ExitToApp)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF222F21)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                menuItems.forEach { menuItem ->
                    NavigationDrawerItem(
                        icon = {
                            if (menuItem.iconPainter != null) {
                                Image(
                                    painter = menuItem.iconPainter,
                                    contentDescription = menuItem.title,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(Color.White)
                                )
                            } else if (menuItem.icon != null) {
                                Icon(
                                    imageVector = menuItem.icon,
                                    contentDescription = menuItem.title,
                                    tint = Color.White
                                )
                            }
                        },
                        label = { Text(menuItem.title) },
                        selected = when (menuItem.title) {
                            "SharePlate" -> true
                            "Home" -> false
                            "Profile" -> false
                            else -> false
                        },
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                when (menuItem.title) {
                                    "Home" -> {
                                        activity.startActivity(Intent(activity, HomePage::class.java))
                                        activity.finish()
                                    }
                                    "Profile" -> {
                                        val intent = Intent(activity, HomePage::class.java)
                                        intent.putExtra("initialScreen", "Profile")
                                        activity.startActivity(intent)
                                        activity.finish()
                                    }
                                    "Logout" -> {
                                        try {
                                            AppwriteService.getAccount().deleteSession("current")
                                            activity.startActivity(Intent(activity, AuthActivity::class.java))
                                            activity.finish()
                                        } catch (e: AppwriteException) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = Color.White,
                            unselectedTextColor = Color.White,
                            selectedContainerColor = Color(0x3DDCE93A)
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Donate Food", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF222F21)
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(activity as MapActivity)
                }
                
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    activity: ComponentActivity,
    initialScreen: String = "SharePlate Map"
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(initialScreen) }
    val username = AppwriteService.getUsername() ?: "User"

    NavigationDrawer(
        drawerState = drawerState,
        activity = activity,
        currentScreen = currentScreen,
        onScreenChange = { screen ->
            when (screen) {
                "Home" -> {
                    activity.startActivity(
                        Intent(activity, HomePage::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    activity.finish()
                }
                "Profile" -> {
                    // Navigate to HomePage with Profile screen
                    activity.startActivity(
                        Intent(activity, HomePage::class.java).apply {
                            putExtra("initialScreen", "Profile")
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                    activity.finish()
                }
                else -> currentScreen = screen
            }
            scope.launch {
                drawerState.close()
            }
        }
    ) {
        // Rest of your MapScreen content
    }
}

// Copy the MapScreen, getRoute, and decodePolyline functions from your existing MainActivity.kt
// ... (paste the rest of the map-related code here)