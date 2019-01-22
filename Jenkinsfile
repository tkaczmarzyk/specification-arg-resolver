pipeline {
    agent {
        docker { 
            image 'softleader/openjdk8:font.build-env' 
            args '-v /opt/jenkins/m2:/root/.m2'
        }
    }
    stages {
        stage('deploy') {
            steps {
                sh 'mvn clean deploy -DskipTests=true -e'
            }
        }
    }
}
