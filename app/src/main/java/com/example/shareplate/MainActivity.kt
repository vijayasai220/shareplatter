package com.example.shareplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.shareplate.data.TodoItem
import com.example.shareplate.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TodoList()
                }
            }
        }
    }
}

@Composable
fun TodoList() {
    var items by remember { mutableStateOf<List<TodoItem>>(listOf()) }
    
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                items = SupabaseClient.client.postgrest["todos"]
                    .select()
                    .decodeList()
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error appropriately
            }
        }
    }
    
    LazyColumn {
        items(
            items,
            key = { item -> item.id }
        ) { item ->
            Text(
                item.name,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}