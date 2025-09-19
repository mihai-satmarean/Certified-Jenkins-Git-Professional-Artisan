pipeline {
    agent {
        label 'docker'
    }
    
    parameters {
        string(name: 'DOCKER_IMAGE', defaultValue: 'nginx:alpine', description: 'Base Docker image')
        string(name: 'PORT', defaultValue: '8080', description: 'Port to expose')
        booleanParam(name: 'SKIP_CLEANUP', defaultValue: false, description: 'Skip cleanup stage')
        choice(name: 'ENVIRONMENT', choices: ['dev', 'staging', 'production'], description: 'Deployment environment')
    }
    
    environment {
        IMAGE_NAME = 'jenkins-parameterized-app'
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
    <artifactId>parameterized-app</artifactId>
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
                    mkdir -p app/src/main/java/com/jenkins/parameterized
                    cat > app/src/main/java/com/jenkins/parameterized/ParameterizedAppApplication.java << 'EOF'
package com.jenkins.parameterized;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ParameterizedAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParameterizedAppApplication.class, args);
    }
}
EOF
                    
                    # Create REST controller
                    cat > app/src/main/java/com/jenkins/parameterized/HelloController.java << 'EOF'
package com.jenkins.parameterized;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Parameterized Pipeline!";
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
        
        stage('Build with Parameters') {
            steps {
                echo "Building for environment: ${params.ENVIRONMENT}"
                dir('app') {
                    script {
                        sh """
                            # Build Java application with Maven
                            mvn clean package -DskipTests
                            
                            # Build Docker image with parameters
                            docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                        """
                    }
                }
            }
        }
        
        stage('Test with Parameters') {
            steps {
                echo "Testing on port: ${params.PORT}"
                script {
                    sh """
                        # Test container with parameterized port
                        docker run --rm -d --name test-container -p ${params.PORT}:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                        sleep 10
                        curl -f http://localhost:${params.PORT}/health || exit 1
                        docker stop test-container
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
            script {
                if (!params.SKIP_CLEANUP) {
                    echo 'Cleaning up Docker images...'
                    sh """
                        # Remove our test images to save disk space
                        docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true
                        docker rmi ${IMAGE_NAME}:latest || true
                    """
                } else {
                    echo 'Skipping cleanup as requested by parameter'
                }
            }
        }
        success {
            echo "Parameterized pipeline completed successfully for environment: ${params.ENVIRONMENT}"
        }
        failure {
            echo 'Parameterized pipeline failed! Check the logs above for errors.'
        }
    }
}
