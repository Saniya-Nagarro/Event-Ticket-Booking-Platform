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
                            bat 'mvn clean compile'
                        }
                    }
                }

                stage('Build Event Service') {
                    steps {
                        dir('event-service') {
                            bat 'mvn clean compile'
                        }
                    }
                }

                stage('Build Booking Service') {
                    steps {
                        dir('booking-service') {
                            bat 'mvn clean compile'
                        }
                    }
                }

                stage('Build Notification Service') {
                    steps {
                        dir('notification-service') {
                            bat 'mvn clean compile'
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
                            bat 'mvn test'
                        }
                    }
                }

                stage('Test Event Service') {
                    steps {
                        dir('event-service') {
                            bat 'mvn test'
                        }
                    }
                }

                stage('Test Booking Service') {
                    steps {
                        dir('booking-service') {
                            bat 'mvn test'
                        }
                    }
                }

                stage('Test Notification Service') {
                    steps {
                        dir('notification-service') {
                            bat 'mvn test'
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_ENV}") {
                    dir('user-service') {
                        bat 'mvn sonar:sonar'
                    }
                    dir('event-service') {
                        bat 'mvn sonar:sonar'
                    }
                    dir('booking-service') {
                        bat 'mvn sonar:sonar'
                    }
                    dir('notification-service') {
                        bat 'mvn sonar:sonar'
                    }
                }
            }
        }

        stage('Package') {
            parallel {
                stage('Package User Service') {
                    steps {
                        dir('user-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }

                stage('Package Event Service') {
                    steps {
                        dir('event-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }

                stage('Package Booking Service') {
                    steps {
                        dir('booking-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }

                stage('Package Notification Service') {
                    steps {
                        dir('notification-service') {
                            bat 'mvn clean package -DskipTests'
                        }
                    }
                }
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