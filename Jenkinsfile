pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/sstdream/Book-api-test.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('Run API Tests') {
            steps {
                sh 'mvn test'
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
                sh 'mvn exec:java -Dexec.mainClass="PerformanceTest"'
            }
        }
    }
}