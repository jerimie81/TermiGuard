# Detection Logic

The detection logic describes how TermiGuard identifies connectivity changes,
service health issues, and platform-specific constraints that can disrupt long-
running tunnels. The goal is to minimize downtime by proactively responding to
Android 15 lifecycle events and OEM power management behaviors.

## Connectivity Detection

1. **Network transitions**
   - Monitor `ConnectivityManager` callbacks for active network changes.
   - On a transport change (Wi-Fi ↔ cellular), re-validate the VPN tunnel and
     attempt SSH reconnection if the session drops.

2. **VPN state checks**
   - Listen for WireGuard tunnel state changes and surface the status in the
     foreground notification.
   - If the tunnel transitions to `DOWN`, notify the UI and prompt the user to
     reconnect.

3. **Foreground service health**
   - If the VPN service is stopped by the system, log the termination reason and
     request a restart via the UI.

## Device Power Management

1. **Battery optimization status**
   - Detect whether the app is in the "Unrestricted" battery bucket.
   - If not, display guidance to open the system settings and whitelist the app.

2. **Always-on VPN settings**
   - Detect whether Always-on VPN is enabled and, if missing, link the user to
     the system VPN settings.

## Logging & Telemetry (Local)

The detection layer should emit local logs for:

- VPN state transitions (UP/DOWN/RECONNECTING).
- SSH session connect/disconnect events.
- Network transport switches.

These logs support troubleshooting without storing sensitive payloads.
