pipeline {
    agent any
    options {
        disableConcurrentBuilds()
        // 打开这里就不会默认clone工作区了
        // skipDefaultCheckout()
        timeout(time: 1, unit: 'HOURS')
    }
    environment {
      timeString = sh returnStdout: true, script: "date +%Y%m%d%H%M"
    }
    stages {
        stage('Build Jar') {
            agent {
                docker {
                    image 'gradle:jre11-slim'
                    args '-v /data/.gradle:/home/gradle/.gradle'
                    // 非常重要的一个选项，如果不指定这个，gradle会运行在另一个拷贝的工作区
                    reuseNode true
                }
            }
            steps {
                sh 'gradle -x test -Dorg.gradle.daemon=false build'
            }
        }
        stage('Build Docker Image') {
            agent none
            when {
                // 需要在Jenkins中设置全局环境变量以区分各个不同的实例
                environment ignoreCase: true, name: 'JENKINS_NAME', value: 'cj'
                beforeAgent true
            }
            steps {
                echo 'rm all running images or stopped images'
                script {
                    try {
                        sh "docker ps -a|grep -E 'phoebus|Created|Exited'|cut -d ' ' -f 1|xargs -r docker rm"
                    } catch (Exception e) {
                        echo 'rm all running images fail'
                    }
                }

                echo 'build new images'
                script {
                    def pid = sh returnStdout: true, script: "ls -al ./"
                    echo "========= ${pid} ======="
                }
                sh 'docker build . -t phoebus:${timeString}'
                echo 'remove old images'
                script {
                    try {
                        sh "docker images|grep none|awk '{print \$3}'|xargs docker rmi"
                    } catch (Exception e) {
                        echo 'rm all image fail'
                    }
                }
                echo 'running new image'
                sh 'docker run -d --restart=always phoebus java -jar -Xmx200M app.jar --spring.profiles.active=dev'
            }
        }
        stage('Deploy to Kubernetes') {
            agent none
            when {
                environment ignoreCase: true, name: 'JENKINS_NAME', value: 'cj'
                beforeAgent true
            }
            steps {
                // 替换所有的Image版本
                echo "replace image version to ${timeString}"
                contentReplace(
                    configs: [
                        fileContentReplaceConfig(
                            configs: [
                                // fileContentReplaceItemConfig(
                                //     search: 'latest',
                                //     replace: "${timeString}",
                                //     matchCount: 1
                                // ),
                                fileContentReplaceItemConfig(
                                    search: 'active=dev',
                                    replace: 'active=dev2',
                                    matchCount: 1
                                )
                            ],
                            fileEncoding: 'UTF-8',
                            filePath: 'kubernetes.yaml'
                        )
                    ]
                )
                // 发布到本地Kubernetes
                // echo 'publish to local test kubernetes'
                // withKubeConfig([credentialsId: 'k8s-18', serverUrl: 'https://192.168.0.18:6443']) {
                //       sh 'kubectl apply -f kubernetes.yaml'
                // }
            }
        }
    }
}