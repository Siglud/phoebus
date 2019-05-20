pipeline {
    agent none
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
            steps {
                def today = sh returnStdout: true, script: "date +%Y%m%d%H%M"
                echo 'Hello world!, @ ${today}'
            }
        }
    }
}