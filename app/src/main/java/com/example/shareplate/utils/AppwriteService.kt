package com.example.shareplate.utils

import android.content.Context
import io.appwrite.Client
import io.appwrite.services.Account
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.runBlocking

object AppwriteService {
    private const val ENDPOINT = "https://cloud.appwrite.io/v1"
    private const val PROJECT_ID = "677d5c5300122e700877"
    private const val PREFS_NAME = "SharePlatePrefs"
    private const val KEY_USERNAME = "username"
    private const val KEY_SESSION_ID = "session_id"
    
    private lateinit var client: Client
    private lateinit var account: Account
    private lateinit var context: Context
    
    fun initialize(ctx: Context) {
        context = ctx.applicationContext
        client = Client(context)
            .setEndpoint(ENDPOINT)
            .setProject(PROJECT_ID)
        
        account = Account(client)
    }
    
    fun getAccount(): Account {
        if (!::account.isInitialized) {
            throw IllegalStateException("AppwriteService must be initialized first")
        }
        return account
    }
    
    suspend fun getCurrentUser(): io.appwrite.models.User<Map<String, Any>>? {
        return try {
            getAccount().get()
        } catch (e: AppwriteException) {
            null
        }
    }
    
    fun saveUsername(username: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }
    
    fun getUsername(): String? {
        return try {
            runBlocking {
                getCurrentUser()?.name
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getSessionId(): String? {
        return try {
            runBlocking {
                getAccount().getSession("current").id
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun hasValidSession(): Boolean {
        return try {
            // This will throw an exception if there's no valid session
            getAccount().get()
            true
        } catch (e: AppwriteException) {
            false
        }
    }

    suspend fun logout() {
        try {
            getAccount().deleteSession("current")
        } catch (e: AppwriteException) {
            // Handle logout error
            e.printStackTrace()
        }
    }
} 