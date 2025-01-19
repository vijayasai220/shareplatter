package com.example.shareplate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shareplate.auth.AuthActivity
import com.example.shareplate.feed.FeedContent
import io.appwrite.exceptions.AppwriteException
import com.example.shareplate.utils.AppwriteService
import kotlinx.coroutines.launch
import com.example.shareplate.feed.FeedService
import io.appwrite.services.Databases
import io.appwrite.Client
import androidx.compose.ui.platform.LocalContext

class HomePage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialScreen = intent.getStringExtra("initialScreen") ?: "Home"
        
        setContent {
            var username by remember { mutableStateOf("") }
            
            LaunchedEffect(Unit) {
                // Get username from Appwrite
                val user = AppwriteService.getCurrentUser()
                username = user?.name ?: "User"
            }
            
            HomeScreen(this, initialScreen = initialScreen, username = username)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activity: ComponentActivity,
    initialScreen: String = "Home",
    username: String
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(initialScreen) }

    NavigationDrawer(
        drawerState = drawerState,
        activity = activity,
        currentScreen = currentScreen,
        onScreenChange = { screen ->
            currentScreen = screen
            scope.launch {
                drawerState.close()
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen, color = Color.White) },
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
                when (currentScreen) {
                    "Home" -> HomeContent()
                    "Profile" -> ProfileContent(username)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    activity: ComponentActivity,
    currentScreen: String,
    onScreenChange: (String) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF222F21)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // SharePlate logo at the top
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Menu items
                val menuItems = listOf(
                    MenuItem("Home", Icons.Default.Home),
                    MenuItem("SharePlate", null, painterResource(id = R.drawable.splash_logo)),
                    MenuItem("Profile", Icons.Default.Person),
                    MenuItem("Logout", Icons.Default.ExitToApp)
                )

                menuItems.forEach { menuItem ->
                    NavigationDrawerItem(
                        icon = {
                            when {
                                menuItem.iconPainter != null -> {
                                    Image(
                                        painter = menuItem.iconPainter,
                                        contentDescription = menuItem.title,
                                        modifier = Modifier.size(24.dp),
                                        colorFilter = ColorFilter.tint(Color.White)
                                    )
                                }
                                menuItem.icon != null -> {
                                    Icon(
                                        imageVector = menuItem.icon,
                                        contentDescription = menuItem.title,
                                        tint = Color.White
                                    )
                                }
                            }
                        },
                        label = { Text(menuItem.title) },
                        selected = menuItem.title == currentScreen,
                        onClick = {
                            scope.launch {
                                when (menuItem.title) {
                                    "SharePlate" -> {
                                        drawerState.close()
                                        activity.startActivity(
                                            Intent(activity, MapActivity::class.java)
                                        )
                                    }
                                    "Logout" -> {
                                        drawerState.close()
                                        scope.launch {
                                            try {
                                                AppwriteService.getAccount().deleteSession("current")
                                                activity.startActivity(Intent(activity, AuthActivity::class.java))
                                                activity.finish()
                                            } catch (e: AppwriteException) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                    else -> {
                                        // Update current screen and close drawer
                                        onScreenChange(menuItem.title)
                                        drawerState.close()
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
        content()
    }
}

@Composable
fun HomeContent() {
    val client = Client(LocalContext.current)
        .setEndpoint("https://cloud.appwrite.io/v1")
        .setProject(FeedService.PROJECT_ID)
    val databases = Databases(client)
    val feedService = remember { 
        FeedService(databases)
    }
    FeedContent(feedService)
}

@Composable
fun ProfileContent(username: String) {
    var userDetails by remember { mutableStateOf<io.appwrite.models.User<Map<String, Any>>?>(null) }
    
    LaunchedEffect(Unit) {
        userDetails = AppwriteService.getCurrentUser()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                modifier = Modifier.size(100.dp),
                tint = Color(0xFFDCE93A)
            )
        }
        
        // Username
        Text(
            text = userDetails?.name ?: username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFDCE93A)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Email",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = userDetails?.email ?: "",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray.copy(alpha = 0.5f)
            )
        }
    }
}

data class MenuItem(
    val title: String,
    val icon: ImageVector? = null,
    val iconPainter: Painter? = null
) 