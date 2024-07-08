package gitinternals

import java.io.File
import kotlin.system.exitProcess

// f9e0930b89b3294ea6a195f0808aafd36fafe8
// /c/Users/james/IdeaProjects/Git Internals/.git/objects/1a
//.git/objects/1a/f9e0930b89b3294ea6a195f0808aafd36fafe8
var gitFolder: File = File("")

fun main() {
    gitFolder = askGitFolder()

    when (askCommand()) {
        Command.CAT_FILE -> {
            val objFile = askGitObjectHash()
            val obj = objFactory(objFile)
            obj.print()
        }

        Command.LIST_BRANCHES -> {
            val heads = File(gitFolder, "refs/heads").listFiles()!!.toList()
            val headFile = File(gitFolder, "HEAD")
            val selectedHead = File(gitFolder, headFile.readText())

            heads.sortedBy { it.name }.map {
                val match = if (it.name == selectedHead.name) "*" else " "
                "$match ${it.name}"
            }.forEach(::println)

        }

        Command.LOG -> {
            val branch = askBranch()
            gitLog(branch)
        }

        Command.COMMIT_TREE -> {
            val hash = askGitObjectHash(true)
            val commitObj = CommitObj(hash)
            commitTree(commitObj)

        }
    }


}

fun commitTree(commitObj: CommitObj) {
    val treeObj = TreeObj(commitObj.tree, commitObj.gitRoot)
    val files = walk(treeObj)
    files.forEach(::println)
}

fun walk(gitObj: GitObj, path: String = ""): List<String> {
    return when (gitObj.type) {
        ObjectType.BLOB -> listOf(path)
        ObjectType.TREE -> {
            val treeObj = gitObj as TreeObj
            treeObj.objects.flatMap { (obj, name) ->
                val newPath = if (path.isEmpty()) name else "$path/$name"
                walk(obj, newPath)
            }
        }

        else -> emptyList()
    }
}

fun gitLog(branch: File) {
    val root = branch.parentFile.parentFile.parentFile
    var commit: CommitObj = CommitObj(hashToFile(branch.readText().dropLast(1), root))
    val commits = listOf(commit to false) + rLog(commit)
    commits.joinToString("\n\n") { it.first.getLogEntry(it.second) }.let(::println)
}

fun rLog(commitObj: CommitObj): List<Pair<CommitObj, Boolean>> {

    return when {
        commitObj.parentsObjs.isEmpty() -> emptyList()
//        commitObj.parentsObjs.size == 1 -> listOf(commitObj to false)
        else -> {
            val commits = commitObj.parentsObjs.mapIndexed { index, commitObj -> commitObj to (index != 0) }.reversed()
            val next = CommitObj(commitObj.parents.first(), commitObj.gitRoot)
            commits + rLog(next)
        }
    }
}

fun askBranch(): File {
    println("Enter branch name:")
    val file = gitFolder.resolve("refs/heads").resolve(readln())
    if (!file.isFile || file.readBytes().size != 41) {
        throw IllegalArgumentException("Bad branch")
    } else return file
}

fun askCommand(): Command {
    println("Enter command:")
    val input = readln().replace('-', '_').uppercase()
    while (true) {
        val res = runCatching { Command.valueOf(input) }
        if (res.isSuccess) {
            return res.getOrThrow()
        }
    }
}

fun die() {
    exitProcess(1)
}

fun objFactory(objFile: File): GitObj {
    return when (AbstractGitObj(objFile).type) {
        ObjectType.BLOB -> BlobObj(objFile)
        ObjectType.TREE -> TreeObj(objFile)
        ObjectType.COMMIT -> CommitObj(objFile)
    }
}

//fun inspectBlob(file: File) {
//    val raw = file.readBytes()
//    val inflater = Inflater()
//    inflater.setInput(raw)
//
//    val output = ByteArray(1024)
//    val outputStream = ByteArrayOutputStream()
//    while (!inflater.finished()) {
//        val count = inflater.inflate(output)
//        outputStream.write(output, 0, count)
//    }
//
//    println(outputStream.toByteArray()
//        .map { it.coerceAtLeast(10) }
//        .toByteArray()
//        .decodeToString())
//}

fun askGitFolder(): File {
    println("Enter .git directory location:")
    val file = File(readln())
    if (!file.isDirectory || !file.list()!!.contains("objects")) {
        throw IllegalArgumentException("Bad git folder")
    } else return file
}

fun askGitObjectHash(commHash: Boolean = false): File {
    println(if (commHash) "Enter commit-hash:" else "Enter git object hash:")
    val hashString = readln()
    val objFile = hashToFile(hashString, gitFolder)
    if (!objFile.isFile) throw IllegalArgumentException("File not found") else return objFile
}

fun hashToFile(s: String, gitDir: File): File {
    val objDir = File(gitDir.resolve("objects"), s.take(2))
    return File(objDir, s.drop(2))
}