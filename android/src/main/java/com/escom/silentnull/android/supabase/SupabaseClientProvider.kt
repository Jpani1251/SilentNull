package com.escom.silentnull.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClientProvider {

    val client = createSupabaseClient(
        supabaseUrl = "https://xfyhkegkqkoaztdawdjb.supabase.co/rest/v1/",
        supabaseKey = "sb_secret_71SmoZCc70qYmyZbXaAKNA_PFJ5U70sY"
    ) {
        install(Postgrest)
    }
}
