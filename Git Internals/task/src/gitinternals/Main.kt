package gitinternals

import java.io.File

// f9e0930b89b3294ea6a195f0808aafd36fafe8
// /c/Users/james/IdeaProjects/Git Internals/.git/objects/1a
//.git/objects/1a/f9e0930b89b3294ea6a195f0808aafd36fafe8

fun main() {
    val gitFolder: File = askGitFolder()
    val objFile = askGitHash(gitFolder)
    val obj = objFactory(objFile)
    obj.print()

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

fun askGitHash(gitDir: File): File {
    println("Enter git object hash:")
    val hashString = readln()
    val objDir = File(gitDir.resolve("objects"), hashString.take(2))
    val objFile = File(objDir, hashString.drop(2))
    if (!objFile.isFile) throw IllegalArgumentException("File not found") else return objFile
}

fun hashToFile(s: String, gitDir: File): File {
    val objDir = File(gitDir.resolve("objects"), s.take(2))
    return File(objDir, s.drop(2))
}