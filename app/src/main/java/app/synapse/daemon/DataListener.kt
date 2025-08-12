package app.synapse.daemon

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import app.synapse.core.net.Packet
import app.synapse.core.net.SocketServer

class DataListener: Service() {
    companion object State {
        var isRunning = false
    }

    var sockSrv: SocketServer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v("DataListener", "starting service")
        isRunning = true
        sockSrv = SocketServer()
        sockSrv?.listen { soc ->
            Packet.from(soc)?.let {
                Log.v("DataListener", "PEER: ${soc.localAddress}")
                Log.v("DataListener", "REC : ${it.kvStore.keys()}")
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        isRunning=false
        sockSrv?.stop()
        super.onDestroy()
    }
}