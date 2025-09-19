# Lab 1.1: Step 3 - Docker Pipeline

## Overview
This step covers creating and running a Docker integration pipeline in Jenkins.

## What We're Building

In this step, we'll create a complete CI/CD pipeline that:

1. **Creates a simple Java Spring Boot application** - A web server with health check endpoint
2. **Builds a Docker image** - Packages our application into a container
3. **Tests the container** - Verifies our application works inside Docker
4. **Lists images** - Shows what Docker images we created
5. **Cleans up** - Removes test images to save disk space

## Why This Pipeline?

This pipeline demonstrates:
- **Docker integration** - How Jenkins can build and test Docker containers
- **Application lifecycle** - From source code to running container
- **Testing automation** - Automated testing of containerized applications
- **Resource management** - Proper cleanup of Docker resources
- **CI/CD best practices** - Structured pipeline with clear stages

## Pipeline Flow

```
Source Code → Docker Build → Container Test → Image Listing → Cleanup
     ↓              ↓              ↓              ↓           ↓
  Create App    Build Image    Test Container   Show Images  Remove Images
```

## Step 3: Create Docker Integration Pipeline

### 3.1 Create New Pipeline Project
1. Go to Jenkins main dashboard
2. Click **New Item**
3. Enter name: `docker-integration-pipeline`
4. Select **Pipeline**
5. Click **OK**

### 3.2 Configure Pipeline Script
1. In the pipeline configuration, scroll to **Pipeline** section
2. **Definition**: Pipeline script
3. **Script**: Enter the following Jenkinsfile content:

```groovy
pipeline {
    // Define where this pipeline should run
    agent {
        label 'docker'  // Run on nodes with 'docker' label (our Docker host)
    }
    
    // Set environment variables available throughout the pipeline
    environment {
        DOCKER_REGISTRY = 'your-registry.com'  // Docker registry URL (for future use)
        IMAGE_NAME = 'jenkins-docker-app'      // Name of our Docker image
        IMAGE_TAG = "${BUILD_NUMBER}"          // Tag image with Jenkins build number
    }
    
    stages {
        // Stage 1: Create our Java Spring Boot application files
        stage('Checkout') {
            steps {
                echo 'Creating Java Spring Boot application files...'
                // For demo purposes, we'll create a simple Java Spring Boot app
                // In real projects, this would be 'git checkout' from a repository
                sh '''
                    mkdir -p app
                    
                    # Create Dockerfile - defines how to build our Docker image
                    cat > app/Dockerfile << 'EOF'
# Use OpenJDK 17 on Debian slim
FROM openjdk:17-jdk-slim
# Set working directory inside container
WORKDIR /app
# Copy the built JAR file
COPY target/*.jar app.jar
# Expose port 8080 for the application
EXPOSE 8080
# Start the application
CMD ["java", "-jar", "app.jar"]
EOF
                    
                    # Create pom.xml - Maven configuration for our Java application
                    cat > app/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.4</version>
        <relativePath/>
    </parent>
    
    <groupId>com.jenkins</groupId>
    <artifactId>docker-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF
                    
                    # Create main application class
                    mkdir -p app/src/main/java/com/jenkins/dockerapp
                    cat > app/src/main/java/com/jenkins/dockerapp/DockerAppApplication.java << 'EOF'
package com.jenkins.dockerapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DockerAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(DockerAppApplication.class, args);
    }
}
EOF
                    
                    # Create REST controller
                    cat > app/src/main/java/com/jenkins/dockerapp/HelloController.java << 'EOF'
package com.jenkins.dockerapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Jenkins Docker Pipeline!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "{\\"status\\":\\"healthy\\",\\"timestamp\\":\\"" + LocalDateTime.now() + "\\"}";
    }
}
EOF
                    
                    # Create application properties
                    mkdir -p app/src/main/resources
                    cat > app/src/main/resources/application.properties << 'EOF'
server.port=8080
management.endpoints.web.exposure.include=health,info
EOF
                '''
            }
        }
        
        // Stage 2: Build Java application and Docker image
        stage('Build Docker Image') {
            steps {
                echo 'Building Java application and Docker image...'
                dir('app') {  // Change to app directory where source code is located
                    script {
                        sh """
                            # Build Java application with Maven
                            mvn clean package -DskipTests
                            
                            # Build Docker image with build number as tag
                            docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                            
                            # Also tag as 'latest' for convenience
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                        """
                    }
                }
            }
        }
        
        // Stage 3: Test our Docker container
        stage('Test Container') {
            steps {
                echo 'Testing Docker container...'
                script {
                    sh """
                        # Run container in detached mode (background)
                        # --rm: automatically remove container when it stops
                        # -d: run in detached mode
                        # --name: give container a name for easy reference
                        # -p: map port 8080 from container to port 8080 on host
                        docker run --rm -d --name test-container -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                        
                        # Wait for Spring Boot application to start up
                        sleep 10
                        
                        # Test if our application is responding
                        # curl -f: fail silently on HTTP errors
                        # || exit 1: exit with error if curl fails
                        curl -f http://localhost:8080/health || exit 1
                        
                        # Stop the test container
                        docker stop test-container
                    """
                }
            }
        }
        
        // Stage 4: List and verify our Docker images
        stage('List Images') {
            steps {
                echo 'Listing Docker images...'
                sh """
                    # Show only our application images
                    docker images | grep ${IMAGE_NAME}
                    
                    # Show all Docker images for verification
                    docker images
                """
            }
        }
    }
    
    // Post-build actions - run regardless of pipeline success/failure
    post {
        always {
            echo 'Cleaning up Docker images...'
            script {
                sh """
                    # Remove our test images to save disk space
                    # || true: don't fail if image doesn't exist
                    docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true
                    docker rmi ${IMAGE_NAME}:latest || true
                """
            }
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed! Check the logs above for errors.'
        }
    }
}
```

### 3.3 Save and Run Pipeline
1. Click **Save**
2. Click **Build Now** to run the pipeline
3. Click on the build number to view results
4. Click **Console Output** to see detailed logs

### 3.4 Monitor Pipeline Execution
Watch the pipeline progress through each stage:

**Expected Console Output:**
```
Started by user admin
Running on docker-host-agent in workspace /home/ubuntu/jenkins-agent/workspace/docker-integration-pipeline

[Pipeline] { (Checkout)
Creating application files...
+ mkdir -p app
+ cat > app/Dockerfile << 'EOF'
+ cat > app/pom.xml << 'EOF'
+ mkdir -p app/src/main/java/com/jenkins/dockerapp
+ cat > app/src/main/java/com/jenkins/dockerapp/DockerAppApplication.java << 'EOF'
+ cat > app/src/main/java/com/jenkins/dockerapp/HelloController.java << 'EOF'

[Pipeline] { (Build Docker Image)
Building Java application and Docker image...
+ mvn clean package -DskipTests
[INFO] BUILD SUCCESS
+ docker build -t jenkins-docker-app:1 .
Sending build context to Docker daemon  15.2MB
Step 1/4 : FROM openjdk:17-jdk-slim
Step 2/4 : WORKDIR /app
Step 3/4 : COPY target/*.jar app.jar
Step 4/4 : EXPOSE 8080
Successfully built 1234567890mn
Successfully tagged jenkins-docker-app:1
+ docker tag jenkins-docker-app:1 jenkins-docker-app:latest

[Pipeline] { (Test Container)
Testing Docker container...
+ docker run --rm -d --name test-container -p 8080:8080 jenkins-docker-app:1
+ sleep 10
+ curl -f http://localhost:8080/health || exit 1
{"status":"healthy","timestamp":"2025-01-17T10:30:00.000Z"}
+ docker stop test-container

[Pipeline] { (List Images)
Listing Docker images...
jenkins-docker-app   1       1234567890mn   2 minutes ago   180MB
jenkins-docker-app   latest  1234567890mn   2 minutes ago   180MB

[Pipeline] { (Declarative: Post Actions)
Cleaning up...
+ docker rmi jenkins-docker-app:1 || true
+ docker rmi jenkins-docker-app:latest || true
Pipeline completed successfully!

Finished: SUCCESS
```

### 3.5 Verify Pipeline Results
**Success Indicators:**
- ✅ **All stages completed**: Checkout, Build, Test, List Images
- ✅ **Docker image built**: `jenkins-docker-app:1` and `jenkins-docker-app:latest`
- ✅ **Container test passed**: Health check returned `{"status":"healthy"}`
- ✅ **Images listed**: Both tagged versions visible in `docker images`
- ✅ **Cleanup completed**: Images removed after successful build
- ✅ **Build status**: `Finished: SUCCESS`

## Success Criteria

- [ ] Pipeline project created successfully
- [ ] All pipeline stages complete without errors
- [ ] Docker image builds successfully
- [ ] Container test passes
- [ ] Images are properly listed and cleaned up
- [ ] Pipeline shows "Finished: SUCCESS"

## Next Steps

Once the basic pipeline is working, proceed to [Step 4: Advanced Scenarios](lab-1.1-step4-advanced-scenarios.md) for more complex Docker integration scenarios.
