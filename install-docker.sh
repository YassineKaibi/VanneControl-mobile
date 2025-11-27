#!/bin/bash

# Docker Installation Script for WSL2 (Ubuntu)
# Run this script with: bash install-docker.sh

set -e  # Exit on error

echo "======================================"
echo "Docker & Docker Compose Installation"
echo "======================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Step 1: Update package lists
echo -e "${YELLOW}[1/7] Updating package lists...${NC}"
sudo apt-get update

# Step 2: Install prerequisites
echo -e "${YELLOW}[2/7] Installing prerequisites...${NC}"
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

# Step 3: Add Docker's official GPG key
echo -e "${YELLOW}[3/7] Adding Docker's official GPG key...${NC}"
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Step 4: Set up Docker repository
echo -e "${YELLOW}[4/7] Setting up Docker repository...${NC}"
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Step 5: Update package lists again
echo -e "${YELLOW}[5/7] Updating package lists with Docker repo...${NC}"
sudo apt-get update

# Step 6: Install Docker Engine and Docker Compose
echo -e "${YELLOW}[6/7] Installing Docker Engine and Docker Compose...${NC}"
sudo apt-get install -y \
    docker-ce \
    docker-ce-cli \
    containerd.io \
    docker-buildx-plugin \
    docker-compose-plugin

# Step 7: Start Docker service
echo -e "${YELLOW}[7/7] Starting Docker service...${NC}"
sudo service docker start

# Add current user to docker group (optional, requires logout/login to take effect)
echo -e "${YELLOW}Adding current user to docker group...${NC}"
sudo usermod -aG docker $USER

echo ""
echo -e "${GREEN}======================================"
echo "✅ Installation Complete!"
echo "======================================${NC}"
echo ""
echo "Docker Version:"
sudo docker --version
echo ""
echo "Docker Compose Version:"
sudo docker compose version
echo ""
echo -e "${YELLOW}⚠️  IMPORTANT:${NC}"
echo "To use Docker without sudo, you need to:"
echo "1. Log out and log back in to WSL"
echo "2. Or run: newgrp docker"
echo ""
echo -e "${GREEN}Testing Docker installation...${NC}"
sudo docker run --rm hello-world

echo ""
echo -e "${GREEN}✅ Docker is ready to use!${NC}"
echo ""
