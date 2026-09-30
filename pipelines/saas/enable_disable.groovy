import hudson.matrix.MatrixProject
import hudson.model.FreeStyleProject
import hudson.model.Job
import jenkins.model.Jenkins

@Library('saas') _

node {
  pipelineSetup('skyline')
}

pipeline {
  agent { label 'master' }
    options {
      timestamps()
      ansiColor('xterm')
    }
  parameters{
    choice(
      name: 'ACTION',
      choices: ['Enable', 'Disable'],
      description: 'Enable or Disable All Jobs on this host'
    )
  }
  stages{
    stage('Disable All Jobs') {
      when{
        allOf{
            expression {params.ACTION == 'Disable'}
          }
        }
      steps {
        script {
          Jenkins.instance.getAllItems(Job.class).findAll { Job j ->
            if ( j.fullName.contains("${env.JOB_NAME}") ) {
              println "Skipping this job"
            }
            j.disabled = true
            j.save()
            println "Disabled ${j.fullName}"
          }
        }
      }
    }
    stage('Enable All Jobs') {
      when{
        allOf{
            expression {params.ACTION == 'Enable'}
          }
        }
      steps {
        script {
          Jenkins.instance.getAllItems(Job.class).findAll { Job j ->
            j.disabled = false
            j.save()
            println "Enabled ${j.fullName}"
          }
        }
      }
    }
  }
  post {
    always {
      cleanUp()
    }
  }
}
