package com.termiguard.a16

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.termiguard.a16.security.KeyStoreProvider

/**
 * Main Entry Point for TermiGuard-A16.
 * Orchestrates the VPN and SSH UI.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Keys on first run
        KeyStoreProvider.getOrCreateSshKeyPair()
        KeyStoreProvider.getOrCreateWgKeyPair()

        setContent {
            TermiGuardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Dashboard()
                }
            }
        }
    }
}

@Composable
fun Dashboard() {
    var vpnActive by remember { mutableStateOf(false) }
    var sshActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TermiGuard-A16",
            style = MaterialTheme.typography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { vpnActive = !vpnActive },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (vpnActive) "Disconnect VPN" else "Connect VPN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { sshActive = !sshActive },
            modifier = Modifier.fillMaxWidth(),
            enabled = vpnActive // Best practice: SSH through VPN
        ) {
            Text(if (sshActive) "Close Terminal" else "Open SSH Terminal")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Status: ${if (vpnActive) "Protected" else "Unsecured"}",
            color = if (vpnActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun TermiGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}
