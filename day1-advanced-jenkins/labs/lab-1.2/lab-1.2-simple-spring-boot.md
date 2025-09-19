# Lab 1.2: Simple Spring Boot Application - Foundation for Weather Service

## Lab Overview
**Prerequisites**: Jenkins & Git Associate certification, Docker knowledge, Maven basics

## Learning Objectives
By the end of this lab, you will be able to:
- Create a simple Spring Boot application with REST endpoints
- Build and run Spring Boot applications with Docker
- Create basic Jenkins pipeline for Maven-based Spring Boot projects
- Understand the foundation concepts for Weather Service Wrapper
- Implement health checks and basic monitoring

## Lab Environment Setup

### Prerequisites
- Docker integration with Jenkins working
- Jenkins master with Maven and Docker plugins
- Docker installed and running
- Maven 3.9+ installed
- Java 17+ installed
- Git repository access

### Environment Components
```
┌─────────────────┐    Build    ┌─────────────────┐
│   Jenkins       │◄───────────►│   Docker        │
│   Master        │             │   Host          │
│                 │             │                 │
│ - Maven Plugin  │             │ - Docker Daemon │
│ - Docker Plugin │             │ - Java 17       │
│ - Git Plugin    │             │ - Maven 3.9     │
└─────────────────┘             └─────────────────┘
```
## Lab Steps

### Step 1: Create Simple Spring Boot Application

#### 1.1 Project Structure
Create the following project structure:
```
simple-spring-app/
├── pom.xml
├── Dockerfile
├── Jenkinsfile
└── src/
    ├── main/
    │   ├── java/com/example/demo/
    │   │   ├── DemoApplication.java
    │   │   └── controller/
    │   │       └── DemoController.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/example/demo/
            └── controller/
                └── DemoControllerTest.java
```

#### 1.2 Create Maven Configuration (pom.xml)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>simple-spring-app</artifactId>
    <version>1.0.0</version>
    <name>simple-spring-app</name>
    <description>Simple Spring Boot Application - Foundation for Weather Service</description>
    <properties>
        <java.version>11</java.version>
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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
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
```

#### 1.3 Create Main Application Class
**src/main/java/com/example/demo/DemoApplication.java**:
```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
```

#### 1.4 Create REST Controller
**src/main/java/com/example/demo/controller/DemoController.java**:
```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class DemoController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from Simple Spring Boot App!");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "running");
        return response;
    }

    @GetMapping("/hello")
    public Map<String, String> hello(@RequestParam(defaultValue = "World") String name) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, " + name + "!");
        response.put("service", "Simple Spring Boot App");
        return response;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Simple Spring Boot App");
        response.put("version", "1.0.0");
        response.put("java.version", System.getProperty("java.version"));
        response.put("spring.boot.version", "3.3.4");
        response.put("description", "Foundation application for Weather Service Wrapper");
        return response;
    }
}
```

#### 1.5 Create Application Properties
**src/main/resources/application.properties**:
```properties
# Server Configuration
server.port=8080
spring.application.name=simple-spring-app

# Actuator Configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always

# Application Info
info.app.name=Simple Spring Boot App
info.app.description=Foundation application for Weather Service Wrapper
info.app.version=1.0.0
```

#### 1.6 Create Unit Test
**src/test/java/com/example/demo/controller/DemoControllerTest.java**:
```java
package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DemoController.class)
class DemoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHomeEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello from Simple Spring Boot App!"))
                .andExpect(jsonPath("$.status").value("running"));
    }

    @Test
    void testHelloEndpoint() throws Exception {
        mockMvc.perform(get("/hello").param("name", "Jenkins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Jenkins!"))
                .andExpect(jsonPath("$.service").value("Simple Spring Boot App"));
    }

    @Test
    void testInfoEndpoint() throws Exception {
        mockMvc.perform(get("/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Simple Spring Boot App"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
```

### Step 2: Create Docker Configuration

#### 2.1 Create Dockerfile
```dockerfile
# Multi-stage build for Simple Spring Boot App
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "app.jar"]
```

### Step 3: Create Jenkins Pipeline

#### 3.1 Create Jenkinsfile
```groovy
pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'ghcr.io/mihai-satmarean'
        IMAGE_NAME = 'simple-spring-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${IMAGE_NAME}:${IMAGE_TAG}")
                }
            }
        }
        
        stage('Test Docker Image') {
            steps {
                script {
                    sh """
                        docker run -d --name test-app-${BUILD_NUMBER} -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                        sleep 30
                        curl -f http://localhost:8080/actuator/health
                        curl -f http://localhost:8080/
                        curl -f http://localhost:8080/hello?name=Jenkins
                    """
                }
            }
        }
        
        stage('Push to Registry') {
            steps {
                script {
                    docker.image("${IMAGE_NAME}:${IMAGE_TAG}").push()
                }
            }
        }
    }
    
    post {
        always {
            script {
                sh "docker stop test-app-${BUILD_NUMBER} || true"
                sh "docker rm test-app-${BUILD_NUMBER} || true"
            }
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
```

### Step 4: Test the Application

#### 4.1 Local Testing
```bash
# Build the application
mvn clean package

# Run the application
java -jar target/simple-spring-app-1.0.0.jar

# Test endpoints
curl http://localhost:8080/
curl http://localhost:8080/hello?name=World
curl http://localhost:8080/info
curl http://localhost:8080/actuator/health
```

#### 4.2 Docker Testing
```bash
# Build Docker image
docker build -t simple-spring-app:latest .

# Run container
docker run -d --name simple-app -p 8080:8080 simple-spring-app:latest

# Test endpoints
curl http://localhost:8080/
curl http://localhost:8080/actuator/health

# Check logs
docker logs simple-app

# Stop and remove
docker stop simple-app
docker rm simple-app
```

## Lab Validation

### Success Criteria
- [ ] Spring Boot application builds successfully
- [ ] All unit tests pass
- [ ] Docker image builds without errors
- [ ] Application runs in Docker container
- [ ] All REST endpoints respond correctly
- [ ] Health check endpoint works
- [ ] Jenkins pipeline completes successfully
- [ ] Application is ready for Weather Service Wrapper evolution

### Testing Commands
```bash
# Test Maven build
mvn clean test

# Test Docker build
docker build -t simple-spring-app:test .

# Test application endpoints
curl http://localhost:8080/
curl http://localhost:8080/hello?name=Test
curl http://localhost:8080/info
curl http://localhost:8080/actuator/health
```

## Connection to Weather Service Wrapper

### What We've Built
This simple Spring Boot application provides the foundation for the Weather Service Wrapper:

**Current Features**:
- Basic Spring Boot REST API
- Health monitoring with Actuator
- Docker multi-stage build
- Jenkins CI/CD pipeline
- Unit testing

**Weather Service Wrapper Evolution**:
- Add database integration (H2 + JPA)
- Add external API integration (OpenWeatherMap)
- Add mock data service
- Add advanced monitoring and metrics
- Add security features
- Add advanced Jenkins pipeline features

### Next Steps
After completing this lab:
1. Understand Spring Boot application structure
2. Master Docker multi-stage builds
3. Practice Jenkins pipeline development
4. Complete Lab 1.3: Jenkins Ansible Integration for automated deployment
5. Prepare for advanced features in Weather Service Wrapper

## Troubleshooting

### Common Issues

#### Maven Build Issues
```bash
# Clean and rebuild
mvn clean install

# Check Java version
java -version
mvn -version
```

#### Docker Build Issues
```bash
# Check Docker is running
docker --version
docker ps

# Clean Docker cache
docker system prune
```

#### Application Issues
```bash
# Check application logs
docker logs <container-name>

# Check port availability
netstat -tulpn | grep 8080
```

## Lab Cleanup

### Cleanup Commands
```bash
# Remove Docker containers and images
docker stop $(docker ps -aq) || true
docker rm $(docker ps -aq) || true
docker rmi simple-spring-app:latest || true

# Clean Maven cache
mvn clean
```

## Resources

### Documentation
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Jenkins Pipeline Documentation](https://www.jenkins.io/doc/book/pipeline/)
- [GitHub Repository](https://github.com/mihai-satmarean/Certified-Jenkins-Git-Professional-Artisan)

### Next Lab
- **Lab 1.3**: Advanced Docker Integration
- **Lab 2.1**: Groovy Automation
- **Lab 3.2**: Weather Service Wrapper (Final Lab)

---

**Lab Completion**: Mark this lab as complete when all success criteria are met and the application runs successfully in Docker with Jenkins pipeline.
