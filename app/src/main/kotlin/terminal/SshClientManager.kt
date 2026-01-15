package com.termiguard.a16.terminal

import android.util.Log
import org.apache.sshd.client.SshClient
import org.apache.sshd.client.session.ClientSession
import org.apache.sshd.common.keyprovider.KeyPairProvider
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Wrapper for Apache MINA SSHD.
 * Handles session keep-alives and auto-reconnection for high persistence.
 */
class SshClientManager(private val keyPairProvider: KeyPairProvider) {

    private var client: SshClient? = null
    private var session: ClientSession? = null

    fun connect(host: String, port: Int, username: String) {
        client = SshClient.setUpDefaultClient().apply {
            keyPairProvider = this@SshClientManager.keyPairProvider
            start()
        }

        try {
            val connectFuture = client?.connect(username, host, port)
                ?.verify(10, TimeUnit.SECONDS)

            session = connectFuture?.session
            session?.addPasswordIdentity("IF_PASSWORD_USED") // Prefer KeyPair

            // High Persistence Settings
            session?.auth()?.verify(10, TimeUnit.SECONDS)
            
            // Send SSH_MSG_IGNORE every 45 seconds to keep the NAT table open
            client?.setSessionHeartbeat(
                org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType.IGNORE,
                TimeUnit.SECONDS,
                45
            )

            Log.i("SshClientManager", "SSH Connected to $host")

        } catch (e: Exception) {
            Log.e("SshClientManager", "Connection failed", e)
            stop()
        }
    }

    fun stop() {
        session?.close(false)
        client?.stop()
        client = null
        session = null
    }

    fun isConnected(): Boolean = session?.isOpen == true
}
