pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                bat 'mvn clean compile'
            }
        }
        stage('Run API Tests') {
            steps {
                bat 'mvn test'
            }
            post {
                always {
                    publishHTML([
                        reportDir: 'target/surefire-reports',
                        reportFiles: 'index.html',
                        reportName: 'TestNG Report',
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true
                    ])
                }
            }
        }
        stage('Run Performance Tests') {
            steps {
                bat 'mvn exec:java -Dexec.mainClass="PerformanceTest"'
            }
        }
    }
}