```groovy
pipeline {
    agent {
        label 'docker'  // Run on nodes with 'docker' label
    }
    
    environment {
        IMAGE_NAME = 'jenkins-multi-stage-app'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Creating Java Spring Boot application files...'
                sh '''
                    # Clean workspace first to avoid conflicts
                    rm -rf app
                    mkdir -p app
                    
                    # Create Dockerfile - simple version
                    cat > app/Dockerfile << 'EOF'
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
EOF
                    
                    # Create pom.xml
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
    <artifactId>multi-stage-app</artifactId>
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
                    mkdir -p app/src/main/java/com/jenkins/multistage
                    cat > app/src/main/java/com/jenkins/multistage/MultiStageAppApplication.java << 'EOF'
package com.jenkins.multistage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MultiStageAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultiStageAppApplication.class, args);
    }
}
EOF
                    
                    # Create REST controller
                    cat > app/src/main/java/com/jenkins/multistage/HelloController.java << 'EOF'
package com.jenkins.multistage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Multi-Stage Docker Build!";
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
        
        stage('Multi-Stage Build') {
            steps {
                echo 'Creating multi-stage Dockerfile for Java Spring Boot...'
                script {
                    sh '''
                        # Create a multi-stage Dockerfile for Java Spring Boot
                        cat > app/Dockerfile.multi << 'EOF'
# Stage 1: Build stage - contains Maven and build tools
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
# Copy pom.xml first for better layer caching
COPY pom.xml .
# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B
# Copy source code
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage - contains only runtime dependencies
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar
# Create non-root user for security
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring
# Expose port
EXPOSE 8080
# Start the application
CMD ["java", "-jar", "app.jar"]
EOF
                    '''
                }
            }
        }
        
        stage('Build Multi-Stage Image') {
            steps {
                echo 'Building multi-stage Docker image...'
                dir('app') {
                    script {
                        sh """
                            # Build Java application with Maven
                            mvn clean package -DskipTests
                            
                            # Build using the multi-stage Dockerfile
                            docker build -f Dockerfile.multi -t ${IMAGE_NAME}:multi-${IMAGE_TAG} .
                            
                            # Also tag as 'latest' for convenience
                            docker tag ${IMAGE_NAME}:multi-${IMAGE_TAG} ${IMAGE_NAME}:multi-latest
                        """
                    }
                }
            }
        }
        
        stage('Test Multi-Stage Container') {
            steps {
                echo 'Testing multi-stage Docker container...'
                script {
                    sh """
                        # Test the multi-stage build
                        docker run --rm -d --name test-multi -p 8081:8080 ${IMAGE_NAME}:multi-${IMAGE_TAG}
                        sleep 10
                        curl -f http://localhost:8081/health || exit 1
                        docker stop test-multi
                    """
                }
            }
        }
        
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
    
    post {
        always {
            echo 'Cleaning up Docker images...'
            script {
                sh """
                    # Remove our test images to save disk space
                    docker rmi ${IMAGE_NAME}:multi-${IMAGE_TAG} || true
                    docker rmi ${IMAGE_NAME}:multi-latest || true
                """
            }
        }
        success {
            echo 'Multi-stage pipeline completed successfully!'
        }
        failure {
            echo 'Multi-stage pipeline failed! Check the logs above for errors.'
        }
    }
}
```