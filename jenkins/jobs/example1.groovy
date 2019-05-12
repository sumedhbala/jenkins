pipeline {
    agent any
    parameters {
    string(description: '', name: 'git_url')
    string(description: '', name: 'branch')
    }
    stages {
        stage('Initialize') {
            steps {
                git branch: "${params.branch}",
                    url: "${params.git_url}" 
                jobDsl targets: ["jenkins/pipelines/example1.groovy"].join('\n'),
                sandbox: true
            }
        }
    }
}

