@Library('saas') _

pipeline {
  agent any
  triggers{
    cron('15 12 * * *')
  }
  options {
    timestamps()
    ansiColor('xterm')
  }
  environment {
    AWSAccountName = getAccountName(getAWSAccountNumber())
    AWSAccountNumber = getAWSAccountNumber()
    AWSRegion = getAWSRegion()
    DeployAccount = getAccountName(getAWSAccountNumber())
    DeployEnvironment = getDeployEnvironment()
    DeployRegion = getDeployRegion()
    RegionDefaultHostName = getRegionDefaultHostName()
    CIAWSAccessKeyID = getCIAWSAccessKeyID()
    CIAWSSecretAccessKey = getCIAWSSecretAccessKey()
    ChefOrg = 'skyline'
    CurrentRestrictedURL = getGeoRoute53RestrictedURL()
    GitCreds = 'ci/jenkins-private-key'
    Githost = 'git.nimbus.io'
    HostedZoneId = getHostedZoneId()
    HttpGitUrl = determineGitHost("http")
    SshGitUrl = determineGitHost()
  }
  stages {
    stage ("Install Knife Plugins") {
      steps{
        script{

          // ensure spork plugin is installed
          spork_installed = sh(
            script: 'knife spork info',
            returnStatus: true
          )
          if ( spork_installed == 10 ) {
            echo "spork is NOT installed(spork_installed = ${spork_installed})...installing"
            spork_installed_result = sh(
              script: '/opt/chef-workstation/bin/chef env --chef-license accept-silent && chef gem install knife-spork',
              returnStatus: true
            )
          }
        }
      }
    }
    stage('Setup Environment'){
      steps {
        script{
          checkout scm

          // checkout copy of saaslib so that permissions are availble for jobs
          checkout changelog: false,
            poll: false,
            scm: [$class: 'GitSCM',
            branches: [[name: '*/master']],
            extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'saaslib']],
            userRemoteConfigs: [[credentialsId: "${gitCreds}", url: "${env.SshGitUrl}/saaseng/nimbus-jenkins-shared-libraries.git"]]]
        }
      }
    }

    stage ("Import data from job files") {
      steps{
        script{

          // get list of job definitions to include on this host
          try {
            dir("envs/${env.DeployAccount}"){
              jobSpecs = readFile("jobs.txt").split("\n")
              // jobSpecs = ['Chef/**']  # used for testing
            }
          } catch(e) {
            println("no jobs.txt found in: envs/${env.DeployAccount}")
            deleteDir()
            throw e; // rethrow so the build is considered failed
          }
          if ( jobSpecs[0].size() == 0 ) {
            currentBuild.result = "UNSTABLE"
            error("No jobs definitions found to create")
          }
        }
      }
    }

    stage("Setup Views") {
      steps{
        script{
          // top level views - extracted from filenames' top directory within jobDefinitions directory
          List<String> viewsToUse = []

          // use the leftmost portion of parent path for each top-level View - ex. 'chef/server' -> 'chef'
          jobSpecs.eachWithIndex{ item ->
            println(item)
            if ( item.contains("/") ) {
              viewsToUse += item.tokenize('/')[0]
            }
            else {
              viewsToUse += item
            }
          }

          // add other views
          viewsToUse += "Seed"

          // use jobDSL to create each View
          viewsToUse.unique().each{ thisView ->
            jobDsl scriptText: '''
                categorizedJobsView(thisViewName) {
                    jobs {
                        regex(thisViewRegex)
                    }
                    categorizationCriteria {
                        regexGroupingRule(thisCategoryRegex, '$1' )
                    }
                    columns {
                        status()
                        weather()
                        categorizedJob()
                        lastSuccess()
                        lastFailure()
                        lastDuration()
                    }
                }
            ''',
            removedJobAction: 'IGNORE',
            removedViewAction: 'IGNORE',
            additionalParameters: [
              thisViewName: thisView,
              thisViewRegex: "^${thisView}.*",
              thisCategoryRegex: "^${thisView}-([a-zA-Z09]+)-(.*)",
            ]
          }
        }
      }
    }

    stage('Setup Jobs'){
      steps{
        script{
          // jobSpecs - contains the imported file contents - each line can be in Ant style glob pattern
          // https://ant.apache.org/manual/dirtasks.html#patterns

          dir('jobdefinitions') {
            // checkout the Shared Library for manual inclusion

            eksClusterList = ['--select--'] + getEKSClusters()

            jobSpecs.each{ thisFileSpec ->

              def theseFiles = findFiles(glob: "${thisFileSpec}/*.groovy")
              // def theseFiles = findFiles(glob: "Chef/Server/*.groovy")  # used for testing
              theseFiles.each{ thisFile ->
                // create var, jobPrefix to pass to each jobDSL script for View Regex Usage
                def thisJobPrefix = thisFile.path.split("/")[0] + "-"

                // for Categories, add to end of jobPrefix for View Regex Usage
                if (thisFile.path.split("/").size() > 2) {
                  thisJobPrefix += thisFile.path.split("/")[1] + "-"
                }

                // use JobDSL to create jobs
                def job = jobDsl targets: thisFile.path,
                  removedJobAction: 'IGNORE',
                  additionalParameters: [
                    awsAccountName: env.AWSAccountName,
                    awsAccountNumber: env.AWSAccountNumber,
                    awsRegion: env.AWSRegion,
                    awsRegionDefaultHostName: env.RegionDefaultHostName,
                    deployEnvironment: env.DeployEnvironment,
                    geoRoute53RestrictedURL: env.CurrentRestrictedURL,
                    hostedZoneId: env.HostedZoneId,
                    CI_AWS_ACCESS_KEY_ID: env.CIAWSAccessKeyID,
                    CI_AWS_SECRET_ACCESS_KEY: env.CIAWSSecretAccessKey,
                    cookbooks: getCookbookList(),
                    eksClusterList: eksClusterList,
                    gitCreds: "${GitCreds}",
                    httpGitUrl: "${env.HttpGitUrl}",
                    jobPrefix: thisJobPrefix,
                    provisioningImages: getProvisioningImages(),
                    sshGitUrl: "${env.SshGitUrl}"
                  ],
                  additionalClasspath: "$WORKSPACE/saaslib/src"
              }
            }
          }
        }
      }
    }

    stage ("Setup Configs") {
      steps{
        script{
          // load configs based on Environment and Region
          jobDsl targets: ["configs/${env.DeployAccount}/${env.DeployRegion}/*.groovy"].join('\n'),
            removedJobAction: 'DELETE',
            removedViewAction: 'DELETE'
        }
      }
    }

    stage ("Setup Access") {
      steps{
        script{
          // update permissions to seed job to allow Authenticated Users to run
          Jenkins.instance.getItem("Seed").removeProperty(hudson.security.AuthorizationMatrixProperty)
          Jenkins.instance.getItem("Seed").addProperty(new hudson.security.AuthorizationMatrixProperty())
          Jenkins.instance.getItem("Seed").getProperty(hudson.security.AuthorizationMatrixProperty).add("GROUP:hudson.model.Item.Build:authenticated")
          Jenkins.instance.getItem("Seed").getProperty(hudson.security.AuthorizationMatrixProperty).add("GROUP:hudson.model.Item.Read:authenticated")
        }
      }
    }

  }
  post {
    // cleanup {
    //   cleanUp()
    // }
    failure {
        slackSend channel: "#ci-notifications", color: 'danger', message: "${env.JOB_NAME} FAILED for ${env.BUILD_URL}", teamDomain: 'nimbus', tokenCredentialId: 'slack-integration-token'
    }
    success {
        slackSend channel: "#ci-notifications", color: 'good', message: "${env.JOB_NAME} SUCCEEDED for ${env.BUILD_URL}", teamDomain: 'nimbus', tokenCredentialId: 'slack-integration-token'
    }
  }
}

