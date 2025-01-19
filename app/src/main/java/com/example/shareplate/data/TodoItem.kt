package com.example.shareplate.data

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(val id: Int, val name: String) 