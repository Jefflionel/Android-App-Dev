fun main() {
    val words = listOf("Kotlin", "is", "awesome", "for", "Android", "development")

    // Step 1
    val wordLengths = words.associateWith { it.length }

    // Step 2
    wordLengths.filter { it.value > 4 }.forEach { (word, length) ->
        println("$word has $length letters")
    }
}
