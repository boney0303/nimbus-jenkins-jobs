// SAMPLE Pipeline Job
// Here is a list of the vars needed for each job definition
// Set these at the top of your file to be used in all your jobs defined within the same file.
// jobPrefix - will be set by the full path to this file, starting after 'jobdefinitions'- Example: 'samples'
// NameOfJobWithoutSpaces - last portion of job name within UI
// TeamName - provide environment/team-level access scheme - contact #devops for more info
// sshGitUrl - provided from SEED job - leave as is
// credentialsId  - provided from Seed job - leave as is

// This is a sample of a (single-branch) pipeline job - copy it to your directory
// and modify to suit your needs

// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = '<git-project-name>'
def perms = new JobPermissions()

// SAMPLE - your changes should be made below this line
pipelineJob(jobPrefix + 'NameOfJobWithoutSpaces') {
  // set permissions for team
  // see nimbus-jenkins-shared-libraries/browse/src/com/nimbus/JobPermissions.groovy for list of team names
  configure perms.setPermissions(this, ['TeamName'], deployEnvironment)

  // parameters must be configured here AND within the pipeline
  parameters {
    choiceParam('nameofChoiceParam', ['choice1', 'choice2', 'etc'], 'Description of choiceParam')
    stringParam('nameofStringParam', 'defaultValue', 'Description of stringParam')
    booleanParam('nameOfBooleanParam', true, 'Description of booleanParam')
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            remote("${sshGitUrl}/${project}/bitBucketRepoName.git")
            credentialsId(env.SSH_CREDS)
            branch('*/master')
          }
        }
      }
      scriptPath('path/to/pipeline/within/repo/pipelineFilename.groovy')
    }
  }
}

// if you need more than a couple of jobs created that are very similar, you
// can use groovy loops to create them.
