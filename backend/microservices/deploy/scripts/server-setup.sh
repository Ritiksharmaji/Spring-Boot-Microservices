#!/usr/bin/env bash
# =============================================================================
# One-time setup of a fresh AWS Lightsail Ubuntu 24.04 instance (run as the default
# "ubuntu" user, with sudo). Installs Docker, adds swap, hardens SSH/firewall,
# and clones the repository.
#
#   curl -fsSL https://raw.githubusercontent.com/Ritiksharmaji/Spring-Boot-Microservices/main/backend/microservices/deploy/scripts/server-setup.sh -o setup.sh
#   less setup.sh      # read it first
#   bash setup.sh
# =============================================================================
set -euo pipefail

REPO_URL=${REPO_URL:-https://github.com/Ritiksharmaji/Spring-Boot-Microservices.git}
SWAP_SIZE=${SWAP_SIZE:-4G}

echo "==> System update"
sudo apt-get update -y
sudo DEBIAN_FRONTEND=noninteractive apt-get upgrade -y
sudo apt-get install -y ca-certificates curl gnupg git ufw fail2ban unattended-upgrades jq

echo "==> Docker Engine + Compose plugin (official apt repository)"
if ! command -v docker >/dev/null; then
  sudo install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  sudo chmod a+r /etc/apt/keyrings/docker.gpg
  echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    | sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
  sudo apt-get update -y
  sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
  sudo usermod -aG docker "$USER"
fi
sudo systemctl enable --now docker

echo "==> Swap ($SWAP_SIZE) as a safety net for memory spikes"
if ! swapon --show | grep -q /swapfile; then
  sudo fallocate -l "$SWAP_SIZE" /swapfile
  sudo chmod 600 /swapfile
  sudo mkswap /swapfile
  sudo swapon /swapfile
  echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
  echo 'vm.swappiness=10' | sudo tee /etc/sysctl.d/99-swappiness.conf
  sudo sysctl --system >/dev/null
fi

echo "==> Firewall: only SSH, HTTP, HTTPS"
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow OpenSSH
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable

echo "==> SSH hardening (key-only login) and fail2ban"
sudo sed -i 's/^#\?PasswordAuthentication .*/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo systemctl restart ssh || sudo systemctl restart sshd
sudo systemctl enable --now fail2ban
sudo dpkg-reconfigure -f noninteractive unattended-upgrades

echo "==> Docker log rotation defaults"
echo '{ "log-driver": "json-file", "log-opts": { "max-size": "20m", "max-file": "5" } }' | sudo tee /etc/docker/daemon.json >/dev/null
sudo systemctl restart docker

echo "==> Repository"
if [ ! -d "$HOME/ecommerce/.git" ]; then
  git clone "$REPO_URL" "$HOME/ecommerce"
fi

cat <<MSG

Server ready. Next steps (see backend/microservices/docs/DEPLOYMENT.md):
  1. Log out and back in (docker group), then:  cd ~/ecommerce/backend/microservices/deploy
  2. cp .env.prod.example .env.prod && chmod 600 .env.prod && nano .env.prod
  3. ./scripts/init-letsencrypt.sh      # TLS certificates for API_DOMAIN and AUTH_DOMAIN
  4. ./scripts/deploy.sh                # pull images and start everything
MSG
