package com.example.scrollstop

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.scrollstop.ui.theme.ScrollStopTheme
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.Button
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScrollStopTheme {
                val usageViewModel: UsageViewModel = viewModel()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                            containerColor = Color(0xFF0B0D0B)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        DashboardScreen(viewModel = usageViewModel)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { openOverlaySettings() },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Enable Floating Timer")
                        }
                    }
                }
            }
        }

    }

    private fun openOverlaySettings() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            startService(Intent(this, FloatingTimerService::class.java))
            Toast.makeText(
                this,
                "Floating timer started!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScrollStopTheme {
        Greeting("Android")
    }
}