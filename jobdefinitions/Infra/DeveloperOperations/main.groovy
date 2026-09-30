// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

pipelineJob(jobPrefix + 'KubectlToken') {
  configure perms.setPermissions(this, ['ops','developers'], deployEnvironment)
  parameters {
    stringParam('USER_NAME', '', 'Please provide the LDAP username')
    stringParam('JIRA_TICKET', '', 'Please provide the Jira ticket number')
    choiceParam('eksCluster', eksClusterList, 'EKS cluster to use')
    choiceParam('DURATION', ['3600', '7200', '10800', '14400', '43200'], 'Duration (in seconds) for which the credentials would be valid')
    booleanParam('AUTOSEND', false, 'Send Credentials via Link(WIP)')
  }
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
      scriptPath('pipelines/kubectl/developer_operations.groovy')
    }
  }
}
