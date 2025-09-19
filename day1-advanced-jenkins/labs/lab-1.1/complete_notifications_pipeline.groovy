pipeline {
    agent {
        label 'docker'
    }
    
    environment {
        IMAGE_NAME = 'jenkins-notifications-app'
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
    <artifactId>notifications-app</artifactId>
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
                    mkdir -p app/src/main/java/com/jenkins/notifications
                    cat > app/src/main/java/com/jenkins/notifications/NotificationsAppApplication.java << 'EOF'
package com.jenkins.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationsAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationsAppApplication.class, args);
    }
}
EOF
                    
                    # Create REST controller
                    cat > app/src/main/java/com/jenkins/notifications/HelloController.java << 'EOF'
package com.jenkins.notifications;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Notifications Pipeline!";
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
                            docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                            docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${IMAGE_NAME}:latest
                        """
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                echo 'Testing the application...'
                script {
                    sh """
                        docker run --rm -d --name test-container -p 8080:8080 ${IMAGE_NAME}:${IMAGE_TAG}
                        sleep 10
                        curl -f http://localhost:8080/health || exit 1
                        docker stop test-container
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
            echo 'Pipeline completed. Sending notifications...'
            // Always run cleanup
            script {
                sh """
                    docker rmi ${IMAGE_NAME}:${IMAGE_TAG} || true
                    docker rmi ${IMAGE_NAME}:latest || true
                """
            }
        }
        
        success {
            echo 'Pipeline succeeded! Sending success notification...'
            // Send success notification
            emailext (
                subject: "Pipeline Success: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    Pipeline completed successfully!
                    
                    Job: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    Duration: ${currentBuild.durationString}
                    Node: ${env.NODE_NAME}
                    
                    View build: ${env.BUILD_URL}
                """,
                to: "team@example.com"
            )
        }
        
        failure {
            echo 'Pipeline failed! Sending failure notification...'
            // Send failure notification
            emailext (
                subject: "Pipeline Failed: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    Pipeline failed!
                    
                    Job: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    Duration: ${currentBuild.durationString}
                    Node: ${env.NODE_NAME}
                    
                    Check console output: ${env.BUILD_URL}console
                    View build: ${env.BUILD_URL}
                """,
                to: "devops-team@example.com"
            )
        }
        
        unstable {
            echo 'Pipeline unstable! Sending warning notification...'
            // Send warning for unstable builds
            emailext (
                subject: "Pipeline Unstable: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    Pipeline completed but with warnings!
                    
                    Job: ${env.JOB_NAME}
                    Build: #${env.BUILD_NUMBER}
                    Duration: ${currentBuild.durationString}
                    
                    View build: ${env.BUILD_URL}
                """,
                to: "team@example.com"
            )
        }
    }
}
