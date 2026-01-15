Project: TermiGuard‑A16

Comprehensive TODO

This TODO document lays out every step required to deliver TermiGuard‑A16—a secure VPN and terminal application for the Samsung SM‑A165M running Android 15—and, if applicable, to integrate an externalized memory service as described in the agent‑learning handover notes.  Tasks are grouped chronologically and by subsystem so that contributors can track progress from project inception through release and maintenance.

Phase 0: Research & Planning


 Understand requirements – Review the project blueprint to familiarize yourself with the target device (SM‑A165M), OS version (Android 15/API 35), primary language (Kotlin), and architecture (arm64‑v8a).  Study the strategic guide for SM‑A165M to understand Samsung’s aggressive memory management, foreground service requirements and battery‑exemption steps.


 Assess memory architecture – Evaluate whether the stateless agent core with externalized event‑sourced memory is within scope.  If it is, read the handover document to understand the event taxonomy, stateless contract and security requirements before beginning implementation.


 Define scope and milestones – Decide which features are required in the first release (VPN, SSH client, terminal UI, key storage, memory gateway) and break them into milestones.  Document acceptance criteria and success metrics.


Phase 1: Project Setup
1.1 Initialize repository


 Create a new Git repository and commit the project blueprint, strategic guide and handover notes.


 Reproduce the directory structure specified in the blueprint: app/src/main/kotlin/vpn, terminal, security, etc., along with scripts for server setup.


 Add a .gitignore suited for Android projects (ignore build output, local properties, IntelliJ settings, etc.).


1.2 Environment setup


 Install Android Studio (or your preferred IDE) and configure the Android 15 (API 35) SDK with Kotlin support.


 Connect a Samsung SM‑A165M device and verify ADB connectivity.  Install appropriate USB drivers if necessary.


 Add Termux‑view as a submodule or dependency; this component provides the terminal emulator UI recommended by the strategic guide.


1.3 Dependency management


 In your Gradle build files, declare the dependencies called out in the blueprint:


com.wireguard.android:tunnel:1.0.20230706 for the WireGuard VPN engine.


org.apache.sshd:sshd-core:2.12.0 for SSH client functionality.


com.termux:termux-view:0.118.0 for the terminal UI.




 Sync the Gradle project and resolve any version conflicts.  Confirm that the dependencies compile successfully.


Phase 2: Core Android Implementation
2.1 VPN Service module


 Create TunnelService.kt – Implement Android’s VpnService to create the TUN interface and manage the WireGuard tunnel.  Follow the strategic guide’s recommendation to justify the service with foregroundServiceType="connectedDevice" (or specialUse) in the manifest and ensure your service runs in the foreground.


 Implement TunnelConfig.kt – Encapsulate all routing rules, MTU settings (around 1280 bytes for 4G) and WireGuard configuration logic as suggested in the blueprint.


 Always‑on & kill switch – Expose a user interface for enabling Always‑on VPN via system settings and call VpnService.Builder.setBlocking(true) to drop traffic outside the tunnel in case of disconnects.  Provide a persistent notification while the tunnel is active.


 Battery exemptions – Programmatically prompt users to set the app’s battery usage to “Unrestricted,” as Samsung devices aggressively kill background services.  Explain why this step is necessary and guide them through Settings.


2.2 Terminal and SSH module


 Implement SshClientManager.kt – Wrap Apache MINA SSHD to manage SSH sessions.  Include keep‑alive packets and automatic reconnection logic when the network changes (e.g., switching cell towers).


 Create TerminalFragment.kt – Integrate the Termux‑view emulator to display a terminal UI.  Ensure correct handling of ANSI escape codes, multi‑touch gestures and 90 Hz refresh rates.


 User interface – Provide a UI for selecting or entering SSH credentials, toggling between multiple hosts and starting/stopping SSH sessions.  Integrate with the key store for credential selection.


2.3 Security and key management


 Implement KeyStoreProvider.kt – Use Android’s Keystore to generate and store Ed25519 (or RSA) keys inside the SM‑A165M’s hardware‑backed trusted execution environment.  Provide functions to retrieve keys for SSH and WireGuard configuration.


 Key management flows – Design flows for creating keys on first launch, importing existing keys, backing up public keys, and deleting keys.  Ensure private keys are never exported from secure storage.


 Secure configuration storage – Safely persist WireGuard configurations and SSH host data (e.g., via encrypted preferences or Jetpack Security library).


2.4 Manifest and permissions


 Add the following permissions to AndroidManifest.xml: android.permission.BIND_VPN_SERVICE, android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE, and android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS (as permitted by Google Play policies for core functionality)


 Declare the VPN service with the appropriate <service> tag, specify android:permission="android.permission.BIND_VPN_SERVICE" and set android:foregroundServiceType="connectedDevice" or specialUse.


 Declare any additional services (e.g., a BroadcastReceiver for Always‑on control) and mark them as non‑exported unless required.


Phase 3: Memory Architecture & Stateless Core (if applicable)
If your project scope includes a stateless agent core with an externalized memory service as described in the handover document, follow these additional tasks:
3.1 Formalize event taxonomy and non‑negotiables


 Finalize the stateless agent contract and append‑only event rules.  Define the event taxonomy and schema versions to ensure consistent serialization.  Decide on the event log transport and persistence layer and design a cold‑archive strategy with hashing for immutability.


3.2 Build the memory gateway and projections


 Develop a memory gateway API that enforces authentication and authorization, ensuring that agents cannot bypass the gateway to access storage directly.


 Implement read‑side projections for conversation context, knowledge search, tool index and policy cache.  Support full replay/rebuild to recover state deterministically.


3.3 Agent refactor & migration


 Refactor existing agents to remove direct database access.  Enforce explicit input/output schemas and write tests to guarantee stateless behaviour.


 Plan and execute a dual‑write migration where events are written to both legacy storage and the new event store.  Validate parity, prepare for cut‑over and decommission the legacy store.


3.4 Security & operations


 Perform threat‑model reviews focusing on memory poisoning, replay attacks, event injection, integrity verification and anomaly detection.


 Prepare operational runbooks, API references, schema documentation and CI checks enforcing statelessness and immutability.


Phase 4: Scripts and backend configuration


 Author scripts/setup_mx_linux.sh to configure an MX Linux host as both the WireGuard VPN endpoint and an OpenSSH server.  Tasks include installing wireguard-tools and openssh-server, generating server keys, defining WireGuard peers and writing firewall rules.


 Document how to run the script on a fresh server and verify connectivity from the Android app.


Phase 5: User interface and experience


 Design a simple home screen that allows the user to connect/disconnect the VPN and launch the terminal.  Show status indicators for VPN and SSH sessions.


 Implement onboarding screens explaining battery optimization, Always‑on VPN, and permissions.  Provide a direct link to Settings so the user can enable “Unrestricted” battery usage and Always‑on VPN.


 Create settings screens for managing VPN profiles, SSH hosts and terminal appearance preferences (font size, colour scheme, etc.).


Phase 6: Testing


 Write unit tests for VPN configuration parsing, SSH connection management, key generation and storage modules.


 Create instrumented tests for SM‑A165M to verify that the VPN tunnel remains active across network changes and that the app survives Samsung’s aggressive memory management.


 Verify that CPU WakeLocks are properly acquired and released on the Helio G99 to keep sessions alive without draining the battery.


 If implementing the external memory service, test event ingestion, replay determinism and system performance under load.


Phase 7: Optimization and hardening


 Profile CPU and battery usage and introduce WakeLocks or ForegroundService optimizations specific to the Helio G99 chipset to keep the app alive in low‑power states.


 Conduct security audits of the VPN and SSH modules.  Ensure strong cipher suites and update dependencies to patch vulnerabilities.


 Educate users about Samsung’s battery management quirks (e.g., turning off Adaptive battery and Put apps to sleep features) and incorporate these recommendations into the onboarding flow.


Phase 8: Documentation and readiness


 Provide comprehensive developer documentation: architectural diagrams, code comments, API references and runbooks for deploying the server component.


 Write the README (see separate file) covering project overview, features, installation, usage and contribution guidelines.


 Create in‑app help or a user manual that explains how to configure and use the VPN and terminal features.


Phase 9: Release and maintenance


 Build a signed release APK or App Bundle.  Test on additional devices to check for compatibility issues beyond the SM‑A165M.


 Publish the app to your distribution channel (internal store, F‑Droid or Play Store) following platform guidelines and policies.


 Set up CI/CD pipelines for automated testing, builds and static analysis.  Plan for periodic updates as new Android versions and device firmware release.


 Gather user feedback, monitor crash reports and battery statistics, and iterate on performance and usability improvements.