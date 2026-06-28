package com.steply.app.remote

import android.util.Log
import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import org.json.JSONObject

/**
 * Sends JPEG camera frames from the Android app to the local Steply PC WebSocket server.
 *
 * Protocol:
 * 1. Connect to ws://<PC_IP>:3000/ws
 * 2. Send {"type":"hello","role":"sender","source":"android"}
 * 3. Send binary JPEG frames. The Node demo server converts them for web viewers.
 */
class RemoteCameraStreamer(
    private val serverUrl: String,
    private val onStatus: (String) -> Unit,
    private val onError: (String) -> Unit,
) : AutoCloseable {
    private val client = OkHttpClient()
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var webSocket: WebSocket? = null
    @Volatile private var connected = false

    fun connect() {
        if (connected || webSocket != null) return

        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    connected = true
                    val hello = JSONObject()
                        .put("type", "hello")
                        .put("role", "sender")
                        .put("source", "android")
                        .toString()
                    webSocket.send(hello)
                    emitStatus("PC 연결 성공: $serverUrl")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    connected = false
                    this@RemoteCameraStreamer.webSocket = null
                    android.util.Log.e("RemoteCamera", "onFailure: ${t.message}", t)
                    emitError("PC 연결 실패: ${t.message ?: "unknown error"}")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    connected = false
                    this@RemoteCameraStreamer.webSocket = null
                    android.util.Log.d("RemoteCamera", "onClosed: $code / $reason")
                    emitStatus("PC 연결 종료")
                }
            },
        )
    }

    fun sendJpeg(bytes: ByteArray) {
        val socket = webSocket
        android.util.Log.d(
            "RemoteCamera",
            "sendJpeg called: bytes=${bytes.size}, socketNull=${socket == null}, connected=$connected"
        )

        if (socket == null) return
        if (!connected) return

        val result = socket.send(bytes.toByteString())
        android.util.Log.d("RemoteCamera", "websocket send result=$result")
    }

    private fun emitStatus(message: String) {
        mainHandler.post { onStatus(message) }
    }

    private fun emitError(message: String) {
        mainHandler.post { onError(message) }
    }

    fun disconnect() {
        connected = false
        webSocket?.send(JSONObject().put("type", "stopped").toString())
        webSocket?.close(1000, "Android camera stopped")
        webSocket = null
    }

    override fun close() {
        disconnect()
        client.dispatcher.executorService.shutdown()
    }
}
