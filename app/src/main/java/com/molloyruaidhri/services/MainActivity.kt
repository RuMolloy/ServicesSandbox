package com.molloyruaidhri.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.molloyruaidhri.services.Constants.ACTION_PAUSE_SERVICE
import com.molloyruaidhri.services.Constants.ACTION_START_OR_RESUME_SERVICE
import com.molloyruaidhri.services.ui.theme.ServicesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServicesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ServiceUI(context = LocalContext.current)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun ServiceUI(context: Context) {
    val backgroundServiceStatus = remember { mutableStateOf(false) }
    val backgroundServiceButton = remember { mutableStateOf("Start Background Service") }

    val foregroundServiceStatus = remember { mutableStateOf(false) }
    val foregroundServiceButton = remember { mutableStateOf("Start Foreground Service") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = {
            if(backgroundServiceStatus.value) {
                context.stopService(Intent(context, BackgroundService::class.java))
                backgroundServiceButton.value = "Start Background Service"
            }
            else{
                context.startService(Intent(context, BackgroundService::class.java))
                backgroundServiceButton.value = "Stop Background Service"
            }
            backgroundServiceStatus.value = !backgroundServiceStatus.value
        }) {
            Text(text = backgroundServiceButton.value)
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            if(foregroundServiceStatus.value) {
                Intent(context, ForegroundService::class.java).also {
                    it.action = ACTION_PAUSE_SERVICE
                    context.startService(it)
                }
                foregroundServiceButton.value = "Start Foreground Service"
            }
            else {
                Intent(context, ForegroundService::class.java).also {
                    it.action = ACTION_START_OR_RESUME_SERVICE
                    context.startService(it)
                }
                foregroundServiceButton.value = "Stop Foreground Service"
            }
            foregroundServiceStatus.value = !foregroundServiceStatus.value
        }) {
            Text(text = foregroundServiceButton.value)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ServicesTheme {
        Greeting("Android")
    }
}