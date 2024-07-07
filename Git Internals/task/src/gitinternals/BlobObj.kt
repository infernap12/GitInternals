package gitinternals

import java.io.File
import java.util.*

class BlobObj(objFile: File) : GitObj, AbstractGitObj(objFile) {
    val message: String

    init {
//        val uRegex =
//            Regex("(author|committer)\\s([A-Za-z]+)\\s<([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+)>\\s(\\d+)\\s([-+]\\d{4})")
        val sc: Scanner = Scanner(bytes.decodeToString())
        sc.skip(this.header + Char(0))
//        tree = sc.findWithinHorizon("tree\\s([0-9a-f]{40})", 0).removePrefix("tree ")
//        parents = Regex("parent\\s([0-9a-f]{40})").findAll(bytes.decodeToString()).map { it.groupValues[1] }.toList()
//        author = userLineFormatter(sc.findWithinHorizon(uRegex.pattern, 0))
//        committer = userLineFormatter(sc.findWithinHorizon(uRegex.pattern, 0))
        message = sc.useDelimiter("\\A").next().lines().dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }.joinToString("\n")
    }
    override fun print() {
        println("*${this.type.name.uppercase()}*")
        println(message)
    }

}
