package com.example.composeapiapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class User(val id: Int, val name: String, val email: String)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                var users by remember { mutableStateOf(listOf<User>()) }
                var loading by remember { mutableStateOf(false) }
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = { Text("👥 User Management", fontWeight = FontWeight.Bold) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(padding)
                            .padding(16.dp)
                    ) {
                        UserForm(
                            onSubmit = { name, email, clearFields ->
                                lifecycleScope.launch {
                                    loading = true
                                    try {
                                        sendUserToAPI(name, email)
                                        users = getUsersFromAPI()
                                        clearFields()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("✅ User added successfully")
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("❌ Error: ${e.message}")
                                        }
                                    } finally {
                                        loading = false
                                    }
                                }
                            },
                            onGetUsers = {
                                lifecycleScope.launch {
                                    loading = true
                                    try {
                                        users = getUsersFromAPI()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("📋 User list updated")
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("❌ Failed to fetch users")
                                        }
                                    } finally {
                                        loading = false
                                    }
                                }
                            },
                            onDeleteUser = { userId ->
                                lifecycleScope.launch {
                                    loading = true
                                    try {
                                        deleteUserFromAPI(userId)
                                        users = getUsersFromAPI()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("🗑️ User deleted")
                                        }
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("❌ Failed to delete user")
                                        }
                                    } finally {
                                        loading = false
                                    }
                                }
                            },
                            users = users,
                            loading = loading
                        )
                    }
                }
            }
        }
    }

    // 🔹 POST
    private suspend fun sendUserToAPI(name: String, email: String) = withContext(Dispatchers.IO) {
        val url = URL("http://192.168.1.4:3000/users")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.doOutput = true
        val jsonInputString = """{"name":"$name","email":"$email"}"""
        connection.outputStream.use { os ->
            val input = jsonInputString.toByteArray(Charsets.UTF_8)
            os.write(input, 0, input.size)
        }
        if (connection.responseCode != 201)
            throw Exception("Error ${connection.responseCode}: ${connection.errorStream?.bufferedReader()?.readText()}")
        connection.disconnect()
    }

    // 🔹 GET
    private suspend fun getUsersFromAPI(): List<User> = withContext(Dispatchers.IO) {
        val url = URL("http://192.168.1.4:3000/users")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        val response = BufferedReader(InputStreamReader(connection.inputStream)).readText()
        connection.disconnect()

        if (responseCode == 200) {
            val jsonArray = JSONArray(response)
            List(jsonArray.length()) { i ->
                val obj = jsonArray.getJSONObject(i)
                User(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    email = obj.getString("email")
                )
            }
        } else throw Exception("Error $responseCode")
    }

    // 🔹 DELETE
    private suspend fun deleteUserFromAPI(id: Int) = withContext(Dispatchers.IO) {
        val url = URL("http://192.168.1.4:3000/users/$id")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "DELETE"
        val responseCode = connection.responseCode
        connection.disconnect()
        if (responseCode != 200 && responseCode != 204) {
            throw Exception("Error $responseCode while deleting user")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserForm(
    onSubmit: (String, String, clearFields: () -> Unit) -> Unit,
    onGetUsers: () -> Unit,
    onDeleteUser: (Int) -> Unit,
    users: List<User>,
    loading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    fun clearFields() {
        name = ""
        email = ""
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { onSubmit(name, email, ::clearFields) },
                enabled = name.isNotBlank() && email.isNotBlank() && !loading,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add")
                Spacer(Modifier.width(6.dp))
                Text("Add User")
            }

            OutlinedButton(
                onClick = onGetUsers,
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Refresh List")
            }
        }

        if (loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        if (users.isEmpty()) {
            Text(
                text = "No users available yet.",
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.DarkGray)
                                )
                            }
                            IconButton(onClick = { userToDelete = user }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete user",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 🔹 Confirmation Dialog
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete ${userToDelete?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteUser(userToDelete!!.id)
                    userToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
