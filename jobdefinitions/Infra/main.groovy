// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

pipelineJob(jobPrefix + 'SelfServeHelmsman') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/cloudprovisioning.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/helmsman/provision_cluster.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'OpaExecution') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    textParam("opa_spec", "", "The opa policy spec content")
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/opa-policies.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/opa/opa_pipeline.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'EnableDisableAllJobs') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('ACTION', ['Enable', 'Disable'], 'Enable or Disable All Jobs on this host')
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/nimbus-jenkins-jobs.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/saas/enable_disable.groovy')
    }
  }
}
