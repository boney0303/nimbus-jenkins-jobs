// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

pipelineJob(jobPrefix + 'SharedPrivateLinkService') {
  configure perms.setPermissions(this, ['ops', 'analytics'], deployEnvironment)
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
      scriptPath('services/shared_privatelink_service/pipeline.groovy')
    }
  }
}
