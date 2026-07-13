package com.siamoonga.attendance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.firestoreSettings
import com.siamoonga.attendance.navigation.AppNavigation
import com.siamoonga.attendance.ui.theme.AttendlyTheme
import com.siamoonga.attendance.ui.theme.Ink

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings {
            setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(Long.MAX_VALUE)
                    .build()
            )
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            AttendlyTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Ink)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}