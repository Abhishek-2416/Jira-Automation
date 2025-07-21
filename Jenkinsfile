pipeline {
  agent any

  // if you need to pull in credentials, define them in Jenkins as 'jira-api-token-id'
  environment {
    JIRA_AUTH = 'jira-api-token-id'
  }

  parameters {
    string(name: 'Summary',    defaultValue: '', description: 'Jira issue summary')
    text(name: 'Description',  defaultValue: '', description: 'Jira issue description')
    string(name: 'PROJECT',    defaultValue: '', description: 'Jira project key (e.g. STPE)')
    string(name: 'Issue_Type', defaultValue: 'Task', description: 'Issue type (e.g. Bug, Task)')
    string(name: 'Assignee',   defaultValue: '', description: 'Username to assign')
    string(name: 'Reporter',   defaultValue: '', description: 'Username of reporter')
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Create Jira Ticket') {
      steps {
        script {
          // load your helper script
          def jiraUtil = load "${env.WORKSPACE}/scripts/jiraUtil.groovy"

          // call the function exported by jiraUtil
          jiraUtil.createJiraTicket(
            params.Summary,
            params.Description,
            params.PROJECT,
            params.Issue_Type,
            params.Assignee,
            params.Reporter
          )
        }
      }
    }
  }

  post {
    failure {
      echo "❌ Jira ticket creation failed."
    }
    success {
      echo "✅ Pipeline completed."
    }
  }
}
