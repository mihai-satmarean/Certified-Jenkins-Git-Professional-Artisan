# Jenkins & Git Associate - Lab Environment

## Overview
This lab environment provides a progressive, hands-on approach to learning Jenkins and Git for the Jenkins & Git Associate certification. The training is structured over 3 days with increasing complexity and real-world scenarios.

## Training Structure

### Day 1: Git Fundamentals + Jenkins Introduction
**Focus**: Foundation building with Git and basic Jenkins
- **Morning**: Git installation, configuration, and GitHub integration
- **Afternoon**: Jenkins installation, first jobs, and basic CI workflow
- **Outcome**: Working Git → Jenkins integration

### Day 2: Jenkins Integrations + Pipelines  
**Focus**: Advanced Jenkins features and pipeline development
- **Morning**: GitHub webhooks, pipeline jobs, and Jenkinsfiles
- **Afternoon**: Notifications, master-agent setup, and Maven builds
- **Outcome**: Complete CI pipeline with advanced features

### Day 3: Git Branching + Full CI/CD
**Focus**: Advanced Git workflows and complete CI/CD implementation
- **Morning**: Branching strategies, merging, and conflict resolution
- **Afternoon**: Team workflow simulation and end-to-end CI/CD
- **Outcome**: Production-ready CI/CD pipeline

## Lab Environment Architecture

```
lab-environment/
├── day1-git-fundamentals/          # Day 1: Git + Jenkins Basics
│   ├── labs/                       # Individual lab exercises
│   ├── scripts/                    # Setup and utility scripts
│   └── sample-projects/            # Sample applications
├── day2-jenkins-integrations/      # Day 2: Advanced Jenkins
│   ├── labs/                       # Pipeline and integration labs
│   ├── scripts/                    # Jenkins setup scripts
│   └── pipelines/                  # Sample Jenkinsfiles
├── day3-full-cicd/                 # Day 3: Complete CI/CD
│   ├── labs/                       # Advanced Git and CI/CD labs
│   ├── scripts/                    # Full environment setup
│   └── team-workflow/              # Team simulation scenarios
└── shared/                         # Shared resources
    ├── docker-compose.yml          # Complete environment
    ├── sample-apps/                # Multi-language applications
    └── documentation/              # Additional resources
```

## Prerequisites

### System Requirements
- **Operating System**: Windows, macOS, or Linux
- **Memory**: Minimum 8GB RAM (16GB recommended)
- **Storage**: 20GB free space
- **Network**: Internet connection for downloads

### Software Requirements
- **Docker**: Latest version with Docker Compose
- **Git**: Version 2.30 or higher
- **Text Editor**: VS Code, Vim, or similar
- **Browser**: Chrome, Firefox, or Safari
- **Terminal**: Command line access

### Accounts Needed
- **GitHub**: Free account for repository hosting
- **Email**: For Jenkins notifications (optional)

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd jenkins-git-associate/lab-environment
```

### 2. Setup Day 1 Environment
```bash
cd day1-git-fundamentals
./scripts/setup-day1-environment.sh
```

### 3. Start Jenkins
```bash
cd jenkins-setup
./start-jenkins.sh
```

### 4. Begin Labs
Follow the lab instructions in order:
- Lab 1.1: Git Installation and Basic Workflow
- Lab 1.2: GitHub Integration
- Lab 1.3: Jenkins Installation and First Job
- Lab 1.4: Git + Jenkins Integration

## Lab Progression

### Progressive Complexity
Each day builds upon the previous day's knowledge:

1. **Day 1**: Basic concepts and simple workflows
2. **Day 2**: Advanced features and complex integrations
3. **Day 3**: Real-world scenarios and team workflows

### Hands-On Approach
- **70% Hands-on**: Practical exercises and real scenarios
- **20% Theory**: Concepts explained through examples
- **10% Assessment**: Knowledge checks and demonstrations

### Real-World Applications
- **Sample Applications**: Java, Node.js, Python, and static websites
- **Team Scenarios**: Multiple developers working on the same project
- **Production Patterns**: Industry-standard CI/CD practices

## Learning Outcomes

### Technical Skills
- **Git Mastery**: Advanced branching, merging, and collaboration
- **Jenkins Expertise**: Pipelines, integrations, and automation
- **CI/CD Implementation**: Complete automation workflows
- **DevOps Practices**: Infrastructure as Code and monitoring

## Support and Resources

### Documentation
- **Lab Instructions**: Step-by-step guides for each exercise
- **Troubleshooting**: Common issues and solutions
- **Best Practices**: Industry standards and recommendations
- **Reference Materials**: Quick reference guides and cheat sheets

### Instructor Support
- **Live Assistance**: Help during lab exercises
- **Code Reviews**: Feedback on implementations
- **Best Practices**: Guidance on industry standards
- **Certification Prep**: Exam preparation and tips

### Community Resources
- **GitHub Repository**: Sample code and additional exercises
- **Discussion Forums**: Q&A and knowledge sharing
- **Video Tutorials**: Supplementary learning materials
- **Blog Posts**: Advanced topics and real-world examples


## Contact and Support

For questions, issues, or additional support:
- **Email**: training@bittnet.ro
- **GitHub**: Create issues in the repository
- **Documentation**: Check the comprehensive guides
- **Instructor**: Available during lab sessions

---

**Happy Learning!** 🚀

This lab environment is designed to provide you with practical, hands-on experience that directly prepares you for the Jenkins & Git Associate certification and real-world CI/CD implementation.
