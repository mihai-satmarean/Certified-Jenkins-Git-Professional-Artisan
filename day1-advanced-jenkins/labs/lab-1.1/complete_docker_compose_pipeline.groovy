```groovy
pipeline {
    agent {
        label 'docker'  // Run on nodes with 'docker' label
    }
    
    environment {
        IMAGE_NAME = 'jenkins-compose-app'
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
    <artifactId>compose-app</artifactId>
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
                    mkdir -p app/src/main/java/com/jenkins/compose
                    cat > app/src/main/java/com/jenkins/compose/ComposeAppApplication.java << 'EOF'
package com.jenkins.compose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComposeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(ComposeAppApplication.class, args);
    }
}
EOF
                    
                    # Create REST controller
                    cat > app/src/main/java/com/jenkins/compose/HelloController.java << 'EOF'
package com.jenkins.compose;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

@RestController
public class HelloController {
    
    @GetMapping("/")
    public String hello() {
        return "Hello from Docker Compose Pipeline!";
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
        
        stage('Docker Compose Test') {
            steps {
                echo 'Testing multi-container setup with Docker Compose...'
                script {
                    sh '''
                        # Create docker-compose.yml for Java Spring Boot + Nginx
                        cat > app/docker-compose.yml << 'EOF'
services:
  # Our Java Spring Boot application
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    networks:
      - app-network

  # Nginx reverse proxy
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - app
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
EOF
                        
                        # Create Nginx configuration
                        cat > app/nginx.conf << 'EOF'
events {
    worker_connections 1024;
}

http {
    upstream app {
        server app:8080;
    }
    
    server {
        listen 80;
        
        location / {
            proxy_pass http://app;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
        }
    }
}
EOF
                    '''
                }
            }
        }
        
        stage('Build and Test Compose') {
            steps {
                echo 'Building and testing Docker Compose setup...'
                dir('app') {
                    script {
                        sh """
                            # Build Java application with Maven
                            mvn clean package -DskipTests
                            
                            # Start services with Docker Compose
                            docker-compose up -d
                            
                            # Wait for services to be healthy
                            sleep 15
                            
                            # Test the application through Nginx
                            curl -f http://localhost/health
                            
                            # Test direct access to app
                            curl -f http://localhost:8080/health
                            
                            # Stop and cleanup
                            docker-compose down
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo 'Cleaning up Docker Compose resources...'
            script {
                sh """
                    # Stop any remaining containers
                    docker-compose -f app/docker-compose.yml down || true
                    
                    # Remove any orphaned containers
                    docker container prune -f || true
                """
            }
        }
        success {
            echo 'Docker Compose pipeline completed successfully!'
        }
        failure {
            echo 'Docker Compose pipeline failed! Check the logs above for errors.'
        }
    }
}
```