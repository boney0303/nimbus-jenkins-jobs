
// Creates Jenkins managed config files consumed by pipeline jobs.
// Values below are placeholders. Replace them with your organisation's real
// (non-secret) settings. Never commit real secrets here; reference Jenkins
// credential IDs instead.
configFiles {
    customConfig {
        id('pipeline_settings')
        name('pipeline_settings')
        comment('Shared pipeline settings for the dev environment')
        content('''{
  "AWSAccountName": "skyline-dev",
  "AWSAccountNumber": "111111111111",
  "AWSRegion": "us-east-1",
  "AWS_DEFAULT_REGION": "us-east-1",
  "ChefClientKey": "chef_jenkins_user_key",
  "ChefDataBagSecret": "chef-databag-secret",
  "ChefServer": "https://chef.dev.nimbus.io",
  "ChefServerEnvironment": "dev-01",
  "DRAWSRegion": "us-west-2",
  "Project": "skyline",
  "SlackChannel": "ci-notifications",
  "WorkerGroups" : {
    "standard": {
      "labels": "generic",
      "sgFilter" : "dev-01-jenkins-instance",
      "instanceType" : "m5.large"
    }
  },
  "WorkerTags" : {
    "Owner": "saaseng"
  }
}''')}
}
