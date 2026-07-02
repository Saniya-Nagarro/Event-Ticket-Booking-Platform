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
                withCredentials([
                    string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')
                ]) {

                    bat '''
                        mvn -f user-service/pom.xml sonar:sonar ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.login=%SONAR_TOKEN% ^
                        -Dsonar.projectKey=user-service ^
                        -Dsonar.projectName=user-service
                    '''

                    bat '''
                        mvn -f event-service/pom.xml sonar:sonar ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.login=%SONAR_TOKEN% ^
                        -Dsonar.projectKey=event-service ^
                        -Dsonar.projectName=event-service
                    '''

                    bat '''
                        mvn -f booking-service/pom.xml sonar:sonar ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.login=%SONAR_TOKEN% ^
                        -Dsonar.projectKey=booking-service ^
                        -Dsonar.projectName=booking-service
                    '''

                    bat '''
                        mvn -f notification-service/pom.xml sonar:sonar ^
                        -Dsonar.host.url=http://localhost:9000 ^
                        -Dsonar.login=%SONAR_TOKEN% ^
                        -Dsonar.projectKey=notification-service ^
                        -Dsonar.projectName=notification-service
                    '''
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
            when {
                expression {
                    return false
                }
            }
            steps {
                echo 'Docker build skipped'
            }
        }
    }

    post {

        always {
            junit allowEmptyResults: true,
                  testResults: '**/target/surefire-reports/*.xml'

            archiveArtifacts artifacts: '**/target/*.jar',
                             allowEmptyArchive: true
        }

        success {
            echo 'CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'CI/CD pipeline failed. Check console logs.'
        }
    }
}