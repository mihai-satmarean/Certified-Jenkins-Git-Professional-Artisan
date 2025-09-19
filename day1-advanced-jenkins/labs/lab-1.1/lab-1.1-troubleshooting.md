# Lab 1.1: Troubleshooting

## Overview
This document covers common issues and solutions for Jenkins Docker integration.

## Common Issues and Solutions

### SSH Connection Issues

#### SSH Authentication Failed (Server rejected private key)
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

#### Fix SSH Key Issues
```bash
# Option 1: Regenerate SSH keys
ansible-playbook setup-ssh-keys.yml -i inventory.ini -v

# Option 2: Manual key setup
sudo -u jenkins ssh-keygen -t rsa -b 4096 -f /var/lib/jenkins/.ssh/id_rsa -N ""
sudo -u jenkins ssh-copy-id -i /var/lib/jenkins/.ssh/id_rsa.pub ubuntu@docker-host-ip
```

### Jenkins Node Issues

#### Node Fails to Connect
- Check SSH credentials and network connectivity
- Verify Docker is installed on the target host
- Check if ubuntu user is in docker group
- Review node log for specific error messages

#### Connection Timeout
- Check security groups allow SSH (port 22)
- Verify network connectivity between Jenkins master and Docker host

#### Node Runs on Wrong Host
- Verify the node label is set to `docker`
- Check that the `docker-host-agent` node has the `docker` label
- Ensure the node is online and connected

### Docker Issues

#### Docker Permission Denied
```bash
# Ensure ubuntu user is in docker group
ssh ubuntu@docker-host-ip "groups ubuntu"

# If not in docker group, add it
ssh ubuntu@docker-host-ip "sudo usermod -aG docker ubuntu"
```

#### Docker Not Found
```bash
# Check if Docker is installed
ssh ubuntu@docker-host-ip "docker --version"

# Check Docker daemon status
ssh ubuntu@docker-host-ip "sudo systemctl status docker"

# Start Docker daemon if not running
ssh ubuntu@docker-host-ip "sudo systemctl start docker"
```

#### Docker Build Fails
```bash
# Check available disk space
ssh ubuntu@docker-host-ip "df -h"

# Check Docker build context
ssh ubuntu@docker-host-ip "ls -la /home/ubuntu/jenkins-agent/workspace/docker-integration-pipeline/app/"

# Check Docker daemon logs
ssh ubuntu@docker-host-ip "sudo journalctl -u docker.service"
```

### Pipeline Issues

#### Pipeline Fails at "Docker Build" Stage
```bash
# Check if Docker is available on the node
ssh ubuntu@docker-host-ip "docker --version"

# Check if Docker daemon is running
ssh ubuntu@docker-host-ip "sudo systemctl status docker"
```

#### Pipeline Fails at "Test Container" Stage
```bash
# Check if container is running
ssh ubuntu@docker-host-ip "docker ps"

# Check container logs
ssh ubuntu@docker-host-ip "docker logs test-container"

# Test HTTP manually
ssh ubuntu@docker-host-ip "curl -v http://localhost:3000/health"
```

#### Network Issues
```bash
# Check if port 3000 is available
ssh ubuntu@docker-host-ip "netstat -tlnp | grep 3000"

# Test with different port
docker run --rm -d --name test-container -p 3001:3000 jenkins-docker-app:latest
curl http://localhost:3001/health
```

### Plugin Issues

#### Plugin Installation Fails
- Check Jenkins version compatibility
- Install plugins manually through Jenkins UI
- Restart Jenkins after plugin installation
- Check Jenkins logs for plugin errors

#### Plugin Dependencies Missing
- Install plugins through Jenkins UI (handles dependencies automatically)
- Check plugin compatibility matrix
- Update Jenkins to latest LTS version

### Performance Issues

#### Slow Docker Builds
```bash
# Check available resources
ssh ubuntu@docker-host-ip "free -h"
ssh ubuntu@docker-host-ip "df -h"

# Clean up Docker system
ssh ubuntu@docker-host-ip "docker system prune -a"

# Check Docker daemon configuration
ssh ubuntu@docker-host-ip "sudo cat /etc/docker/daemon.json"
```

#### High Memory Usage
- Increase JVM options for Jenkins agents
- Monitor Docker container resource usage
- Implement proper cleanup in pipelines

## Diagnostic Commands

### System Health Check
```bash
# Check all services status
ssh ubuntu@docker-host-ip "sudo systemctl status docker jenkins"

# Check disk space
ssh ubuntu@docker-host-ip "df -h"

# Check memory usage
ssh ubuntu@docker-host-ip "free -h"

# Check network connectivity
ssh ubuntu@docker-host-ip "ping -c 3 jenkins-master-ip"
```

### Docker Health Check
```bash
# Check Docker version and info
ssh ubuntu@docker-host-ip "docker --version && docker info"

# List all containers and images
ssh ubuntu@docker-host-ip "docker ps -a && docker images"

# Check Docker daemon logs
ssh ubuntu@docker-host-ip "sudo journalctl -u docker.service --no-pager"
```

### Jenkins Health Check
```bash
# Check Jenkins service status
ssh ubuntu@jenkins-master-ip "sudo systemctl status jenkins"

# Check Jenkins logs
ssh ubuntu@jenkins-master-ip "sudo tail -f /var/log/jenkins/jenkins.log"

# Check Jenkins node status
curl -u admin:password http://jenkins-master-ip:8080/computer/api/json
```

## Cleanup Procedures

### Docker Cleanup
```bash
# Stop all running containers
docker stop $(docker ps -aq)

# Remove all containers
docker rm $(docker ps -aq)

# Remove all images
docker rmi $(docker images -q)

# Clean up Docker system
docker system prune -a
```

### Jenkins Cleanup
- Remove test jobs and pipelines
- Clean up build history
- Remove temporary credentials
- Clear workspace directories

## Getting Help

### Log Locations
- **Jenkins logs**: `/var/log/jenkins/jenkins.log`
- **Docker logs**: `sudo journalctl -u docker.service`
- **SSH logs**: `/var/log/auth.log`
- **System logs**: `/var/log/syslog`

### Useful Resources
- [Jenkins Docker Plugin Documentation](https://plugins.jenkins.io/docker-plugin/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [SSH Plugin Documentation](https://plugins.jenkins.io/ssh-slaves/)
- [GitHub Repository](https://github.com/mihai-satmarean/Certified-Jenkins-Git-Professional-Artisan)

## Next Steps

Once troubleshooting is complete, proceed to [Summary](lab-1.1-summary.md) for key learning points and next steps.
