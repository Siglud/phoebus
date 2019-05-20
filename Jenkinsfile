pipeline {
    agent any
    options {
        disableConcurrentBuilds()
        skipDefaultCheckout()
        timeout(time: 1, unit: 'HOURS')
    }
    stages {
        stage('echo stage') {
            agent {
                docker {
                    image 'gradle:jre11-slim'
                    args '-v /data/.gradle:/root/.gradle'
                }
            }
            steps {
                sh 'gradle clean && gradle build'
            }
        }
    }
}