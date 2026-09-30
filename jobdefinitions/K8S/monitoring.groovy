def project = 'skyline'

pipelineJob(jobPrefix + 'Monitoring-VictoriaMetricsService') {
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
      scriptPath('services/victoria_metrics_cluster_service/terragrunt_victoria_metrics_cluster_service.groovy')
    }
  }
}

