package com.termiguard.a16

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.termiguard.a16.security.KeyStoreProvider
import com.termiguard.a16.vpn.TunnelService

/**
 * Main Entry Point for TermiGuard-A16.
 * Orchestrates the VPN and SSH UI.
 */
class MainActivity : ComponentActivity() {
    private var vpnActive by mutableStateOf(false)
    private val vpnPrepareLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startVpn()
            }
        }

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
                    Dashboard(
                        vpnActive = vpnActive,
                        onToggleVpn = { enabled ->
                            if (enabled) {
                                requestVpnStart()
                            } else {
                                stopVpn()
                            }
                        },
                        onOpenBatterySettings = { openBatterySettings() },
                        onOpenVpnSettings = { openVpnSettings() }
                    )
                }
            }
        }
    }

    private fun requestVpnStart() {
        val prepareIntent = VpnService.prepare(this)
        if (prepareIntent != null) {
            vpnPrepareLauncher.launch(prepareIntent)
        } else {
            startVpn()
        }
    }

    private fun startVpn() {
        val intent = Intent(this, TunnelService::class.java).setAction(TunnelService.ACTION_START)
        ContextCompat.startForegroundService(this, intent)
        vpnActive = true
    }

    private fun stopVpn() {
        val intent = Intent(this, TunnelService::class.java).setAction(TunnelService.ACTION_STOP)
        startService(intent)
        vpnActive = false
    }

    private fun openBatterySettings() {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        startActivity(intent)
    }

    private fun openVpnSettings() {
        val intent = Intent(Settings.ACTION_VPN_SETTINGS)
        startActivity(intent)
    }
}

@Composable
fun Dashboard(
    vpnActive: Boolean,
    onToggleVpn: (Boolean) -> Unit,
    onOpenBatterySettings: () -> Unit,
    onOpenVpnSettings: () -> Unit
) {
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
            onClick = { onToggleVpn(!vpnActive) },
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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenBatterySettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Battery Optimization Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenVpnSettings,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Always-on VPN Settings")
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
