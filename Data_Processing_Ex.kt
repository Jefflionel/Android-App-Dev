data class Student(val name: String, val age: Int, val score: Int)

fun List<Student>.averageScore(): Double =
        if (isEmpty()) 0.0 else sumOf { it.score }.toDouble() / size

fun processStudents(students: List<Student>, action: (Student) -> Unit) {
    students.forEach(action)
}

fun main() {
    val students =
            listOf(
                    Student("Alice", 20, 85),
                    Student("Bob", 21, 92),
                    Student("Jeff", 19, 78),
                    Student("Sara", 22, 95),
                    Student("Mike", 20, 65)
            )

    // students with score >= 80
    students.filter { it.score >= 80 }.map { it.name }.forEach { println("Good student: $it") }

    println("\nProcessing all students:")
    processStudents(students) { student ->
        println("${student.name} (${student.age}) scored ${student.score}")
    }

    println("\nAverage score of all students: ${students.averageScore()}")
}
