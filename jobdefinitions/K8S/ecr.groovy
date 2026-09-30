// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

if ( awsRegion == 'us-east-1') {
  if ( awsAccountNumber == '111111111111') {
    pipelineJob(jobPrefix + 'ECR-CreateApplyPolicy') {
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
          scriptPath('pipelines/ecr/create_ecr_with_policy.groovy')
        }
        triggers {
          cron('@hourly')
        }
      }
    }
  }
}
