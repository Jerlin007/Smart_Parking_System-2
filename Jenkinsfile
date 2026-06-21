pipeline {
agent any

tools {
    maven 'Maven-3.9'
    jdk 'JDK-21'
    nodejs 'Node-24'
}

environment {
    BACKEND_IMAGE = 'smart-parking-app'
    FRONTEND_IMAGE = 'smart-parking-frontend'
    IMAGE_TAG = "${env.BUILD_NUMBER}"
    K8S_NAMESPACE = 'parking-system'
}

stages {

    stage('Checkout') {
        steps {
            checkout scm
        }
    }

    stage('Verify Tools') {
        steps {
            bat 'java -version'
            bat 'mvn -version'
            bat 'node -v'
            bat 'npm -v'
            bat 'docker --version'
            bat 'kubectl version --client'
            bat 'minikube version'
        }
    }

    stage('Build Backend') {
        steps {
            bat 'mvn clean package -DskipTests'
        }
        post {
            success {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    stage('Run Tests') {
        steps {
            bat 'mvn test'
        }
        post {
            always {
                junit testResults: 'target/surefire-reports/*.xml',
                      allowEmptyResults: true
            }
        }
    }

    stage('Build Frontend') {
        steps {
            dir('frontend') {
                bat 'npm install'
                bat 'npm run build'
            }
        }
        post {
            success {
                archiveArtifacts artifacts: 'frontend/build/**'
            }
        }
    }

    stage('Docker Check') {
        steps {
            bat 'docker version'
            bat 'docker ps'
        }
    }

    stage('Build Docker Images') {
        steps {

            bat 'docker build -t %BACKEND_IMAGE%:latest .'

            dir('frontend') {
                bat 'docker build -t %FRONTEND_IMAGE%:latest .'
            }

            bat "docker tag %BACKEND_IMAGE%:latest %BACKEND_IMAGE%:%IMAGE_TAG%"
            bat "docker tag %FRONTEND_IMAGE%:latest %FRONTEND_IMAGE%:%IMAGE_TAG%"

            bat 'docker images | findstr smart-parking'
        }
    }

    stage('Verify Minikube') {
        steps {
            bat 'minikube status'
            bat 'kubectl config use-context minikube'
            bat 'kubectl get nodes'
        }
    }

    stage('Load Images into Minikube') {
        steps {

            bat "minikube image load %BACKEND_IMAGE%:latest"
            bat "minikube image load %BACKEND_IMAGE%:%IMAGE_TAG%"

            bat "minikube image load %FRONTEND_IMAGE%:latest"
            bat "minikube image load %FRONTEND_IMAGE%:%IMAGE_TAG%"
        }
    }

    stage('Deploy to Kubernetes') {
        steps {
            bat 'kubectl apply -f k8s/'
        }
    }

    stage('Verify Deployment') {
        steps {
            bat 'kubectl get pods -n %K8S_NAMESPACE%'
            bat 'kubectl get svc -n %K8S_NAMESPACE%'
            bat 'kubectl get deployments -n %K8S_NAMESPACE%'
        }
    }

    stage('Wait For Pods') {
        steps {

            bat 'kubectl rollout status deployment/mysql-deployment -n %K8S_NAMESPACE% --timeout=180s'

            bat 'kubectl rollout status deployment/smart-parking-backend -n %K8S_NAMESPACE% --timeout=300s'

            bat 'kubectl rollout status deployment/smart-parking-frontend -n %K8S_NAMESPACE% --timeout=180s'
        }
    }

    stage('Health Check') {
        steps {

            bat 'kubectl get pods -n %K8S_NAMESPACE%'

            bat 'kubectl get svc -n %K8S_NAMESPACE%'

            bat 'kubectl get deployments -n %K8S_NAMESPACE%'

            bat 'kubectl get ingress -n %K8S_NAMESPACE%'
        }
    }
}

post {

    success {
        echo '======================================='
        echo 'PIPELINE COMPLETED SUCCESSFULLY'
        echo 'SMART PARKING DEPLOYED TO KUBERNETES'
        echo '======================================='
    }

    failure {
        echo '======================================='
        echo 'PIPELINE FAILED'
        echo 'CHECK STAGE LOGS'
        echo '======================================='
    }

    always {
        cleanWs()
    }
}
}
