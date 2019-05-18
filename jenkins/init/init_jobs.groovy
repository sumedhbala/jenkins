pipeline {
    agent any
    parameters {
    string(description: 'GIT URL', name: 'git_url')
    string(description: 'GIT BRANCH', name: 'branch')
    string(description: 'PATH TO JOBDSL SCRIPTS', name: 'scripts')
    }
    stages {
        stage('Initialize') {
            steps {
                git branch: "${params.branch}",
                    url: "${params.git_url}"
                jobDsl targets: ["${params.scripts}"].join('\n'),
                sandbox: true
            }
        }
    }
}
