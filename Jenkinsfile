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
                echo 'rm all running images or stopped images'
                sh "docker ps -a|grep -E 'phoebus|Created|Exited'|cut -d ' ' -f 1|xargs -r docker rm"
                echo 'build new images'
                sh 'docker build . -t phoebus'
                echo 'remove old images'
                sh "docker images|grep none|awk '{print \$3}'|xargs docker rmi"
                echo 'running new image'
                sh 'docker run -d --restart=always phoebus java -jar -Xmx200M app.jar --spring.profiles.active=dev'
            }
        }
    }
}