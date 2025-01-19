package com.example.shareplate.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.postgrest

object SupabaseClient {
    private const val SUPABASE_URL = "https://gbehvytbuecyvwttpyl.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdiZWh2eXRidWVjeXZ3dHR4cHlsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzYyMzEyMDMsImV4cCI6MjA1MTgwNzIwM30.hOrdN8nbHNq8RQY3MaowEP8uEIYzpewfqY2IlYaXSSw"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Storage)
        install(Postgrest)
        install(GoTrue)
        install(Realtime)
    }
} 