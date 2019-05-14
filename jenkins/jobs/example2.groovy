pipelineJob('example2') {
    triggers {
        cron('*/15 * * * *')
    }
    definition {
        cps {
            script(readFileFromWorkspace('jenkins/pipelines/examplexi21.groovy'))
            sandbox()
        }
    }
}

