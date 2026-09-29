package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.service.FcmNotificationManager
import com.example.ui.screens.MainAppScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: HealthViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Firebase Cloud Messaging
    FcmNotificationManager.initialize(applicationContext)

    setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainAppScreen(viewModel = viewModel)
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }
}

