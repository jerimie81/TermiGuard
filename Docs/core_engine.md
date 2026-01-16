# Core Engine

The core engine coordinates the VPN tunnel, SSH session lifecycle, and key
management routines so the app can maintain a high-persistence connection on
Android 15 devices. It is centered around three pillars:

1. **VPN service orchestration**
   - The `TunnelService` is responsible for managing the WireGuard tunnel,
     maintaining foreground service status, and emitting user-visible state via
     notifications.
   - The service keeps track of the last known WireGuard `Config` so it can
     transition the tunnel up/down when the lifecycle changes.

2. **SSH session lifecycle**
   - SSH sessions are expected to be managed by a client manager that can handle
     keep-alive pings, reconnection on transport loss, and safe teardown.
   - Terminal UI components are intended to bind to the SSH session IO streams
     so the user experience remains consistent after reconnections.

3. **Key management and secure storage**
   - Keys are generated and stored through the Android Keystore in the security
     module. WireGuard and SSH materials remain non-exportable.
   - VPN and SSH configurations should be persisted using encrypted storage to
     survive process restarts and device reboots.

## Flow Overview

1. **App launch:** initialize keys and load any cached profiles.
2. **User action:** request VPN permission and start the VPN service.
3. **Foreground operation:** keep the VPN service active with notification
   updates and progress signals.
4. **SSH session:** once VPN connectivity is confirmed, the SSH manager
   establishes sessions and the terminal UI attaches to its streams.
5. **Shutdown:** stop the VPN/SSH services cleanly, update the status, and
   release any wake locks or resources.
