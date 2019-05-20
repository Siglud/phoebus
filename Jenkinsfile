pipeline {
    agent none
    options {
        disableConcurrentBuilds()
        skipDefaultCheckout()
        timeout(time: 1, unit: 'HOURS')
    }
    environment {
        NOW = sh returnStdout: true, script: "date +%Y%m%d%H%M"
    }
    stages {
        stage('echo stage') {
            agent {
                label 'master'
            }
            steps {
                echo 'Hello world!, @ ${NOW}'
            }
        }
    }
}