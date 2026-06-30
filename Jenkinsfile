pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        DOCKER_IMAGE_PREFIX = "event-ticket"
        SONARQUBE_ENV = "SonarQube"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            parallel {
                stage('Build User Service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn clean compile'
                        }
                    }
                }

                stage('Build Event Service') {
                    steps {
                        dir('event-service') {
                            sh 'mvn clean compile'
                        }
                    }
                }

                stage('Build Booking Service') {
                    steps {
                        dir('booking-service') {
                            sh 'mvn clean compile'
                        }
                    }
                }

                stage('Build Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn clean compile'
                        }
                    }
                }
            }
        }

        stage('Unit Testing + JaCoCo') {
            parallel {
                stage('Test User Service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn test'
                        }
                    }
                }

                stage('Test Event Service') {
                    steps {
                        dir('event-service') {
                            sh 'mvn test'
                        }
                    }
                }

                stage('Test Booking Service') {
                    steps {
                        dir('booking-service') {
                            sh 'mvn test'
                        }
                    }
                }

                stage('Test Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn test'
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    dir('user-service') {
                        sh 'mvn sonar:sonar'
                    }
                    dir('event-service') {
                        sh 'mvn sonar:sonar'
                    }
                    dir('booking-service') {
                        sh 'mvn sonar:sonar'
                    }
                    dir('notification-service') {
                        sh 'mvn sonar:sonar'
                    }
                }
            }
        }

        stage('Package') {
            parallel {
                stage('Package User Service') {
                    steps {
                        dir('user-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }

                stage('Package Event Service') {
                    steps {
                        dir('event-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }

                stage('Package Booking Service') {
                    steps {
                        dir('booking-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }

                stage('Package Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Docker Image Build') {
            steps {
                sh 'docker build -t event-ticket/user-service:latest ./user-service'
                sh 'docker build -t event-ticket/event-service:latest ./event-service'
                sh 'docker build -t event-ticket/booking-service:latest ./booking-service'
                sh 'docker build -t event-ticket/notification-service:latest ./notification-service'
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
        }

        success {
            echo 'CI pipeline completed successfully.'
        }

        failure {
            echo 'CI pipeline failed.'
        }
    }
}
