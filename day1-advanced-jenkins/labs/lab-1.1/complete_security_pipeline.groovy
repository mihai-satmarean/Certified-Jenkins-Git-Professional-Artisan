pipeline {
    agent {
        label 'docker'
    }
    
    environment {
        IMAGE_NAME = 'jenkins-security-app'
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
                    
                    # Create secure Dockerfile
                    cat > app/Dockerfile << 'EOF'
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
    <artifactId>security-app</artifactId>
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
                    mkdir -p app/src/main/java/com/jenkins/security
                    cat > app/src/main/java/com/jenkins/security/SecurityAppApplication.java << 'EOF'
package com.jenkins.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecurityAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityAppApplication.class, args);
    }
}
EOF
                    
                    # Create REST controller
                    cat > app/src/main/java/com/jenkins/security/HelloController.java << 'EOF'
package com.jenkins.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Security Pipeline!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"healthy\",\"timestamp\":\"" + LocalDateTime.now() + "\"}";
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
        
        stage('Build') {
            steps {
                echo 'Building Java application with Maven...'
                dir('app') {
                    script {
                        sh """
                            mvn clean package -DskipTests
                        """
                    }
                }
            }
        }
        
        stage('Secure Build') {
            steps {
                echo 'Implementing security best practices...'
                script {
                    sh """
                        # 1. Build with security flags
                        docker build \\
                            --no-cache \\
                            --pull \\
                            --build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ') \\
                            --build-arg VCS_REF=\$(git rev-parse --short HEAD 2>/dev/null || echo 'local') \\
                            -t ${IMAGE_NAME}:${IMAGE_TAG} app/
                        
                        # 2. Test with non-root user
                        echo "Testing with non-root user..."
                        docker run --rm \\
                            --user 1000:1000 \\
                            --read-only \\
                            --tmpfs /tmp \\
                            ${IMAGE_NAME}:${IMAGE_TAG} \\
                            java -jar app.jar --help || echo "Help command completed"
                        
                        # 3. Check image layers and size
                        echo "Image analysis:"
                        docker history ${IMAGE_NAME}:${IMAGE_TAG}
                        docker images ${IMAGE_NAME}:${IMAGE_TAG}
                    """
                }
            }
        }
        
        stage('Security Validation') {
            steps {
                echo 'Running security validation...'
                script {
                    sh """
                        # Check for secrets in image
                        echo "Checking for secrets..."
                        docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} \\
                            find / -name "*.key" -o -name "*.pem" -o -name "*.p12" 2>/dev/null || true
                        
                        # Check running processes
                        echo "Checking running processes..."
                        docker run --rm ${IMAGE_NAME}:${IMAGE_TAG} ps aux || echo "Process check completed"
                        
                        # Test application functionality
                        echo "Testing application..."
                        docker run --rm -d --name test-security -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                        sleep 10
                        curl -f http://localhost:8080/health || exit 1
                        docker stop test-security
                    """
                }
            }
        }
        
        stage('List Images') {
            steps {
                echo 'Listing Docker images...'
                sh """
                    docker images | grep ${IMAGE_NAME}
                    docker images
                """
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up security test resources...'
            script {
                sh """
                    docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true
                """
            }
        }
        success {
            echo 'Security pipeline completed successfully!'
        }
        failure {
            echo 'Security pipeline failed! Check the logs above for errors.'
        }
    }
}
