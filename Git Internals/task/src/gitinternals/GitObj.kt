package gitinternals

import java.io.File

interface GitObj {
    fun print()
    val hash: String
    val bytes: ByteArray
    val type: ObjectType
    val gitRoot: File
}
