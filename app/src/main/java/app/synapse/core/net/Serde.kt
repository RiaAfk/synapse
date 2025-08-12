package app.synapse.core.net

interface Serde {
    fun serialize() : ByteArray
}