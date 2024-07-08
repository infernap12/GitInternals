package gitinternals

interface GitObj {
    fun print()
    val hash: String
    val bytes: ByteArray
}
