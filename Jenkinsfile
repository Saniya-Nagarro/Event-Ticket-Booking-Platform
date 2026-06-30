pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven-3.9'
    }

    environment {
        SONARQUBE_ENV = 'SonarQube'
        IMAGE_PREFIX = 'event-ticket'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -f user-service/pom.xml clean compile'
                bat 'mvn -f event-service/pom.xml clean compile'
                bat 'mvn -f booking-service/pom.xml clean compile'
                bat 'mvn -f notification-service/pom.xml clean compile'
            }
        }

        stage('Unit Testing + JaCoCo') {
            steps {
                bat 'mvn -f user-service/pom.xml test'
                bat 'mvn -f event-service/pom.xml test'
                bat 'mvn -f booking-service/pom.xml test'
                bat 'mvn -f notification-service/pom.xml test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    bat 'mvn -f user-service/pom.xml sonar:sonar -Dsonar.projectKey=user-service -Dsonar.projectName=user-service'
                    bat 'mvn -f event-service/pom.xml sonar:sonar -Dsonar.projectKey=event-service -Dsonar.projectName=event-service'
                    bat 'mvn -f booking-service/pom.xml sonar:sonar -Dsonar.projectKey=booking-service -Dsonar.projectName=booking-service'
                    bat 'mvn -f notification-service/pom.xml sonar:sonar -Dsonar.projectKey=notification-service -Dsonar.projectName=notification-service'
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -f user-service/pom.xml clean package -DskipTests'
                bat 'mvn -f event-service/pom.xml clean package -DskipTests'
                bat 'mvn -f booking-service/pom.xml clean package -DskipTests'
                bat 'mvn -f notification-service/pom.xml clean package -DskipTests'
            }
        }

        stage('Docker Image Build') {
            steps {
                bat 'docker build -t event-ticket/user-service:latest ./user-service'
                bat 'docker build -t event-ticket/event-service:latest ./event-service'
                bat 'docker build -t event-ticket/booking-service:latest ./booking-service'
                bat 'docker build -t event-ticket/notification-service:latest ./notification-service'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }

        success {
            echo 'Week 3 CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'Week 3 CI/CD pipeline failed. Check console logs.'
        }
    }
}