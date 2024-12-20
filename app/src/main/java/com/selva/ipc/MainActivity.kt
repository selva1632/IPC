package com.selva.ipc

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.selva.ipc.MessengerService.Companion.MSG_REPLY
import com.selva.ipc.MessengerService.Companion.MSG_SAY_HELLO
import com.selva.ipc.ui.theme.IPCTheme

class MainActivity : ComponentActivity() {
    private var isBound: Boolean = false
    private var messenger: Messenger? = null
    private lateinit var serviceIntent: Intent

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            isBound = true
            messenger = Messenger(binder)
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
            messenger = null
        }
    }

    internal class ReplyHandler(private val context: Context) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REPLY -> {
                    Log.i(TAG, "message received to activity")
                    val message = msg.obj as String
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    private val replyMessenger = Messenger(ReplyHandler(this@MainActivity))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        serviceIntent = Intent(this@MainActivity, MessengerService::class.java).also {
            startService(it)
        }

        setContent {
            IPCTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        ::sentRequestToServer,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun sentRequestToServer() {
        if (!isBound) return
        val message = Message.obtain(null, MSG_SAY_HELLO)
        message.obj = "Hello Server"
        message.replyTo = replyMessenger
        try {
            messenger?.send(message)
        } catch (e: Throwable) {
            Log.i(TAG, "error - ${e.message}")
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this, MessengerService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun Greeting(sendRequest: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { sendRequest() }) {
            Text(text = "Send Request to Server")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewGreetings() {
    Greeting(sendRequest = {})
}