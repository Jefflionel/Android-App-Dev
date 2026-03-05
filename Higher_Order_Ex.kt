import kotlin.*

fun processList(numbers: List<Int>, predicate: (Int) -> Boolean): List<Int> =
        numbers.filter(predicate) 

fun main() {
    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    // Example 1: keep only even numbers
    val evenNumbers = processList(numbers) { it % 2 == 0 }
    println("Even: $evenNumbers") 

    // Example 2: keep only numbers > 5
    val bigNumbers = processList(numbers) { it > 5 }
    println("Big: $bigNumbers") 
}
