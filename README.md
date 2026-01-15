# TermiGuard-A16

**TermiGuard-A16** is a specialized, high-persistence networking and terminal utility engineered for **Android 15 (API 35)**. While designed for the **Samsung SM-A165M** (Helio G99), its architecture is device-agnostic, ensuring secure and uninterrupted VPN/SSH sessions on the most restrictive modern mobile environments.

## 🚀 The Core Problem
Modern mobile OSs, particularly Samsung's **"Chimera"** service, aggressively terminate background processes to save battery. TermiGuard-A16 is built to survive these conditions, providing a reliable bridge for remote server administration.

## ✨ Key Features
*   **Persistent WireGuard VPN:** Battery-efficient encrypted tunneling via `com.wireguard.android`, optimized with a **1280 MTU** for cellular stability.
*   **Robust SSH Terminal:** Integrated **Apache MINA SSHD** client with **Termux-view** UI. Features custom keep-alive packets (`SSH_MSG_IGNORE`) every 45s to keep carrier NAT tables open.
*   **Hardware-Backed Security:** Cryptographic keys (Ed25519/AES) are generated and stored within the device's **Trusted Execution Environment (TEE)**, ensuring they are non-exportable.
*   **Always-on Connectivity:** Built-in support for system-level Always-on VPN and automatic reconnection logic for network handovers (Wi-Fi to 4G/5G).

## 🛠 Technical Specifications
*   **Target OS:** Android 15 (API 35)
*   **Architecture:** arm64-v8a (64-bit only)
*   **Memory Alignment:** Fully **16 KB page-aligned** native binaries (mandatory for Android 15 performance).
*   **Persistence Strategy:** 
    *   Uses `FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE` to justify long-running sessions.
    *   Programmatic prompts for **"Unrestricted"** battery usage.
    *   Strategic use of **CPU WakeLocks** during active data transfers.

## 📁 Repository Structure
```text
app/src/main/kotlin/
├── vpn/            # WireGuard TunnelService and VpnService logic
├── terminal/       # SSH Client Manager and Termux-view integration
├── security/       # Hardware-backed KeyStoreProvider (Ed25519/AES)
scripts/
└── setup_mx_linux.sh # Server-side WireGuard/SSH configuration script
```

## 🏗 Build Requirements
*   **Android Studio:** Ladybug (or later)
*   **NDK:** r28+ (for 16 KB page alignment)
*   **Linker Flags:** `-Wl,-z,max-page-size=16384`

## 🛡 Server-Side Setup (MX Linux)
A remote gateway must be prepared to accept connections. Use the provided script:
```bash
ssh username@your-server
sudo bash ./scripts/setup_mx_linux.sh
```
This script automates:
1. WireGuard peer configuration.
2. OpenSSH hardening (Ed25519 only).
3. NAT/IP Forwarding setup.

## 📱 Samsung SM-A165M Optimization
To guarantee 100% session persistence on Samsung devices:
1. **Battery Settings:** Set TermiGuard-A16 to **"Unrestricted"**.
2. **Always-on VPN:** Enable in `Settings > Connections > More > VPN`.
3. **Adaptive Battery:** It is recommended to disable "Auto-optimize daily" in Device Care.

## 📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgements
*   [WireGuard](https://www.wireguard.com/)
*   [Apache MINA SSHD](https://mina.apache.org/sshd-project/)
*   [Termux](https://termux.dev/)
*   [DontKillMyApp](https://dontkillmyapp.com
