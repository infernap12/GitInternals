package gitinternals

import kotlinx.datetime.*
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import java.io.File
import java.util.*


class CommitObj(val objFile: File) : GitObj, AbstractGitObj(objFile) {
    val tree: String
    val parents: List<String>
    val parentsObjs: List<CommitObj>
    val author: String?
    val committer: String?
    val message: String

    fun getLogEntry(merge: Boolean): String {
        return """
                    Commit: $hash${if (merge) " (merged)" else ""}
                    $committer
                    $message
                """.trimIndent()
    }

    constructor(hash: String, gitRoot: File) : this(hashToFile(hash, gitRoot))

    init {
        val uRegex =
            Regex("(author|committer)\\s([A-Za-z]+)\\s<([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+)>\\s(\\d+)\\s([-+]\\d{4})")
        val sc: Scanner = Scanner(bytes.decodeToString())
        tree = sc.findWithinHorizon("tree\\s([0-9a-f]{40})", 0).removePrefix("tree ")
        parents = Regex("parent\\s([0-9a-f]{40})").findAll(bytes.decodeToString()).map { it.groupValues[1] }.toList()
        parentsObjs = parents.map { CommitObj(it, gitRoot) }
        author = userLineFormatter(sc.findWithinHorizon(uRegex.pattern, 0))
        committer = userLineFormatter(sc.findWithinHorizon(uRegex.pattern, 0))
        message = sc.useDelimiter("\\A").next().lines().dropWhile { it.isBlank() }.dropLastWhile { it.isBlank() }.joinToString("\n")
    }

    override fun print() {
        println("*${this.type.name.uppercase()}*")
        println("tree: $tree")
        if (parents.isNotEmpty()) println("parents: ${parents.joinToString(" | ")}")
        author?.let { println("author: $it") }
        committer?.let { println("committer: $it") }
        println("commit message:\n$message")
    }
}

fun userLineFormatter(str: String): String {
    val strings = str.split(" ")
    val name = strings[1]
    val email = strings[2].removeSurrounding("<", ">")
    val tsLabel = if (strings[0] == "author") "original timestamp:" else "commit timestamp:"
    val timestamp = parseTimeStamp(strings[3], strings[4])

    return "$name $email $tsLabel $timestamp"
}

fun parseTimeStamp(epochSeconds: String, offset: String): String {
    val instant = Instant.fromEpochSeconds(epochSeconds.toLong())
    val of = UtcOffset.parse(offset, UtcOffset.Formats.FOUR_DIGITS)
    return instant.format(dateTimeFormat, of)
}

val dateTimeFormat =
    DateTimeComponents.Format {
        date(LocalDate.Formats.ISO)
        char(' ')
        time(LocalTime.Formats.ISO)
        char(' ')
        offset(UtcOffset.Formats.ISO)
    }
