package app.synapse.core.net

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException

class SocketServer {
    private val tcpPort = 7777
    private val inetAddress = "0.0.0.0"
    private val socket = ServerSocket()
    private var listenerJob : Job = SupervisorJob()
    private val networkScope = CoroutineScope(Dispatchers.IO + listenerJob)
    private var isRunning = false
    private val jobList = mutableListOf<Job>()


    init {
        try {
            socket.bind(InetSocketAddress(inetAddress, tcpPort))
            isRunning = true
        } catch ( e:Exception) {
            Log.e("SockServer", "ERROR: $inetAddress:$tcpPort already in use or permission denied")
            Log.e("SockServer", "ERROR-${e.hashCode()}: ${e.message}")
        }
    }

    fun listen(router: suspend (Socket) -> Unit) {
        networkScope.launch {
            while (isRunning) {
                try {
                    val conn = socket.accept()
                    val job = networkScope.launch {
                        Log.v("SocketServer", "Connection accepted !!!")
                        router(conn)
                    }
//                jobList += job
//
//                with(job) {
//                    job.invokeOnCompletion {
//                        jobList.remove(this)
//                    }
//                }
                } catch (e: SocketException) {
                    Log.e("SocketServer", "SocketError: ${e.message}")
                } catch (e: SocketTimeoutException) {
                    Log.e("SocketServer", "Timeout: ${e.message}")
                }
            }
        }
    }

    fun stop() {
        Log.v("SocketServer", "stop call")
        if (isRunning) {
            isRunning = false
            listenerJob.cancel()
//            jobList.map {
//                it.cancel()
//            }
        }
    }

}