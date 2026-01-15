#!/bin/bash

# TermiGuard-A16: MX Linux Server Setup Script
# Configures WireGuard VPN endpoint and hardened OpenSSH server.

set -e

echo "Starting TermiGuard-A16 Server Setup..."

# 1. Install Dependencies
sudo apt update
sudo apt install -y wireguard openssh-server iptables

# 2. Configure WireGuard
WG_PRIVATE_KEY=$(wg genkey)
WG_PUBLIC_KEY=$(echo $WG_PRIVATE_KEY | wg pubkey)

cat <<EOF | sudo tee /etc/wireguard/wg0.conf
[Interface]
PrivateKey = $WG_PRIVATE_KEY
Address = 10.0.0.1/24
ListenPort = 51820
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE

# Client Placeholder (Update with device public key)
#[Peer]
#PublicKey = <DEVICE_PUBLIC_KEY>
#AllowedIPs = 10.0.0.2/32
EOF

sudo systemctl enable wg-quick@wg0
sudo systemctl start wg-quick@wg0

# 3. Harden OpenSSH
sudo sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo sed -i 's/#PubkeyAuthentication yes/PubkeyAuthentication yes/' /etc/ssh/sshd_config
# Add high-persistence keep-alive settings
echo "ClientAliveInterval 60" | sudo tee -a /etc/ssh/sshd_config
echo "ClientAliveCountMax 3" | sudo tee -a /etc/ssh/sshd_config

sudo systemctl restart ssh

echo "-----------------------------------------------"
echo "Setup Complete!"
echo "Server WireGuard Public Key: $WG_PUBLIC_KEY"
echo "WireGuard Port: 51820 (UDP)"
echo "SSH Port: 22 (TCP)"
echo "-----------------------------------------------"
echo "Next: Add your device's public key to /etc/wireguard/wg0.conf"
