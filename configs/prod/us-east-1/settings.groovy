
// Creates Jenkins managed config files consumed by pipeline jobs.
// Values below are placeholders. Replace them with your organisation's real
// (non-secret) settings. Never commit real secrets here; reference Jenkins
// credential IDs instead.
configFiles {
    customConfig {
        id('pipeline_settings')
        name('pipeline_settings')
        comment('Shared pipeline settings for the prod environment')
        content('''{
  "AWSAccountName": "skyline-prod",
  "AWSAccountNumber": "333333333333",
  "AWSRegion": "us-east-1",
  "AWS_DEFAULT_REGION": "us-east-1",
  "GeoRoute53RestrictedURL": "restricted-000000000.us-east-1.elb.amazonaws.com",
  "HostedZoneId": "Z0000000000000PROJECTA",
  "ChefClientKey": "chef_jenkins_user_key",
  "ChefDataBagSecret": "chef-databag-secret",
  "ChefServer": "https://chef.prod.nimbus.io",
  "ChefServerEnvironment": "prod-01",
  "DRAWSRegion": "us-west-2",
  "GitROCredsId": "git-readonly",
  "GitRWCredsId": "git-readwrite",
  "JiraCredsId": "JIRA_API_TOKEN",
  "Project": "skyline",
  "SlackChannel": "ci-notifications",
  "WorkerGroups" : {
    "standard": {
      "labels": "generic",
      "sgFilter" : "prod-01-jenkins-instance",
      "instanceType" : "m5.xlarge"
    }
  },
  "WorkerTags" : {
    "Owner": "saaseng"
  }
}''')}
}
