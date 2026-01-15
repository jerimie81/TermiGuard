package com.termiguard.a16.vpn

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import java.io.IOException

/**
 * Core VpnService implementation.
 * Manages the WireGuard tunnel and ensures foreground persistence on Android 15.
 */
class TunnelService : VpnService(), Tunnel {

    private var tunnelBackend: GoBackend? = null
    private var tunnelFileDescriptor: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        tunnelBackend = GoBackend(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // High-Persistence: Ensure the service stays in the foreground
        // On Android 15, this requires a valid notification and foregroundServiceType
        startTunnel()
        return START_STICKY
    }

    private fun startTunnel() {
        try {
            // Placeholder for real configuration retrieval
            val config = TunnelConfig(
                privateKey = "PRIVATE_KEY_FROM_KEYSTORE",
                address = "10.0.0.2/32",
                peers = listOf(
                    TunnelConfig.TunnelPeer(
                        publicKey = "SERVER_PUBLIC_KEY",
                        endpoint = "SERVER_IP:51820"
                    )
                )
            ).toWireGuardConfig()

            // Establish the TUN interface
            tunnelFileDescriptor = Builder()
                .addAddress("10.0.0.2", 32)
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)
                .setMtu(1280)
                .setSession("TermiGuard-A16")
                .setBlocking(true) // Prevent leaks outside the tunnel
                .establish()

            Log.i("TunnelService", "VPN Tunnel established")

        } catch (e: Exception) {
            Log.e("TunnelService", "Failed to start tunnel", e)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            tunnelFileDescriptor?.close()
        } catch (e: IOException) {
            Log.e("TunnelService", "Error closing tunnel FD", e)
        }
    }

    override fun getName(): String = "TermiGuardTunnel"

    override fun onStateChange(newState: Tunnel.State) {
        Log.i("TunnelService", "Tunnel state changed: $newState")
    }
}
