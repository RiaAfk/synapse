package app.synapse

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.synapse.core.net.Packet
import app.synapse.daemon.DataListener
import app.synapse.ui.theme.SynapseTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

class MainActivity : ComponentActivity() {

    override fun onStart() {
        super.onStart()
        if (!DataListener.isRunning) {
            Intent(this, DataListener::class.java).also {
                startService(it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynapseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    var key by remember { mutableStateOf("key") }
    var value by remember { mutableStateOf("4444") }
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = "Hello ${name}!",
            modifier = modifier
        )
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(2.dp)
        ) {
            TextField(
                value = key,
                onValueChange = { key = it },
                placeholder = { Text("Key") },
                modifier = Modifier.fillMaxWidth()
                    .padding(2.dp)
            )
            TextField(
                value = value,
                onValueChange = { value = it },
                placeholder = { Text("Value") },
                modifier = Modifier.fillMaxWidth()
                    .padding(2.dp)
            )
            Button(
                onClick = {
                    Log.w("Btn", "Click recorded")
                    CoroutineScope(Dispatchers.IO).launch {
                        val socks = Socket()
                        socks.connect(InetSocketAddress("127.0.0.1",7777))
                        val pkt = Packet.from(
                            kvStore = Packet.KvStore().apply {
                                put(key , value)
                            }
                        )

                        socks.outputStream.write(
                            pkt.serialize()
                        )
                    }
                }
            ) {
                Text("Submit")
            }
        }
    }
}