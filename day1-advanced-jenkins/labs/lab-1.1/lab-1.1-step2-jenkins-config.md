# Lab 1.1: Step 2 - Jenkins Configuration

## Overview
This step covers configuring Jenkins for Docker integration, including plugin installation and build agent setup.

## Step 2: Configure Jenkins for Docker Integration

### 2.1 Install Required Plugins in Jenkins Master

**Essential plugins already installed via Ansible:**
- Git Plugin ✅
- SSH Agent Plugin ✅  
- SSH Slaves Plugin ✅
- Ansible Plugin ✅

**Additional plugins to install manually via Jenkins UI:**
1. Go to: `http://jenkins-master-ip:8080/pluginManager/`
2. Install these plugins:
   - **Docker Pipeline** (docker-workflow)
   - **Pipeline** (workflow-aggregator) 
   - **GitHub** (github)
   - **Maven Integration** (maven-plugin)
   - **Blue Ocean** (blueocean) - optional

**Note:** Some plugins have complex dependencies that are better installed through Jenkins UI which handles dependencies automatically.

### 2.2 Configure SSH Credentials in Jenkins

**Step 1: Access Jenkins Credentials Manager**
1. Open Jenkins web interface: `http://jenkins-master-ip:8080`
2. Go to **Manage Jenkins** → **Manage Credentials**
3. Click on **Global credentials (unrestricted)**
4. Click **Add Credentials**

**Step 2: Add SSH Credential**
1. **Kind**: Select "SSH Username with private key"
2. **Scope**: Global
3. **ID**: `docker-host-ssh-key` (or any descriptive name)
4. **Description**: `SSH key for Docker host access`
5. **Username**: `ubuntu`
6. **Private Key**: Select "Enter directly"
7. **Key**: Copy the private key content from Jenkins master:

```bash
# On Jenkins master, get the private key content
sudo cat /var/lib/jenkins/.ssh/id_rsa
```

**Step 3: Save and Verify Credential**
1. Click **OK** to save the credential
2. The credential should appear in the Global credentials list
3. Note the credential ID for later use

**Step 4: Test SSH Connection Manually**
Since Jenkins 2.516.2 doesn't have built-in SSH testing, verify the connection manually:

```bash
# Test SSH connection from Jenkins master
ssh ubuntu@docker-host-ip "echo 'SSH connection successful'"

# Test Docker access
ssh ubuntu@docker-host-ip "docker --version && docker ps"
```

### 2.3 Configure Docker Host as Build Agent

**Step 1: Access Node Management**
1. Go to **Manage Jenkins** → **Manage Nodes and Clouds**
2. Click **New Node**

**Step 2: Configure New Node**
1. **Node name**: `docker-host-agent`
2. **Type**: Select "Permanent Agent"
3. Click **OK**

**Step 3: Configure Node Details**
1. **Description**: `Docker host for containerized builds`
2. **Number of executors**: `2`
3. **Remote root directory**: `/home/ubuntu/jenkins-agent`
4. **Labels**: `docker docker-host linux`
5. **Usage**: "Only build jobs with label expressions matching this node"
6. **Launch method**: "Launch agents via SSH"
7. **Host**: `docker-host-ip` (e.g., `instructor-docker-host-1`)
8. **Credentials**: Select the SSH credential created in step 2.2
9. **Host Key Verification Strategy**: "Non verifying Verification Strategy"
10. **Availability**: "Keep this agent online as much as possible"

**Step 4: Advanced Configuration**
1. **JavaPath**: `/usr/bin/java` (or leave empty for auto-detection)
2. **JVM Options**: `-Xmx2g -Xms512m`
3. **Prefix Start Agent Command**: Leave empty
4. **Suffix Start Agent Command**: Leave empty

**Step 5: Save and Test Connection**
1. Click **Save**
2. The node should appear in the list and start connecting automatically
3. Check the node status - it should show "Connected" with a green icon
4. If connection fails, check the node's log for error details

**Step 6: Verify Node Capabilities**
1. Click on the node name to view details
2. Go to **System Information** tab
3. Check that Docker is available in the environment variables
4. Look for `DOCKER_HOST`, `PATH` containing `/usr/bin/docker`, etc.

## Success Criteria

- [ ] All required plugins are installed
- [ ] SSH credentials are configured correctly
- [ ] Docker host is configured as build agent
- [ ] Node shows "Connected" status
- [ ] Docker is available in node environment

## Troubleshooting

**If Node Fails to Connect:**
- Check SSH credentials and network connectivity
- Verify Docker is installed on the target host
- Check if ubuntu user is in docker group
- Review node log for specific error messages

**If Connection Timeout:**
- Check security groups allow SSH (port 22)
- Verify network connectivity between Jenkins master and Docker host

## Next Steps

Once Jenkins configuration is complete, proceed to [Step 3: Docker Pipeline](lab-1.1-step3-docker-pipeline.md).
