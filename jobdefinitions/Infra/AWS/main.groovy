// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

// Map of DNS domains available per environment. Replace with your own.
def dnsDomains = [
  'dev': [
    'dev.nimbus.io',
  ],
  'staging': [
    'staging.nimbus.io',
  ],
  'prod': [
    'nimbus.io',
    'prod.nimbus.io',
  ]
]

pipelineJob(jobPrefix + 'UpsertRoute53Entry') {
  configure perms.setPermissions(this, ['ops', 'dev'], deployEnvironment)
  parameters {
    choiceParam('myDomain', dnsDomains[awsAccountName] ?: ['nimbus.io'], 'Domain for the entry')
    choiceParam('myType', ['A', 'CNAME'], 'A or CNAME')
    stringParam('myDnsName', '', 'Record Name')
    stringParam('myTarget', '', 'Target IP or CNAME')
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
      scriptPath('pipelines/route53/upsertRoute53Entry.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'AWSEBSCleanUp') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    booleanParam('dryRun', false, 'DryRun')
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
      scriptPath('pipelines/ebs/aws-ebs-cleanup.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'CreateEcrRepository') {
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
      scriptPath('pipelines/ecr/create_ecr.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'SyncBootstrapScripts') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/bootstrap_scripts.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipeline.groovy')
    }
    triggers {
      cron('H H/4 * * *')
    }
  }
}

pipelineJob(jobPrefix + 'TagResources') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    booleanParam('tag_ec2', true, "Tag EC2 Instances")
    booleanParam('tag_eks', true, "Tag EKS Clusters and Instances")
    booleanParam('tag_elbs', true, "Tag Load Balancers")
    booleanParam('tag_s3_buckets', true, "Tag S3 Buckets")
    booleanParam('tag_security_groups', true, "Tag Security Groups")
    booleanParam('tag_vpcs', true, "Tag VPCs")
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
      scriptPath('pipelines/tagging/tag_resources.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'Build-JenkinsAMI') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    booleanParam('dryRun', false, 'DryRun')
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
      scriptPath('pipelines/packer/jenkins-master.groovy')
    }
  }
}
