package com.termiguard.a16.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config

/**
 * Core VpnService implementation.
 * Manages the WireGuard tunnel and ensures foreground persistence on Android 15.
 */
class TunnelService : VpnService(), Tunnel {

    private var tunnelBackend: GoBackend? = null
    private var currentConfig: Config? = null
    private var currentState: Tunnel.State = Tunnel.State.DOWN

    companion object {
        const val ACTION_START = "com.termiguard.a16.vpn.START"
        const val ACTION_STOP = "com.termiguard.a16.vpn.STOP"
        private const val NOTIFICATION_CHANNEL_ID = "termiguard_vpn"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        tunnelBackend = GoBackend(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // High-Persistence: Ensure the service stays in the foreground
        // On Android 15, this requires a valid notification and foregroundServiceType
        when (intent?.action) {
            ACTION_STOP -> {
                stopTunnel()
                stopSelf()
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification("Connecting"))
                startTunnel()
            }
        }
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
            currentConfig = config

            tunnelBackend?.setState(this, Tunnel.State.UP, config)
            Log.i("TunnelService", "VPN Tunnel established and WireGuard state set")

        } catch (e: Exception) {
            Log.e("TunnelService", "Failed to start tunnel", e)
            stopSelf()
        }
    }

    private fun stopTunnel() {
        try {
            tunnelBackend?.setState(this, Tunnel.State.DOWN, currentConfig)
        } catch (e: Exception) {
            Log.e("TunnelService", "Failed to stop tunnel", e)
        } finally {
            currentState = Tunnel.State.DOWN
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTunnel()
    }

    override fun getName(): String = "TermiGuardTunnel"

    override fun onStateChange(newState: Tunnel.State) {
        currentState = newState
        val status = if (newState == Tunnel.State.UP) "Connected" else "Disconnected"
        val notification = buildNotification(status)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
        Log.i("TunnelService", "Tunnel state changed: $newState")
    }

    private fun buildNotification(status: String) =
        NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("TermiGuard VPN")
            .setContentText("Status: $status")
            .setSmallIcon(android.R.drawable.stat_sys_vpn_ic)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "TermiGuard VPN",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
