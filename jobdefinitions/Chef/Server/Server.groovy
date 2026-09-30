// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

pipelineJob(jobPrefix + 'Check-Setup') {
  configure perms.setPermissions(this, ['ops', 'analytics'], deployEnvironment)
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/chef-repo.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/checks/checkSetup.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'RunKnife') {
  configure perms.setPermissions(this, ['ops', 'analytics'], deployEnvironment)
  parameters {
    stringParam('chef_node_id', '', 'The node Id - leave blank for all nodes')
    stringParam('chef_attribute', '', 'Only show a certain attribute(only used if chef node Id is entered)')
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/chef-repo.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/chef-knife-node.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'SyncFromRepo') {
  configure perms.setPermissions(this, ['ops', 'analytics'], deployEnvironment)
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/chef-repo.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/chefrepo.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'UploadCookbook') {
  configure perms.setPermissions(this, ['ops', 'analytics'], deployEnvironment)
  parameters {
    choiceParam('cookbook', cookbooks)
    stringParam('version', '', 'The cookbook version being uploaded')
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/chef-repo.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/upload-cookbook.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'ReleaseCookbook') {
  configure perms.setPermissions(this, ['ops', 'analytics'], deployEnvironment)
  parameters {
    choiceParam('cookbook', cookbooks)
    stringParam('version', '', 'The cookbook version being released')
    stringParam('ticket', '', 'Enter the JIRA ticket number associated with this release')
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/chef-repo.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/release-cookbook.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'PromoteCookbook') {
  configure perms.setPermissions(this, ['ops', 'analytics'], deployEnvironment)
  parameters {
    choiceParam('cookbook', cookbooks)
  }
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/chef-repo.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/promote-cookbook.groovy')
    }
  }
}
