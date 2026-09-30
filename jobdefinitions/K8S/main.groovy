// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

pipelineJob(jobPrefix + 'CreateNamespace') {
  configure perms.setPermissions(this, ['dev','ops'], deployEnvironment)
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
      scriptPath('pipelines/kubectl/create_namespace.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'DescribeResource') {
  configure perms.setPermissions(this, ['dev','ops'], deployEnvironment)
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
      scriptPath('pipelines/kubectl/describe_resource.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'GetResources') {
  configure perms.setPermissions(this, ['dev','ops'], deployEnvironment)
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
      scriptPath('pipelines/kubectl/get_resources.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'DrainNodes') {
  configure perms.setPermissions(this, ['dev'], deployEnvironment)
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
      scriptPath('pipelines/eks/drain_nodes.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'RollingDrainNodes') {
  configure perms.setPermissions(this, ['dev'], deployEnvironment)
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
      scriptPath('pipelines/eks/rolling_drain_nodes.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'K8sRecycler') {
  configure perms.setPermissions(this, ['dev'], deployEnvironment)
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
      scriptPath('pipelines/eks/k8s-recycler.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'ECR-CreateApplyPolicy') {
  configure perms.setPermissions(this, ['dev', 'cs', 'ops'], deployEnvironment)
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
  }
}


pipelineJob(jobPrefix + 'ECR-Mirroring') {
  configure perms.setPermissions(this, ['dev', 'ops'], deployEnvironment)
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
      scriptPath('pipelines/ecr/ecr_mirroring.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'CreateReadOnlyRole') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/shared-kubernetes.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('jenkins/create-readonly-role.groovy')
    }
  }
}
