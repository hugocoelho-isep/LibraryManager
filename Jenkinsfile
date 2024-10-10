pipeline {
    agent any

    environment {
        // Define any environment variables needed
        //MAVEN_HOME = tool 'M3'  // Assuming you have defined Maven in Jenkins Global Tools
        //JAVA_HOME = tool 'JDK11' // Adjust to the correct JDK version

        // LINUX
        //MAVEN_HOME = '/usr/local/apache-maven'
        //JAVA_HOME = '/usr/lib/jvm/java-11-openjdk'

        // WINDOWS
        MAVEN_HOME = 'C:\\Program Files\\Apache\\apache-maven-3.8.6'
        JAVA_HOME = 'C:\\Program Files\\Java\\jdk-17'

        // macOS
        //MAVEN_HOME = '/usr/local/apache-maven'
        //JAVA_HOME = '/Library/Java/JavaVirtualMachines/jdk-11.0.11.jdk/Contents/Home'

        // Define the GitHub repository URL as an environment variable
        GITHUB_URL = 'https://github.com/hugocoelho-isep/LibraryManager.git'
        GITHUB_BRANCH = 'main'
        GITHUB_ID = '22c33e88-6dc6-44e0-93bc-dd93dc7f0e31'
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout the code from your repository
                git url: "${GITHUB_URL}", branch: "${GITHUB_BRANCH}", credentialsId: "${GITHUB_ID}"
            }
        }

        stage('Build') {
            steps {
                // Clean, compile, and package the application using Maven
                sh "${MAVEN_HOME}/bin/mvn clean install"
            }
        }

        stage('Validate') {
            steps {
                // Validate the project structure
                sh "${MAVEN_HOME}/bin/mvn validate"
            }
        }

        stage('Compile') {
            steps {
                // Compile the source code
                sh "${MAVEN_HOME}/bin/mvn compile"
            }
        }

        stage('Test') {
            steps {
                // Run unit tests with Maven
                sh "${MAVEN_HOME}/bin/mvn test"
            }
        }

        stage('Package') {
            steps {
                // Package the application into a JAR or WAR
                sh "${MAVEN_HOME}/bin/mvn package"
            }
        }

        stage('Deploy') {
            steps {
                // Deploy to your server or container platform (adjust the steps as necessary)
                sh '''
                echo "Deploying application..."
                # For example, you can use SCP to copy the jar/war to a server
                scp target/your-app.jar user@server:/path/to/deploy/
                '''
            }
        }
    }

    post {
        success {
            // Actions to take when the pipeline succeeds
            echo 'Pipeline completed successfully!'
        }
        failure {
            // Actions to take when the pipeline fails
            echo 'Pipeline failed!'
        }
    }
}