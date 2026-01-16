# Platform Support

TermiGuard is optimized for Android 15 (API 35) and devices with aggressive
power management policies. The following guidance defines the supported
platform scope and the expected behavior on compatible devices.

## Primary Target

- **Samsung SM-A165M (Helio G99)**
  - Requires a foreground service with the connected-device type.
  - Benefits from explicit battery optimization exemptions.
  - Tested assumptions: Always-on VPN support and 16 KB page alignment.

## General Android 15 Devices

- **Android 15 (API 35)**
  - Requires 16 KB page-aligned native binaries.
  - Requires a persistent foreground notification for long-running VPN tunnels.

## Compatibility Notes

- **Android 14 and below**
  - Not a target in the current roadmap. If support is desired, validate the
    WireGuard backend, keystore capabilities, and foreground service behavior.

- **OEM-specific power management**
  - Samsung, Xiaomi, Oppo, and OnePlus devices require user guidance to disable
    aggressive battery optimizations.

## Recommended Validation Matrix

| Device Class | OS Version | Validation Focus |
| --- | --- | --- |
| Samsung SM-A165M | Android 15 | VPN persistence, battery exemptions |
| Pixel (A series) | Android 15 | VPN setup, foreground service behavior |
| OnePlus (mid-range) | Android 15 | Network handover stability |
