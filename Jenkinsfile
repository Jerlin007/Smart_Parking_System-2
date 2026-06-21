pipeline {
agent any


tools {
    maven 'Maven-3.9'
    jdk 'JDK-21'
    nodejs 'Node-24'
}

environment {
    BACKEND_JAR = 'target/*.jar'
    FRONTEND_BUILD = 'frontend/build'
}

stages {

    stage('Checkout') {
        steps {
            checkout scm
        }
    }

    stage('Build Backend') {
        steps {
            bat 'mvn clean package -DskipTests'
        }
        post {
            success {
                archiveArtifacts artifacts: BACKEND_JAR, fingerprint: true
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
                archiveArtifacts artifacts: "${FRONTEND_BUILD}/**"
            }
        }
    }
}

post {
    failure {
        echo "Pipeline failed at: ${env.STAGE_NAME}"
    }

    success {
        echo 'Pipeline completed successfully'
    }

    always {
        cleanWs()
    }
}

}
