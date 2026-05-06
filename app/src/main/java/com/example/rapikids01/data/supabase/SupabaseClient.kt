package com.example.rapikids01.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://xhgtnnrhkkpvqrcdmbsc.supabase.co",
        supabaseKey = "sb_publishable_cx9IFqbcnaRqvJ4zAQS10Q_f32uBgZb"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

}
