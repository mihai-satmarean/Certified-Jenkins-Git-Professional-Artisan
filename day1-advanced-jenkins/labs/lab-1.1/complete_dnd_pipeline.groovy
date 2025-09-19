pipeline {
    agent {
        label 'docker'
    }
    
    environment {
        IMAGE_NAME = 'jenkins-dind-app'
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
                    
                    # Create simple Dockerfile
                    cat > app/Dockerfile << 'EOF'
FROM eclipse-temurin:17-jre
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
    <artifactId>dind-app</artifactId>
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
                    mkdir -p app/src/main/java/com/jenkins/dind
                    cat > app/src/main/java/com/jenkins/dind/DindAppApplication.java << 'EOF'
package com.jenkins.dind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DindAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(DindAppApplication.class, args);
    }
}
EOF
                    
                    # Create REST controller
                    cat > app/src/main/java/com/jenkins/dind/HelloController.java << 'EOF'
package com.jenkins.dind;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Docker-in-Docker Pipeline!";
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
        
        stage('Docker-in-Docker Build') {
            steps {
                echo 'Testing Docker-in-Docker build process...'
                dir('app') {
                    script {
                        sh """
                            # Build Java application with Maven
                            mvn clean package -DskipTests
                            
                            # Method 1: Using Docker socket mounting (requires privileged access)
                            docker run --rm \\
                                -v /var/run/docker.sock:/var/run/docker.sock \\
                                -v \$(pwd):/workspace \\
                                -w /workspace \\
                                docker:latest \\
                                docker build -t ${IMAGE_NAME}:dind-${IMAGE_TAG} .
                            
                            # Test the DinD built image
                            docker run --rm -d --name test-dind -p 8082:8080 ${IMAGE_NAME}:dind-${IMAGE_TAG}
                            sleep 10
                            curl -f http://localhost:8082/health || exit 1
                            docker stop test-dind
                        """
                    }
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
                    docker rmi ${IMAGE_NAME}:dind-${IMAGE_TAG} || true
                """
            }
        }
        success {
            echo 'Docker-in-Docker pipeline completed successfully!'
        }
        failure {
            echo 'Docker-in-Docker pipeline failed! Check the logs above for errors.'
        }
    }
}