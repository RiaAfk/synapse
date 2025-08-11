package app.synapse.daemon

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import app.synapse.core.net.Packet
import app.synapse.core.net.SocketServer

class DataListener: Service() {

    inner class LocalBinder : Binder() {
        fun getService() = this@DataListener
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sockSrv = SocketServer()
        sockSrv.listen { pkt ->
            Packet.from(pkt)?.let {
                Log.w("DataListener", "REC: ${it.kvStore.keys()}")
            }
        }
        return START_STICKY
    }
}