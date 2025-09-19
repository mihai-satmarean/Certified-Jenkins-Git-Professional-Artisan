# Lab 1.1: Step 4 - Advanced Docker Scenarios

## Overview
This step covers advanced Docker integration scenarios including multi-stage builds, Docker Compose, and Docker-in-Docker.

## Prerequisites
Before starting this step, ensure you have:
- Completed Step 3: Docker Pipeline successfully
- Basic understanding of Docker concepts
- Jenkins pipeline running on Docker host
- Maven and Java 17 installed on Docker host

## How to Create Advanced Pipeline in Jenkins UI

### Step-by-Step Instructions for Jenkins Interface

**Step 1: Access Jenkins Dashboard**
1. Open your web browser
2. Go to: `http://instructor-jenkins-master-1:8080`
3. You should see the Jenkins dashboard with a list of existing jobs

**Step 2: Create New Pipeline Project**
1. On the Jenkins dashboard, look for the **"New Item"** link in the left sidebar
2. Click **"New Item"**
3. You'll see a page titled "Enter an item name"

**Step 3: Configure New Pipeline**
1. In the **"Enter an item name"** field, type: `advanced-docker-pipeline`
2. Below the name field, you'll see different project types:
   - ✅ **Select "Pipeline"** (click the radio button next to it)
3. Click **"OK"** button at the bottom

**Step 4: Configure Pipeline Settings**
1. You'll now see the pipeline configuration page
2. Scroll down to find the **"Pipeline"** section
3. In the **"Definition"** dropdown, make sure **"Pipeline script"** is selected
4. In the large text area labeled **"Script"**, you'll paste the pipeline code

**Step 5: Add Pipeline Code**
1. Copy the pipeline code from the sections below
2. Paste it into the **"Script"** text area
3. Click **"Save"** button at the bottom of the page

**Step 6: Run the Pipeline**
1. You'll be redirected to the pipeline job page
2. Look for the **"Build Now"** link in the left sidebar
3. Click **"Build Now"**
4. You'll see a new build appear in the **"Build History"** section

**Step 7: Monitor Pipeline Execution**
1. Click on the build number (e.g., #1, #2, etc.) in the Build History
2. Click **"Console Output"** in the left sidebar
3. Watch the pipeline execute step by step

**Visual Guide - Jenkins UI Elements:**
```
Jenkins Dashboard
├── Left Sidebar
│   ├── New Item ← Click here to create new pipeline
│   ├── Build Now ← Click here to run pipeline
│   └── Console Output ← Click here to see logs
├── Main Area
│   ├── Build History ← Shows all builds
│   └── Pipeline Configuration ← Where you paste code
└── Top Navigation
    ├── Manage Jenkins
    └── User Account
```

**Troubleshooting UI Issues:**
- **Can't find "New Item"?** - Make sure you're logged in as admin
- **No "Pipeline" option?** - Install Pipeline plugin first
- **Script area is empty?** - Make sure "Pipeline script" is selected in Definition dropdown
- **Can't save?** - Check for syntax errors in the pipeline code

## Step 4: Advanced Docker Integration Scenarios

### 4.1 Multi-Stage Docker Builds

**What are Multi-Stage Builds?**
Multi-stage builds allow you to use multiple `FROM` statements in a single Dockerfile. Each stage can use a different base image and copy artifacts from previous stages. This helps create smaller, more efficient final images.

**Why Use Multi-Stage Builds?**
- **Smaller final images** - Only include runtime dependencies
- **Better security** - Exclude build tools from production image
- **Faster deployments** - Smaller images transfer faster
- **Cleaner separation** - Build environment separate from runtime

**Where to Implement:**
Add this as a new stage in your existing pipeline or create a separate pipeline for advanced scenarios.

**How to Add This to Jenkins:**
1. **Option A: Add to Existing Pipeline**
   - Go to your existing `docker-integration-pipeline` job
   - Click **"Configure"** in the left sidebar
   - Scroll to the **"Pipeline"** section
   - **IMPORTANT**: Add the new stage code INSIDE the existing `stages { }` block, before the closing `}`
   - Click **"Save"**

2. **Option B: Create New Pipeline (Recommended)**
   - Follow the steps above to create `advanced-docker-pipeline`
   - Use the complete pipeline code below (NOT just the stage)


**Implementation:**
Copy the complete pipeline code from: `complete_docker_pipeline.groovy`

This file contains a complete, working Jenkins pipeline that demonstrates multi-stage Docker builds with proper error handling and cleanup.

**Explanation of Multi-Stage Build:**

## What We're Achieving with Multi-Stage Build:

### 1. **Optimizarea Dimensiunii Imaginii**
- **Builder stage**: Conține Maven, JDK, build tools (~500MB+)
- **Runtime stage**: Doar JRE și aplicația (~200MB)
- **Rezultat**: Imagine finală mult mai mică

### 2. **Securitate Îmbunătățită**
- **Builder stage**: Are toate tool-urile de build (vulnerabilități potențiale)
- **Runtime stage**: Doar runtime-ul necesar (suprafață de atac redusă)
- **Rezultat**: Aplicația rulează într-un mediu mai sigur

### 3. **Performanță**
- **Deployment mai rapid**: Imagini mai mici = transfer mai rapid
- **Cache optimization**: Dependencies se cachează separat de cod
- **Resource efficiency**: Runtime stage folosește mai puține resurse

### 4. **Best Practices DevOps**
- **Separation of concerns**: Build environment vs Runtime environment
- **Production readiness**: Imaginea finală este optimizată pentru producție
- **Maintainability**: Modificările de cod nu afectează cache-ul de dependencies

## Technical Implementation:

1. **Builder Stage (maven:3.9.9-eclipse-temurin-17):**
   - Contains Maven and build tools
   - Downloads dependencies first (cached layer)
   - Compiles and packages the application
   - Creates the JAR file

2. **Runtime Stage (eclipse-temurin:17-jre):**
   - Only contains Java runtime (JRE), not JDK
   - Copies the built JAR from builder stage
   - Creates non-root user for security
   - Much smaller final image

**Expected Results:**
- **Size reduction**: Final image ~200MB vs ~500MB+ with build tools
- **Security**: No build tools in production image
- **Performance**: Faster image pulls and deployments
- **Pipeline SUCCESS**: All stages complete successfully

### 4.2 Docker Compose Integration

**What is Docker Compose?**
Docker Compose is a tool for defining and running multi-container Docker applications. It uses YAML files to configure application services, networks, and volumes.

**Why Use Docker Compose?**
- **Multi-container applications** - Run multiple services together
- **Service orchestration** - Define dependencies between services
- **Environment management** - Different configs for dev/staging/prod
- **Networking** - Automatic service discovery between containers

**Where to Implement:**
Add this as a new stage in your pipeline to test multi-container scenarios.

**How to Add This to Jenkins:**
1. **Create a new pipeline** called `docker-compose-pipeline` (follow the steps from the beginning of this file)
2. **Copy the complete pipeline code below** (NOT just the stage)
3. **Paste it into the "Script" area**
4. **Click "Save"**
5. **Run the pipeline** by clicking "Build Now"


**Implementation:**
Copy the complete pipeline code from: `complete_docker_compose_pipeline.groovy`

This file contains a complete, working Jenkins pipeline that demonstrates Docker Compose integration with proper error handling and cleanup.

**Explanation of Docker Compose Setup:**

1. **App Service:**
   - Builds from current directory (our Spring Boot app)
   - Exposes port 8080
   - Sets production profile
   - Includes health check
   - Connected to app-network

2. **Nginx Service:**
   - Uses nginx:alpine image
   - Exposes port 80
   - Loads custom nginx.conf
   - Depends on app service
   - Acts as reverse proxy

3. **Networking:**
   - Custom bridge network for service communication
   - Services can reach each other by name

**Benefits:**
- **Load balancing** - Nginx can distribute traffic
- **SSL termination** - Nginx handles HTTPS
- **Service discovery** - Containers find each other by name
- **Health monitoring** - Built-in health checks

### 4.3 Docker-in-Docker (DinD) Setup

**What is Docker-in-Docker?**
Docker-in-Docker (DinD) allows you to run Docker commands inside a Docker container. This is useful for CI/CD pipelines where you need to build Docker images from within a containerized environment.

**Why Use Docker-in-Docker?**
- **Containerized CI/CD** - Run build processes in containers
- **Isolation** - Build environment is isolated and reproducible
- **Scalability** - Easy to scale build agents
- **Consistency** - Same build environment everywhere

**Security Considerations:**
- DinD requires privileged access or Docker socket mounting
- Use with caution in production environments
- Consider alternatives like Kaniko for secure image building

**Where to Implement:**
Add this as a new stage to demonstrate containerized Docker builds.

**How to Add This to Jenkins:**
1. **Create a new pipeline** called `docker-in-docker-pipeline` (follow the steps from the beginning of this file)
2. **Copy the complete pipeline code below**
3. **Paste it into the "Script" area**
4. **Click "Save"**
5. **Run the pipeline** by clicking "Build Now"


**Implementation:**
Copy the complete pipeline code from: `complete_dnd_pipeline.groovy`

This file contains a complete, working Jenkins pipeline that demonstrates Docker-in-Docker functionality with proper error handling and cleanup.

**Explanation of Docker-in-Docker:**

1. **Socket Mounting Method:**
   - Mounts host Docker socket into container
   - Container can control host Docker daemon
   - Requires careful security considerations

2. **Buildx Method:**
   - Uses Docker Buildx for advanced building
   - More secure than socket mounting
   - Supports multi-platform builds

**Alternative: Kaniko (Recommended for Production)**

Copy the complete pipeline code from: `complete_kaniko_pipeline.groovy`

This file contains a complete, working Jenkins pipeline that demonstrates Kaniko for secure container image building without requiring Docker daemon access.

**Benefits of Kaniko:**
- **No Docker daemon required** - More secure
- **No privileged access needed** - Better security
- **Works in restricted environments** - Kubernetes, etc.
- **Same Dockerfile syntax** - No changes needed

### 4.4 Advanced Pipeline with Parameters

**What are Pipeline Parameters?**
Pipeline parameters allow users to input values when running a pipeline. This makes pipelines more flexible and reusable for different scenarios.

**Why Use Parameters?**
- **Flexibility** - Same pipeline for different environments
- **Reusability** - One pipeline, multiple use cases
- **User control** - Users can customize pipeline behavior
- **Environment management** - Different configs for dev/staging/prod

**Where to Implement:**
Add parameters to your existing pipeline or create a new parameterized pipeline.

**How to Add Parameters to Jenkins:**
1. **Create a new pipeline** called `parameterized-pipeline` (follow the steps from the beginning of this file)
2. **Go to your pipeline job**
3. **Click "Configure"** in the left sidebar
4. **Check the box "This project is parameterized"** (near the top of the page)
5. **Click "Add Parameter"** dropdown
6. **Select parameter type** (String, Boolean, Choice, etc.)
7. **Fill in parameter details** (Name, Default Value, Description)
8. **Add more parameters** if needed
9. **Scroll to "Pipeline" section** and update the script to use `${params.PARAMETER_NAME}`
10. **Click "Save"**

**Example Parameter Setup:**
- **Parameter 1**: Name: `DOCKER_IMAGE_NAME`, Type: String, Default: `jenkins-param-app`
- **Parameter 2**: Name: `DOCKER_IMAGE_TAG`, Type: String, Default: `${BUILD_NUMBER}`
- **Parameter 3**: Name: `ENVIRONMENT`, Type: Choice, Default: `dev` (choices: dev, staging, production)
- **Parameter 4**: Name: `APP_PORT`, Type: String, Default: `8080`
- **Parameter 5**: Name: `HOST_PORT`, Type: String, Default: `8080`
- **Parameter 6**: Name: `SKIP_TESTS`, Type: Boolean, Default: false

**Important for Beginners:**
When you run a parameterized pipeline without setting parameters manually, Jenkins automatically uses the DEFAULT VALUES. The pipeline will show you exactly which values are being used in the "Display Parameters" stage.

**Implementation:**
Copy the complete pipeline code from: `complete_parameterized_pipeline.groovy`

This file contains a complete, working Jenkins pipeline that demonstrates parameterized builds with configurable options for different environments and deployment scenarios.

### 4.5 Pipeline with Notifications

**What are Pipeline Notifications?**
Pipeline notifications send alerts when pipelines succeed, fail, or complete. This helps teams stay informed about build status and quickly respond to issues.

**Why Use Notifications?**
- **Immediate feedback** - Know when builds fail
- **Team collaboration** - Keep everyone informed
- **Issue resolution** - Faster response to problems
- **Audit trail** - Track build history and notifications

**Where to Implement:**
Add notification logic to the `post` section of your pipeline.

**How to Add Notifications to ANY Existing Pipeline:**

1. **Find the `post` section** in your pipeline (after `stages`, before the final `}`)
2. **Add email notifications** to existing `success` and `failure` blocks:

```groovy
post {
    always {
        // Your existing cleanup code here
    }
    success {
        // Your existing success message
        echo "Pipeline completed successfully"
        
        // ADD THIS: Email notification
        emailext (
            subject: "Pipeline Success: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
            body: """
                Pipeline completed successfully!
                
                Job: ${env.JOB_NAME}
                Build: #${env.BUILD_NUMBER}
                Duration: ${currentBuild.durationString}
                
                View build: ${env.BUILD_URL}
            """,
            to: "team@example.com"
        )
    }
    failure {
        // Your existing failure message
        echo "Pipeline failed!"
        
        // ADD THIS: Email notification
        emailext (
            subject: "Pipeline Failed: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
            body: """
                Pipeline failed!
                
                Job: ${env.JOB_NAME}
                Build: #${env.BUILD_NUMBER}
                Duration: ${currentBuild.durationString}
                
                Check console output: ${env.BUILD_URL}console
            """,
            to: "devops-team@example.com"
        )
    }
}
```

**Complete Example:**
Copy the complete pipeline code from: `complete_notifications_pipeline.groovy`

This file contains a complete, working Jenkins pipeline that demonstrates email notifications for different pipeline outcomes (success, failure, unstable).

**Benefits:**
- **Proactive monitoring** - Know about issues immediately
- **Team awareness** - Everyone stays informed
- **Faster resolution** - Quick response to failures
- **Audit trail** - Complete notification history

### 4.6 Security Best Practices

**What are Docker Security Best Practices?**
Docker security best practices are guidelines and techniques to secure containerized applications and the Docker environment. They help protect against common security vulnerabilities and threats.

**Why Implement Security Best Practices?**
- **Vulnerability protection** - Prevent security exploits
- **Compliance** - Meet security standards and regulations
- **Risk reduction** - Minimize security risks
- **Production readiness** - Ensure applications are secure for production

**Where to Implement:**
Add security checks and practices throughout your pipeline stages.

**How to Implement Security Practices:**

### **Option 1: New Security-Focused Pipeline**
Copy the complete pipeline code from: `complete_security_pipeline.groovy`

This file contains a complete, working Jenkins pipeline that demonstrates Docker security best practices.

### **Option 2: Add Security to Existing Pipeline**

**Step 1: Update your Dockerfile**
```dockerfile
# Use specific version for reproducibility
FROM eclipse-temurin:17-jre

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Set working directory
WORKDIR /app

# Copy application with correct ownership
COPY --chown=spring:spring target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \\
    CMD curl -f http://localhost:8080/health || exit 1

# Start application
CMD ["java", "-jar", "app.jar"]
```

**Step 2: Add Security Stage to Existing Pipeline**
```groovy
stage('Security Validation') {
    steps {
        echo 'Running security validation...'
        script {
            sh """
                # Build with security flags
                docker build \\
                    --no-cache \\
                    --pull \\
                    -t ${IMAGE_NAME}:${IMAGE_TAG} .
                
                # Test with non-root user
                echo "Testing with non-root user..."
                docker run --rm \\
                    --user 1000:1000 \\
                    --read-only \\
                    --tmpfs /tmp \\
                    ${IMAGE_NAME}:${IMAGE_TAG} \\
                    java -jar app.jar --help || echo "Help command completed"
                
                # Check for secrets
                echo "Checking for secrets..."
                docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} \\
                    find / -name "*.key" -o -name "*.pem" -o -name "*.p12" 2>/dev/null || true
                
                # Test application functionality
                docker run --rm -d --name test-security -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                sleep 10
                curl -f http://localhost:8080/health || exit 1
                docker stop test-security
            """
        }
    }
}
```

**Security Best Practices Explained:**

1. **Image Security:**
   - Use specific image versions with SHA256 hashes
   - Pull latest base images with `--pull`
   - Build without cache to ensure latest security updates

2. **User Security:**
   - Run containers as non-root user
   - Create dedicated user and group
   - Use `--read-only` filesystem when possible

3. **Vulnerability Scanning:**
   - Scan images for known vulnerabilities
   - Use tools like Trivy, Clair, or Snyk
   - Fail builds on critical vulnerabilities

4. **Resource Limits:**
   - Set memory and CPU limits
   - Use `--tmpfs` for temporary files
   - Implement health checks

5. **Network Security:**
   - Use custom networks
   - Limit exposed ports
   - Implement proper firewall rules

**Benefits:**
- **Reduced attack surface** - Fewer vulnerabilities
- **Compliance** - Meet security standards
- **Production ready** - Secure for deployment
- **Audit trail** - Track security measures

## Success Criteria

- [ ] Multi-stage builds work correctly
- [ ] Docker Compose integration functions properly
- [ ] Docker-in-Docker scenarios execute successfully
- [ ] Parameterized pipelines work as expected
- [ ] Notifications are sent appropriately
- [ ] Security best practices are implemented

## Quick Start Guide - What to Do Now

### Option 1: Try Multi-Stage Builds (Recommended First)
1. **Create new pipeline**: `advanced-multi-stage-pipeline`
2. **Copy the complete multi-stage pipeline code** from section 4.1 above
3. **Paste into Jenkins Script area**
4. **Save and run**

### Option 2: Try Docker Compose
1. **Create new pipeline**: `advanced-compose-pipeline`
2. **Copy the complete Docker Compose pipeline code** from section 4.2 above
3. **Paste into Jenkins Script area**
4. **Save and run**

### Option 3: Try Parameters
1. **Create new pipeline**: `advanced-parameters-pipeline`
2. **Check "This project is parameterized"**
3. **Add parameters** (DOCKER_IMAGE, PORT, SKIP_CLEANUP)
4. **Copy the complete parameters pipeline code** from section 4.4 above
5. **Paste into Jenkins Script area**
6. **Save and run**

### What Each Pipeline Does:
- **Multi-Stage**: Shows how to build smaller, more secure Docker images
- **Docker Compose**: Shows how to run multiple containers together (app + nginx)
- **Parameters**: Shows how to make pipelines flexible and reusable

### Expected Results:
- **Multi-Stage**: Smaller final image, faster builds
- **Docker Compose**: App accessible through nginx on port 80
- **Parameters**: Pipeline runs with custom values you choose
