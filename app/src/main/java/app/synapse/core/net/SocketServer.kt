package app.synapse.core.net

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress

class SocketServer {
    private val tcpPort = 7777
    private val inetAddress = "0.0.0.0"
    private val socket = ServerSocket()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val jobList = mutableListOf<Job>()
    init {
        try {
            socket.bind(InetSocketAddress(inetAddress, tcpPort))
        } catch ( e:Exception) {
            Log.e("SockServer", "ERROR: $inetAddress:$tcpPort already in use or permission denied")
            Log.e("SockServer", "ERROR-${e.hashCode()}: ${e.message}")
        }
    }

    fun listen(router: (Socket) -> Unit) {
        coroutineScope.launch {
            while (true) {
                try {
                    val conn = socket.accept()
                    val job = coroutineScope.launch {
                        Log.i("SocketServer", "Connection accepted !!!")
                        router(conn)
                    }
                    jobList += job
                    job.invokeOnCompletion {
                        jobList.remove(job)
                    }
                }
                catch (e : Exception) {
                    Log.e("SocketServer", "Error: ${e.message}")
                }
            }
        }
    }

}