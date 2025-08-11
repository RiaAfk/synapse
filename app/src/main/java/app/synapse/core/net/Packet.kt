package app.synapse.core.net

import android.util.Log
import java.net.Socket

data class Packet(
    val header : Header,
    val kvStore: KvStore
){
    class Header(private val data: ByteArray) {
        val version: Byte = data[0]
        val length : Int = (data[1].toInt() shl 8) or (data[2].toInt() and 0xFF)
        val type : Byte = data[3]
//         val future : ByteArray = data.sliceArray(2..23)
    }

    class KvStore(private var data: ByteArray = byteArrayOf()) {
        fun put(key : String , value : String ) {
            val keySerialized = key.toByteArray()
            val valueSerialized = key.toByteArray()
            data += byteArrayOf(keySerialized.size.toByte()) + keySerialized
            data += byteArrayOf(valueSerialized.size.toByte()) + valueSerialized
        }

        fun get(key: String) : String? {
            var cur = 0
            var pre = 0
            val keyByte = key.toByteArray()

            while (data.size > cur) {
                cur += data[cur].toInt()+1 // [0]=3+1=4,
                if (cur-pre-1 == keyByte.size) {
                    val target = data.sliceArray(
                            (pre+1)..<cur
                    )

                    pre = cur
                    cur += data[pre].toInt()+1

                    if (target.contentEquals(keyByte)) {
                        return String(
                            data.sliceArray(
                                (pre+1)..<cur
                            )
                        )
                    }

                } else {
                    cur += data[cur].toInt()+1 // [4]=4+1+(4)=9,
                }
                pre = cur // 9+1=10,
            }
            return null
        }

        fun keys() :List<String> {
            var cur = 0
            var pre = 0
            val list = mutableListOf<String>()
            while(data.size > cur) {
                cur = data[cur].toInt()+1
                list += String(data.sliceArray(pre+1..<cur))
                cur = data[cur].toInt()+1
                pre = cur
            }

            return list.toList()
        }
    }

    companion object From {
        const val HEADER_SIZE = 4
        fun from(socket: Socket) : Packet? {
            try {
                val headerData = ByteArray(4)
                socket.getInputStream().read(headerData)
                val header = Header(headerData)
                val chunkData = ByteArray(header.length)
                socket.getInputStream().read(chunkData)
                val kvStore = KvStore(chunkData)
                return Packet(
                    header,
                    kvStore
                )
            } catch (e:Exception) {
                Log.e("fromSocket", "Error: ${e.message}")
                return null
            }
        }
    }
}
//    1        4+1
// 0  1 2 3 |  4  5 6 7 8 || 9  10 11 12 | 13 14 15 16 ||
// 3: 0 0 0 |  4: 0 0 0 0 || 3: 0  0  0  | 2: 0  0  0  ||
// 0          3+1          4+8+1          9+3+1
//    1-3         5-8           4-9           9-13


// 1  5        0+1+3  +   4   ,9
// 3  8           1+3    +  1+4 ,1+3