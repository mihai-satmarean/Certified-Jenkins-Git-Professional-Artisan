# Lab 1.1: Jenkins Docker Integration - Overview

## Lab Overview
**Duration**: 2 hours  
**Difficulty**: Advanced  
**Prerequisites**: Jenkins & Git Associate certification, Docker knowledge, SSH access

## Learning Objectives
By the end of this lab, you will be able to:
- Configure Jenkins to run jobs on remote Docker hosts
- Set up SSH integration for container-based builds
- Implement Docker-in-Docker (DinD) scenarios
- Manage Docker images and containers through Jenkins
- Troubleshoot Docker integration issues

## Lab Environment Setup

### Infrastructure Overview
- **Jenkins Master**: `instructor-jenkins-master-1` (t4g.medium, 2 vCPU, 4GB RAM)
- **Docker Host**: `instructor-docker-host-1` (t4g.small, 2 vCPU, 2GB RAM)
- **Build Agent**: `instructor-build-agent-1` (t4g.small, 2 vCPU, 2GB RAM)
- **Ansible Control**: `instructor-ansible-control-1` (t4g.small, 2 vCPU, 2GB RAM)

### Pre-configured via Ansible
- ✅ SSH keys generated and distributed
- ✅ Docker installed on all relevant hosts
- ✅ Jenkins plugins (Git, SSH Agent, SSH Slaves, Ansible)
- ✅ User permissions configured
- ✅ Network connectivity established

### Access Information
- **Jenkins URL**: `http://instructor-jenkins-master-1:8080`
- **Jenkins Version**: 2.516.2 (LTS)
- **SSH User**: `ubuntu` (password: `LetMe1n!`)
- **SSH Keys**: Generated in `/var/lib/jenkins/.ssh/` and `/home/ubuntu/.ssh/`

**Note**: This lab is designed for Jenkins 2.516.2. Some UI elements and features may differ in other versions.

## Lab Structure

This lab is divided into the following steps:

1. **[Step 1: SSH Setup](lab-1.1-step1-ssh-setup.md)** - Configure SSH connections
2. **[Step 2: Jenkins Configuration](lab-1.1-step2-jenkins-config.md)** - Configure Jenkins for Docker
3. **[Step 3: Docker Pipeline](lab-1.1-step3-docker-pipeline.md)** - Create Docker integration pipeline
4. **[Step 4: Advanced Scenarios](lab-1.1-step4-advanced-scenarios.md)** - Advanced Docker scenarios
5. **[Troubleshooting](lab-1.1-troubleshooting.md)** - Common issues and solutions
6. **[Summary](lab-1.1-summary.md)** - Key learning points and next steps

## Environment Architecture

```
┌─────────────────┐    SSH     ┌─────────────────┐
│   Jenkins       │◄──────────►│   Docker Host   │
│   Master        │            │   (Remote)      │
│                 │            │                 │
│ - Docker Plugin │            │ - Docker Daemon │
│ - SSH Client    │            │ - SSH Server    │
│ - Build Agents  │            │ - Build Tools   │
└─────────────────┘            └─────────────────┘
```

## Prerequisites Checklist

Before starting this lab, ensure you have:
- [ ] Access to Jenkins master web interface
- [ ] SSH access to Docker host
- [ ] Docker knowledge (basic commands)
- [ ] Understanding of Jenkins pipelines
- [ ] Basic SSH key management knowledge

## Next Steps

Proceed to [Step 1: SSH Setup](lab-1.1-step1-ssh-setup.md) to begin the lab.
