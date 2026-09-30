// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

pipelineJob(jobPrefix + 'TerraformCreateClusterService') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('TASK', ['plan-all', 'plan', 'apply-all', 'apply', 'output', 'output-all', 'destroy-all', 'destroy'])
    choiceParam('TERRAFILE_ENV', ['prd', 'stage'])
    stringParam('VPC_ID', '', 'Please provide VPC id')
    booleanParam('IGNORE_DEPENDENCY_ERRORS', false, "'*-all' commands continue processing components even if a dependency fails")
    booleanParam('force', false, 'Flag for force run (will skip approval stage)')
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/live-terraform.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('services/splunk_cluster_service/pipeline.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'TerraformCreateSplunkHeavyForwarderService') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('TASK', ['plan-all', 'plan', 'apply-all', 'apply', 'output', 'output-all', 'destroy-all', 'destroy'])
    choiceParam('TERRAFILE_ENV', ['prd', 'stage'])
    stringParam('VPC_ID', '', 'Please provide VPC id')
    booleanParam('IGNORE_DEPENDENCY_ERRORS', false, "'*-all' commands continue processing components even if a dependency fails")
    booleanParam('force', false, 'Flag for force run (will skip approval stage)')
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/live-terraform.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('services/splunk_heavyforwarder_service/pipeline.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'DNSUpserts') {
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
      scriptPath('pipelines/splunk/createDNSSplunkEntries.groovy')
    }
  }
}
