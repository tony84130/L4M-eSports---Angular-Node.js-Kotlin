package com.example.l4m_esports_mobile.util

object Constants {
    // API Configuration
    const val BASE_URL = "http://10.0.2.2:3000" // 10.0.2.2 est l'adresse localhost pour l'émulateur Android
    // Pour un appareil physique, utilisez l'IP de votre machine : "http://192.168.x.x:3000"
    
    const val API_PREFIX = "/api/"
    
    // DataStore Keys
    const val DATASTORE_NAME = "l4m_esports_prefs"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_USER_ID = "user_id"
    
    // Network
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}

