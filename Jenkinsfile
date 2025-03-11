pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Deploy') {
            steps {
                sh 'scp target/jsp-project.war ec2-user@your-ec2-public-ip:/opt/tomcat/webapps/'
            }
        }
    }
}
