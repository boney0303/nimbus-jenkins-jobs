// using the shared library repo within the jobDSL
// and pass $WORKSPACE@libs as 'additionalClasspath'
import com.nimbus.JobPermissions

def project = 'skyline'
def perms = new JobPermissions()

pipelineJob(jobPrefix + 'SelfServeEKS') {
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
      scriptPath('pipelines/terraform/provision_cluster.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'GenericTerraform') {
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
      scriptPath('pipelines/terraform/generic_terraform.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'SharedEksService') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('TERRA_PROVISIONING_IMAGE', provisioningImages,
      'WARNING !!! Setting which Terraform/Terragrunt version will be used, use default option unless require otherwise. Use the default unless a specific version is required'
    )
    stringParam( "CLUSTER_NAME", "EKS Cluster" )
    choiceParam('TASK', ['plan-all', 'plan', 'apply-all', 'apply', 'destroy-all', 'destroy'], 'Select which Terragrunt task to perform')
    choiceParam('TERRAFILE_ENV', ['prd', 'stage', 'staging', 'perf'], 'Select which Terrafile Environment to use')
    stringParam('SKIP_REGION_VALIDATION', "false", 'Please pass skip skip_region_validation (required for certain regions)')
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
      scriptPath('services/shared_eks_service/terragrunt_shared_eks_service.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'OpaSharedEksService') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('TERRA_PROVISIONING_IMAGE', provisioningImages,
      'WARNING !!! Setting which Terraform/Terragrunt version will be used, use default option unless require otherwise. Use the default unless a specific version is required'
    )
    stringParam( "CLUSTER_NAME", "EKS Cluster" )
    choiceParam('TASK', ['plan-all', 'plan', 'apply-all', 'apply', 'destroy-all', 'destroy'], 'Select which Terragrunt task to perform')
    choiceParam('TERRAFILE_ENV', ['prd', 'stage', 'staging', 'perf'], 'Select which Terrafile Environment to use')
    stringParam('SKIP_REGION_VALIDATION', "false", 'Please pass skip skip_region_validation (required for certain regions)')
    booleanParam('IGNORE_DEPENDENCY_ERRORS', false, "'*-all' commands continue processing components even if a dependency fails")
    booleanParam('force', false, 'Flag for force run (will skip approval stage)')
    booleanParam('skip_opa', false, 'Flag to skip all opa related steps')
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
      scriptPath('services/shared_eks_service/opa_terragrunt_shared_eks_service.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'SharedRdsService') {
  configure perms.setPermissions(this, ['ops' ], deployEnvironment)
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
      scriptPath('services/shared_rds_service/terragrunt_shared_rds_service.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'SharedCacheService') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
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
      scriptPath('services/shared_cache_service/terragrunt_shared_cache_service.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'SharedS3Service') {
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
      scriptPath('services/shared_s3_service/terragrunt_shared_s3_service_tf12.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'BumpModulesVersionTag') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  definition {
    logRotator {
      numToKeep(5)
    }
    cpsScm {
      scm {
        git{
          remote{
            url("${sshGitUrl}/${project}/terraform_modules.git")
            credentials("${gitCreds}")
            branch('*/master')
          }
        }
      }
      scriptPath('pipelines/update_tag.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'VpcEndpoint') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
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
      scriptPath('services/vpc_endpoint/terragrunt_vpc_endpoint_tf12.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'AmakKafkaService') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
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
      scriptPath('services/amak_kafka_service/terragrunt_amak_kafka_service.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'SharedEksCloudWatchELBAlarm') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
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
      scriptPath('services/shared_eks_cloudwatch/shared_elb_alarm/shared_eks_elb_alarm.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'Service-BastionHost') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('TERRA_PROVISIONING_IMAGE', provisioningImages,
      'WARNING !!! Setting which Terraform and Terragrunt version will be used, use default option unless require otherwise. Use the default unless a specific version is required'
    )
    choiceParam('TASK', ['plan', 'apply', 'output', 'destroy'], 'Select which Terragrunt task to perform')
    choiceParam('TERRAFILE_ENV', ['prd', 'stage', 'staging', 'perf'], 'Select which Terrafile Environment to use')
    booleanParam('SKIP_REGION_VALIDATION', false, 'Please pass skip skip_region_validation (required for certain regions)')
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
      scriptPath('services/bastion_host/pipeline.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'Service-ChefHost') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('TERRA_PROVISIONING_IMAGE', provisioningImages,
      'WARNING !!! Setting which Terraform and Terragrunt version will be used, use default option unless require otherwise. Use the default unless a specific version is required'
    )
    choiceParam('TASK', ['plan-all', 'apply-all', 'output-all', 'destroy-all'], 'Select which Terragrunt task to perform')
    choiceParam('TERRAFILE_ENV', ['prd', 'stage', 'staging', 'perf'], 'Select which Terrafile Environment to use')
    booleanParam('SKIP_REGION_VALIDATION', false, 'Please pass skip skip_region_validation (required for certain regions)')
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
      scriptPath('services/chef_host/pipeline.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'Service-JenkinsHost') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
  parameters {
    choiceParam('TERRA_PROVISIONING_IMAGE', provisioningImages,
      'WARNING !!! Setting which Terraform and Terragrunt version will be used, use default option unless require otherwise. Use the default unless a specific version is required'
    )
    choiceParam('TASK', ['plan-all', 'apply-all', 'output-all', 'destroy-all'], 'Select which Terragrunt task to perform')
    choiceParam('TERRAFILE_ENV', ['prd', 'stage', 'staging', 'perf'], 'Select which Terrafile Environment to use')
    booleanParam('SKIP_REGION_VALIDATION', false, 'Please pass skip skip_region_validation (required for certain regions)')
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
      scriptPath('services/jenkins_host/pipeline.groovy')
    }
  }
}

pipelineJob(jobPrefix + 'TerragruntLiveTerraform') {
  configure perms.setPermissions(this, ['ops'], deployEnvironment)
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
      scriptPath('scripts/jenkins_pipeline/terragrunt_live_terraform.groovy')
    }
  }
}
