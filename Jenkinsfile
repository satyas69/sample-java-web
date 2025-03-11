pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', 
                url: 'https://github.com/satyas69/sample-java-web.git',
                credentialsId: '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' // Use the ID you specified in Jenkins
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Deploy') {
            steps {
                sh 'scp target/your-app.war ec2-user@your-ec2-public-ip:/opt/tomcat/webapps/'
            }
        }
    }
}
