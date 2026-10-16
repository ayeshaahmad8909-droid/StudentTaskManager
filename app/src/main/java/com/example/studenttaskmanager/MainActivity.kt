
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Task(
    val id: Int,
    val title: String,
    val subject: String,
    val dueDate: String,
    val completed: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudentTaskApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentTaskApp() {
    val tasks = remember {
        mutableStateListOf(
            Task(1, "Prepare Git commands", "Software Construction", "2026-10-20"),
            Task(2, "Complete portfolio website", "Web Engineering", "2026-10-22"),
            Task(3, "Revise process management", "Operating Systems", "2026-10-24")
        )
    }

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf(4) }
    var filter by remember { mutableStateOf("All") }
    var error by remember { mutableStateOf("") }

    val completedCount = tasks.count { it.completed }
    val pendingCount = tasks.size - completedCount

    val visibleTasks = tasks.filter {
        when (filter) {
            "Pending" -> !it.completed
            "Completed" -> it.completed
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Student Task Manager",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Organize your studies, one task at a time.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryCard("Total", tasks.size, Modifier.weight(1f))
            SummaryCard("Pending", pendingCount, Modifier.weight(1f))
            SummaryCard("Done", completedCount, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Add an Assignment",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Assignment title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Due date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (title.isBlank() || subject.isBlank() || dueDate.isBlank()) {
                    error = "Please fill in all three fields."
                } else if (!dueDate.matches(
                        Regex("""\d{4}-\d{2}-\d{2}""")
                    ) || dueDate.substring(5, 7).toIntOrNull() !in 1..12 ||
                    dueDate.substring(8, 10).toIntOrNull() !in 1..31
                ) {
                    error = "Enter a valid date in YYYY-MM-DD format."
                } else {
                    tasks.add(
                        Task(
                            id = nextId++,
                            title = title.trim(),
                            subject = subject.trim(),
                            dueDate = dueDate
                        )
                    )
                    title = ""
                    subject = ""
                    dueDate = ""
                    error = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Assignment")
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "My Assignments",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Pending", "Completed").forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(option) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visibleTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    onToggle = {
                        val index = tasks.indexOfFirst { it.id == task.id }
                        if (index != -1) {
                            tasks[index] = tasks[index].copy(
                                completed = !tasks[index].completed
                            )
                        }
                    },
                    onDelete = {
                        tasks.removeAll { it.id == task.id }
                    }
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = label, fontSize = 12.sp)
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.completed)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.subject,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    fontSize = 12.sp
                )
                if (task.completed) {
                    Text(
                        text = "Completed",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                }
            }

            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}
