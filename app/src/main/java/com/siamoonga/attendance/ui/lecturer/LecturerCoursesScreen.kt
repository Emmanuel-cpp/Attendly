package com.siamoonga.attendance.ui.lecturer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.siamoonga.attendance.model.Course
import com.siamoonga.attendance.model.Session
import com.siamoonga.attendance.ui.theme.CourseAccents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LecturerCoursesScreen(
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
    onSessionStarted: (Session) -> Unit,
    viewModel: LecturerCoursesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "My courses",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (state.courses.isNotEmpty()) {
                            Text(
                                "${state.courses.size} active",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    OverflowMenu(
                        onSettings = onOpenSettings,
                        onLogout = {
                            FirebaseAuth.getInstance().signOut()
                            onLogout()
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddClicked,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add course")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.courses.isEmpty() -> EmptyState()

                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.courses, key = { it.id }) { course ->
                        val accent = CourseAccents[
                            course.id.hashCode().let { if (it < 0) -it else it } % CourseAccents.size
                        ]
                        CourseCard(
                            course = course,
                            accent = accent,
                            isStarting = state.startingCourseId == course.id,
                            onStartSession = { viewModel.startSession(course, onSessionStarted) }
                        )
                    }
                }
            }

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }

    if (state.showAddDialog) {
        AddCourseDialog(state, viewModel)
    }
}

@Composable
private fun OverflowMenu(onSettings: () -> Unit, onLogout: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Settings") },
                leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                onClick = {
                    expanded = false
                    onSettings()
                }
            )
            DropdownMenuItem(
                text = { Text("Logout") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    accent: Color,
    isStarting: Boolean,
    onStartSession: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = course.code.take(2),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = accent
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        course.code,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        course.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.size(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Group,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            "${course.studentCount} students",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onStartSession,
                enabled = !isStarting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(if (isStarting) "Starting…" else "Start session")
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "No courses yet",
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Tap + to create your first course",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun AddCourseDialog(
    state: LecturerCoursesUiState,
    viewModel: LecturerCoursesViewModel
) {
    AlertDialog(
        onDismissRequest = viewModel::onDialogDismiss,
        title = { Text("New course", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                OutlinedTextField(
                    value = state.newCode,
                    onValueChange = viewModel::onCodeChange,
                    label = { Text("Course code (e.g. CS455)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.newName,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Course name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = viewModel::saveCourse,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (state.isSaving) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::onDialogDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(16.dp)
    )
}