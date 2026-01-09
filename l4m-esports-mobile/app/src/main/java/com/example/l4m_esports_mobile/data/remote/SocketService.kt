package com.example.l4m_esports_mobile.data.remote

import android.util.Log
import com.example.l4m_esports_mobile.data.local.PreferencesManager
import com.example.l4m_esports_mobile.util.Constants
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketService @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    private var socket: Socket? = null
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun connect() {
        if (socket?.connected() == true) {
            return
        }

        try {
            val token = runBlocking { preferencesManager.getToken() }
            val options = IO.Options().apply {
                auth = mapOf("token" to (token ?: ""))
                transports = arrayOf("websocket", "polling")
            }

            socket = IO.socket("${Constants.BASE_URL}", options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketService", "🔌 Socket.io connected")
                _isConnected.value = true
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("SocketService", "🔌 Socket.io disconnected")
                _isConnected.value = false
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("SocketService", "🔌 Socket.io connection error: ${args[0]}")
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("SocketService", "Error connecting to socket: ${e.message}")
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        _isConnected.value = false
    }

    fun on(event: String, callback: (Array<out Any>) -> Unit) {
        socket?.on(event) { args ->
            callback(args)
        }
    }

    fun off(event: String) {
        socket?.off(event)
    }

    fun reconnect() {
        disconnect()
        connect()
    }
}

