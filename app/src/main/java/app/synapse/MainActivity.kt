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
import app.synapse.daemon.DataListener
import app.synapse.ui.theme.SynapseTheme

class MainActivity : ComponentActivity() {
    var dataListener : DataListener? = null
    private val name = mutableStateOf("")

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            dataListener = (service as DataListener.LocalBinder).getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            dataListener = null
        }
    }

    override fun onStart() {
        super.onStart()
        Intent(this,DataListener::class.java).also {
            bindService(it,connection,Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
    }


    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SynapseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name:MutableState<String>,modifier: Modifier = Modifier) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(18.dp)
    ) {
        Text(
            text = "Hello ${name.value}!",
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
                }
            ) {
                Text("Submit")
            }
        }
    }
}