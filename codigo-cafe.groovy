pipeline {

    environment {
        JAVA_HOME = '/opt/java/jdk-22.0.2'
        MAVEN_HOME = '/opt/maven'
        PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${env.PATH}"

        DOCKER_IMAGE = 'codigo-cafe'
        DOCKER_CONTAINER_NAME = 'codigo-cafe'
    }

    stages {
        stage('Notify Start') {
            steps {
                script {
                    withCredentials([
                            string(credentialsId: 'TELEGRAM_BOT_TOKEN', variable: 'TG_TOKEN'),
                            string(credentialsId: 'TELEGRAM_CHAT_ID', variable: 'TG_CHAT')
                    ]) {
                        sh """
                        curl -s -X POST https://api.telegram.org/bot${TG_TOKEN}/sendMessage \
                          -d chat_id="${TG_CHAT}" \
                          --data-urlencode text="🚀 Jenkins: inició *${JOB_NAME}* #${BUILD_NUMBER}" \
                          -d parse_mode=Markdown
                        """
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                git url: "https://github.com/CodigoyCafe01/app-docker.git", branch: 'main'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -Dmaven.test.skip=true'
            }
        }

        stage('Docker Stop and Remove') {
            steps {
                sh """
                    docker stop ${env.DOCKER_CONTAINER_NAME} || true
                    docker rm ${env.DOCKER_CONTAINER_NAME} || true
                """
            }
        }

        stage('Docker Build') {
            steps {
                sh """
                    export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}
                    docker build -t ${env.DOCKER_IMAGE} .
                """
            }
        }

        stage('Docker compose') {
            steps {
                sh """
                    docker-compose up -d --build
                """
            }
        }
    }

    post {
        success {
            script {
                withCredentials([
                        string(credentialsId: 'TELEGRAM_BOT_TOKEN', variable: 'TG_TOKEN'),
                        string(credentialsId: 'TELEGRAM_CHAT_ID', variable: 'TG_CHAT')
                ]) {
                    sh """
                    curl -s -X POST https://api.telegram.org/bot${TG_TOKEN}/sendMessage \
                      -d chat_id="${TG_CHAT}" \
                      --data-urlencode text="✅ Jenkins: *${JOB_NAME}* #${BUILD_NUMBER} finalizó con ÉXITO" \
                      -d parse_mode=Markdown
                    """
                }
            }
        }
        failure {
            script {
                withCredentials([
                        string(credentialsId: 'TELEGRAM_BOT_TOKEN', variable: 'TG_TOKEN'),
                        string(credentialsId: 'TELEGRAM_CHAT_ID', variable: 'TG_CHAT')
                ]) {
                    sh """
                    curl -s -X POST https://api.telegram.org/bot${TG_TOKEN}/sendMessage \
                      -d chat_id="${TG_CHAT}" \
                      --data-urlencode text="❌ Jenkins: *${JOB_NAME}* #${BUILD_NUMBER} FALLÓ" \
                      -d parse_mode=Markdown
                    """
                }
            }
        }
        always {
            echo 'Pipeline completado'
        }
    }
}
