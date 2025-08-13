package app.synapse.core.net

import android.util.Log
import java.net.Socket

data class Packet(
    val header : Header,
    val kvStore: KvStore
): Serde {
    class Header(private val data: ByteArray) : Serde {
        val version: Byte = data[0]
        val length : Int = (data[1].toInt() shl 8) or (data[2].toInt() and 0xFF)
        val type : Byte = data[3]
//         val future : ByteArray = data.sliceArray(2..23)

        override fun serialize(): ByteArray {
            return data
        }

        companion object From {
            fun from(
                version : Byte = 0.toByte(),
                length : Int = 0,
                type : Byte = 0.toByte()
            ) : Header {
                val highByte = (length shr 8).toByte()
                val lowByte = (length and 0xFF).toByte()

                return Header(byteArrayOf(version , highByte, lowByte , 0 , type))
            }
        }
    }

    class KvStore(private var data: ByteArray = byteArrayOf()) : Serde {
        val len : Int
            get() {
                return data.size
            }

        val size : Int
            get() {
                return keys().size
            }


        fun put(key : String , value : String ) {
            if (key.isEmpty() || value.isEmpty()) {
                return
            }
            val keySerialized = key.toByteArray()
            val valueSerialized = value.toByteArray()
            data += byteArrayOf(keySerialized.size.toByte()) + keySerialized

            Log.v("Packet","Puts : ${keySerialized.size} = ${keySerialized.toList()}")
            data += byteArrayOf(valueSerialized.size.toByte()) + valueSerialized
            Log.v("Packet","Puts : ${valueSerialized.size} = ${valueSerialized.toList()}")
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
                cur += data[cur].toInt()+1
                list += String(data.sliceArray(pre+1..<cur))
                cur += data[cur].toInt()+1
                pre = cur
            }

            return list.toList()
        }

        override fun serialize(): ByteArray {
            return data
        }
    }

    override fun serialize(): ByteArray {
        return header.serialize()+kvStore.serialize()
    }

    companion object From {
        const val HEADER_SIZE = 4
        fun from(socket: Socket) : Packet? {
            try {
                val headerData = ByteArray(HEADER_SIZE)
                Log.e("fromSocket", "Header0: ${headerData.toList()}")
                socket.getInputStream().read(headerData)
                val header = Header(headerData)
                Log.e("fromSocket", "Header0: v${header.version} len:${header.length} type-${header.type} , ${headerData.size}")

                val chunkData = ByteArray(header.length)
                socket.getInputStream().read(chunkData)
                val kvStore = KvStore(chunkData)
                socket.close()

                Log.e("fromSocket", "KvStore0: ${kvStore.len} ${kvStore.keys()}")
                return Packet(
                    header,
                    kvStore
                )
            } catch (e:Exception) {
                Log.e("fromSocket", "Error: ${e.message}")
                return null
            }
        }

        fun from(kvStore: KvStore) : Packet {
            val header = Header.from(
                length = kvStore.len
            )

            Log.v("Packet", "Packed with len-${kvStore.len}")

            return Packet(
                header,
                kvStore
            )
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