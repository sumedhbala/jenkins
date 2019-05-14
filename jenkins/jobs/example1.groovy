pipelineJob('example1') {
    definition {
        cps {
            script(readFileFromWorkspace('jenkins/pipelines/example1.groovy'))
            sandbox()
        }
    }
}

