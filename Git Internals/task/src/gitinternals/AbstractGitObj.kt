package gitinternals

import java.io.File
import java.util.zip.InflaterInputStream

open class AbstractGitObj(objFile: File) {
    val bytes: ByteArray
    protected val header: String
    val gitRoot: File = objFile.parentFile.parentFile.parentFile
    val type: ObjectType
    val length: String
    val hash = objFile.parentFile.name + objFile.name



    fun printBytes() {
        println(bytes.decodeToString())
    }

//    val parent: String
//        get() = Regex("tree\\s([0-9a-f]{40})").find(bytes.decodeToString())?.groups?.get(1)?.value.let { "parent: $it" }

    init {
        bytes = InflaterInputStream(objFile.inputStream()).readBytes()
        header = bytes.decodeToString().split("\u0000").first()

        type = ObjectType.valueOf(header.split(" ").first().uppercase())
        length = header.split(" ").last()

//        val sc: Scanner = Scanner(bytes.decodeToString())
//        tree = sc.next("tree\\s([0-9a-f]{40})")
//        parents = sc.

    }
}