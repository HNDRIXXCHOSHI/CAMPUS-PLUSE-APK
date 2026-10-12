package com.campuspulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.campuspulse.app.data.StudentProfileRepository

@Composable
fun CompleteProfileScreen(onComplete: () -> Unit, onLogout: () -> Unit) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val repository = remember { StudentProfileRepository() }
    var name by remember(uid) { mutableStateOf("") }
    var number by remember(uid) { mutableStateOf("") }
    var course by remember(uid) { mutableStateOf("") }
    var department by remember(uid) { mutableStateOf("") }
    var year by remember(uid) { mutableStateOf("") }
    var lockedFields by remember(uid) { mutableStateOf(emptySet<Int>()) }
    var loading by remember(uid) { mutableStateOf(true) }
    var saving by remember(uid) { mutableStateOf(false) }
    var error by remember(uid) { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    DisposableEffect(uid, retry) {
        var active = true
        loading = true; error = null
        if (uid == null) { loading = false; error = "Sign in again to complete your profile." }
        else FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { profile ->
                if (active) {
                    loading = false
                    if (!profile.exists()) error = "Your profile is missing. Contact support."
                    else {
                        lockedFields = listOf(1 to "studentNumber", 2 to "course", 3 to "department")
                            .filter { !profile.get(it.second)?.toString().isNullOrBlank() }.map { it.first }.toSet()
                        name = profile.get("name")?.toString().orEmpty()
                        number = profile.get("studentNumber")?.toString().orEmpty()
                        course = profile.get("course")?.toString().orEmpty()
                        department = profile.get("department")?.toString().orEmpty()
                        year = profile.get("year")?.toString().orEmpty()
                    }
                }
            }.addOnFailureListener { if (active) { loading = false; error = "Unable to load your details. Check your connection and retry." } }
        onDispose { active = false }
    }
    Column(Modifier.fillMaxSize().background(Color(0xFF00254A)).statusBarsPadding().imePadding()
        .verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Complete your student details", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("These details are required before you enter CampusPulse. Your clubs use them to identify their members.", color = Color(0xFFD4DCE5))
        if (loading) CircularProgressIndicator(color = Color(0xFFFF9800))
        error?.let { Text(it, color = Color(0xFFFFB4AB)) }
        listOf("Full name" to name, "Student number" to number, "Programme" to course,
            "Department / Faculty" to department, "Year of study (1–8)" to year).forEachIndexed { index, field ->
            OutlinedTextField(value = field.second, onValueChange = {
                when (index) { 0 -> name = it; 1 -> number = it; 2 -> course = it; 3 -> department = it; 4 -> year = it }
            }, label = { Text(field.first) }, enabled = !loading && !saving && index !in lockedFields, singleLine = true,
                modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFFFF9800), unfocusedLabelColor = Color(0xFFD4DCE5)))
        }
        Button(enabled = !loading && !saving && name.trim().length in 2..80
                && number.trim().matches(Regex("[0-9]{6,20}")) && course.trim().length in 2..150
                && department.trim().length in 2..150 && year.trim().matches(Regex("[1-8]")),
            onClick = {
                saving = true; error = null
                repository.save(name, number, course, department, year,
                    onSuccess = { saving = false; onComplete() }, onError = { saving = false; error = it })
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))) {
            Text(if (saving) "Saving…" else "Save and continue")
        }
        if (error != null && !saving) TextButton(onClick = { retry++ }) { Text("Retry", color = Color(0xFFFF9800)) }
        TextButton(onClick = onLogout, enabled = !saving) { Text("Log out", color = Color.White) }
    }
}
