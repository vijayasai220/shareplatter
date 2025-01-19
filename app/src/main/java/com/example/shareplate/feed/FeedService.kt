package com.example.shareplate.feed

import io.appwrite.Query
import io.appwrite.services.Databases
import io.appwrite.models.Document
import com.example.shareplate.utils.AppwriteService
import android.util.Log
import io.appwrite.ID

class FeedService(private val databases: Databases) {
    companion object {
        const val DATABASE_ID = "677d6309001da6cb4ee9"
        const val COLLECTION_ID = "678009050017c47066fd"
        const val BUCKET_ID = "677d62110026c44f4842"
        const val PROJECT_ID = "677d5c5300122e700877"
    }

    suspend fun createPost(
        userId: String,
        username: String,
        foodName: String,
        imageId: String,
        servingSize: Int
    ) {
        try {
            Log.d("FeedService", "Creating new post for $username")
            Log.d("FeedService", "Image ID for post: $imageId")
            
            databases.createDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = ID.unique(),
                data = mapOf(
                    "userId" to userId,
                    "username" to username,
                    "foodName" to foodName,
                    "imageId" to imageId,
                    "likes" to 0,
                    "likedBy" to listOf<String>(),
                    "timestamp" to "2024-01-20 00:00:00"
                )
            )
            Log.d("FeedService", "Successfully created post with image ID: $imageId")
        } catch (e: Exception) {
            Log.e("FeedService", "Error creating post", e)
            throw e
        }
    }

    suspend fun getFeedPosts(): List<FeedPost> {
        try {
            Log.d("FeedService", "Fetching feed posts...")
            val response = databases.listDocuments(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                queries = listOf(
                    Query.orderDesc("timestamp")
                )
            )
            
            Log.d("FeedService", "Got ${response.documents.size} documents")
            
            return response.documents.map { doc ->
                val imageId = doc.data["imageId"]?.toString() ?: ""
                Log.d("FeedService", "Processing document: ${doc.id}, Image ID: $imageId")
                
                FeedPost(
                    id = doc.id,
                    userId = doc.data["userId"]?.toString() ?: "",
                    username = doc.data["username"]?.toString() ?: "",
                    foodName = doc.data["foodName"]?.toString() ?: "",
                    imageId = imageId,
                    likes = (doc.data["likes"] as? Number)?.toInt() ?: 0,
                    timestamp = doc.data["timestamp"]?.toString() ?: "",
                    isLikedByCurrentUser = (doc.data["likedBy"] as? List<*>)
                        ?.contains(AppwriteService.getCurrentUser()?.id) ?: false
                )
            }
        } catch (e: Exception) {
            Log.e("FeedService", "Error fetching posts", e)
            throw e
        }
    }

    suspend fun likePost(postId: String, userId: String) {
        val currentDoc = databases.getDocument(
            databaseId = DATABASE_ID,
            collectionId = COLLECTION_ID,
            documentId = postId
        )
        
        val currentLikes = (currentDoc.data["likes"] as? Number)?.toInt() ?: 0
        val likedBy = (currentDoc.data["likedBy"] as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        
        if (!likedBy.contains(userId)) {
            likedBy.add(userId)
            databases.updateDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = postId,
                data = mapOf(
                    "likes" to (currentLikes + 1),
                    "likedBy" to likedBy
                )
            )
        }
    }

    suspend fun unlikePost(postId: String, userId: String) {
        val currentDoc = databases.getDocument(
            databaseId = DATABASE_ID,
            collectionId = COLLECTION_ID,
            documentId = postId
        )
        
        val currentLikes = (currentDoc.data["likes"] as? Number)?.toInt() ?: 0
        val likedBy = (currentDoc.data["likedBy"] as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
        
        if (likedBy.contains(userId)) {
            likedBy.remove(userId)
            databases.updateDocument(
                databaseId = DATABASE_ID,
                collectionId = COLLECTION_ID,
                documentId = postId,
                data = mapOf(
                    "likes" to (currentLikes - 1),
                    "likedBy" to likedBy
                )
            )
        }
    }
} 