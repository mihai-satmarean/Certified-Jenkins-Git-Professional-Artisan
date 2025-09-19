# Lab 1.1: Step 1 - SSH Setup

## Overview
This step covers configuring SSH connections between Jenkins master and Docker hosts for secure, passwordless authentication.

## Step 1: Configure SSH Connection to Docker Host

### 1.1 Automated SSH Key Setup (Recommended)

Run the Ansible playbook to setup SSH keys automatically:

```bash
# Run the Ansible playbook to setup SSH keys automatically
ansible-playbook setup-ssh-keys.yml -i inventory.ini -v
```

This playbook will:
- Generate SSH key pairs for both `jenkins` and `ubuntu` users
- Configure SSH settings for passwordless connections
- Display public keys for manual distribution if needed
- Setup authorized_keys on target hosts

### 1.2 Manual SSH Key Setup (Alternative)

If you prefer manual setup:

```bash
# On Jenkins master
ssh-keygen -t rsa -b 4096 -C "jenkins-docker-integration"

# Copy public key to Docker host
ssh-copy-id -i ~/.ssh/id_rsa.pub ubuntu@docker-host-ip

# Test SSH connection
ssh ubuntu@docker-host-ip "docker --version"
```

### 1.3 Verify SSH Connection

```bash
# Test SSH connection from Jenkins master to Docker host
ssh ubuntu@docker-host-ip "docker --version && docker ps"
```

**Expected Results:**
- SSH connection should work without password prompt
- Docker commands should execute successfully
- No permission denied errors

## Troubleshooting SSH Issues

### SSH Authentication Failed
```bash
# 1. Verify SSH keys were generated correctly
sudo ls -la /var/lib/jenkins/.ssh/
sudo cat /var/lib/jenkins/.ssh/id_rsa.pub

# 2. Check if public key is in authorized_keys on target host
ssh ubuntu@docker-host-ip "cat ~/.ssh/authorized_keys"

# 3. Test SSH connection manually
ssh ubuntu@docker-host-ip "echo 'SSH test successful'"

# 4. If manual SSH works but Jenkins doesn't, check key format
sudo cat /var/lib/jenkins/.ssh/id_rsa | head -1
# Should show: -----BEGIN OPENSSH PRIVATE KEY----- or -----BEGIN RSA PRIVATE KEY-----
```

### Fix SSH Key Issues
```bash
# Option 1: Regenerate SSH keys
ansible-playbook setup-ssh-keys.yml -i inventory.ini -v

# Option 2: Manual key setup
sudo -u jenkins ssh-keygen -t rsa -b 4096 -f /var/lib/jenkins/.ssh/id_rsa -N ""
sudo -u jenkins ssh-copy-id -i /var/lib/jenkins/.ssh/id_rsa.pub ubuntu@docker-host-ip
```

## Success Criteria

- [ ] SSH connection works without password prompt
- [ ] Docker commands execute successfully on remote host
- [ ] No permission denied errors
- [ ] SSH keys are properly generated and distributed

## Next Steps

Once SSH setup is complete, proceed to [Step 2: Jenkins Configuration](lab-1.1-step2-jenkins-config.md).
