package com.selva.ipc

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.Toast

class MessengerService : Service() {
    // Server code
    private lateinit var messenger: Messenger

    private inline fun safe(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            Log.i(TAG, "${e.message}")
        }
    }

    private val inComingHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SAY_HELLO -> {
                    val message = msg.obj as? String
                    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                    val replyMessage = Message.obtain(null, MSG_REPLY)
                    replyMessage.obj = "Hello client"
                    Thread {
                        safe {
                            Thread.sleep(5000)
                        }
                    }
                    safe { msg.replyTo?.send(replyMessage) }
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        messenger = Messenger(inComingHandler)
        return messenger.binder
    }

    companion object {
        const val MSG_SAY_HELLO = 1
        const val MSG_REPLY = 2
        private const val TAG = "MyService"
    }
}