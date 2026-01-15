package com.termiguard.a16.vpn

import com.wireguard.config.Config
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import java.net.InetAddress

/**
 * Encapsulates WireGuard routing rules and MTU settings.
 * Optimized for cellular stability on 4G/LTE/5G networks.
 */
data class TunnelConfig(
    val privateKey: String,
    val address: String,
    val dns: String = "1.1.1.1",
    val mtu: Int = 1280, // Suggested MTU for 4G to prevent fragmentation
    val peers: List<TunnelPeer>
) {
    data class TunnelPeer(
        val publicKey: String,
        val endpoint: String,
        val allowedIps: String = "0.0.0.0/0"
    )

    fun toWireGuardConfig(): Config {
        val interfaceBuilder = Interface.Builder()
            .addAddress(address)
            .parsePrivateKey(privateKey)
            .addDnsServer(dns)
            .setMtu(mtu)

        val configBuilder = Config.Builder()
            .setInterface(interfaceBuilder.build())

        peers.forEach { peer ->
            val peerBuilder = Peer.Builder()
                .parsePublicKey(peer.publicKey)
                .parseEndpoint(peer.endpoint)
                .addAllowedIp(peer.allowedIps)
            configBuilder.addPeer(peerBuilder.build())
        }

        return configBuilder.build()
    }
}
