pipeline {
    agent any
    options {
        disableConcurrentBuilds()
        // skipDefaultCheckout()
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('Build Jar') {
            agent {
                docker {
                    image 'gradle:jre11-slim'
                    args '-v /data/.gradle:/root/.gradle'
                }
            }
            steps {
                sh 'gradle build'
            }
        }
        stage('Build Docker Image') {
            when {
                environment ignoreCase: true, name: 'JENKINS_NAME', value: 'cj'
                beforeAgent true
            }
            steps {
                sh 'docker build . -t phoebus'
            }
        }
    }
}