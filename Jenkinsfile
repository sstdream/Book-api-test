pipeline {
    agent any
    tools {
            maven 'Maven-3.9.12'   // 与全局工具配置中的名称一致
        }
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
                        allowMissing: true,
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