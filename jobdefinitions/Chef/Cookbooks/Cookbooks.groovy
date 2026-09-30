// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

// create a job for each cookbook
cookbooks.each {
  def reponame = "${it}"
  multibranchPipelineJob(jobPrefix + "${reponame}") {
    configure perms.setFolderPermissions(this, ['ops'], deployEnvironment)
    branchSources {
      git {
        remote("${sshGitUrl}/${project}/${reponame}.git")
        credentialsId("${gitCreds}")
        id('master')
      }
    }
    orphanedItemStrategy {
      discardOldItems {
          numToKeep(5)
          daysToKeep(10)
      }
    }
  }
}
