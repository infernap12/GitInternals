package gitinternals

import java.io.File


//todo blow from orbit horrific nightmare code
class TreeObj(objFile: File) : GitObj, AbstractGitObj(objFile) {
    constructor(hash: String, gitRoot: File) : this(hashToFile(hash, gitRoot))

    private val entries: List<String>
    val objects: List<Pair<GitObj, String>>

    init {
        val iterator =
            bytes.toUByteArray().dropWhile { it != UByte.MIN_VALUE }.drop(1).iterator()
        val shas = emptyList<String>().toMutableList()
        var takingNumber = true
        val nb = StringBuilder()
        val fb = StringBuilder()

        while (iterator.hasNext()) {
            val b = iterator.next()
            if (takingNumber) {
                if (b == ' '.code.toUByte()) {
                    takingNumber = false
                } else {
                    nb.append(Char(b.toInt()))
                }
            } else if (b == UByte.MIN_VALUE) {
                val bList = emptyList<UByte>().toMutableList()
                repeat(20) {
                    bList.add(iterator.next())
                }
                shas.add("$nb ${bList.toUByteArray().toHex()} $fb")
                nb.clear()
                fb.clear()
                takingNumber = true
            } else {
                fb.append(Char(b.toInt()))
            }

        }
        entries = shas.toList()

        objects = entries.map {
            objFactory(hashToFile(Regex("[0-9a-f]{40}").find(it)!!.value, gitRoot)) to it.split(" ").last()
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    override fun print() {
        println("*${this.type.name.uppercase()}*")
        entries.forEach(::println)
    }

}


@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toHex(): String = this.joinToString("") { it.toString(radix = 16).padStart(2, '0') }