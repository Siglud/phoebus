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
                label 'master'
            }
            script {
                now = sh(returnStdout: true, script: 'date +%Y%m%d%H%M')
            }
            steps {
                echo 'Hello world!, @ ${NOW}'
            }
        }
    }
}