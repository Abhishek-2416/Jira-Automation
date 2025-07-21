pipeline {
  agent any

  environment {
    // your Jenkins credential for Jira API token
    JIRA_AUTH = 'jira-api-token-id'
  }

  parameters {
    string(name: 'Summary',     defaultValue: '',      description: 'Jira issue summary')
    text(  name: 'Description', defaultValue: '',      description: 'Jira issue description')
    string(name: 'PROJECT',     defaultValue: 'STPE',  description: 'Jira project key (e.g. STPE)')
    string(name: 'Issue_Type',  defaultValue: 'Task',  description: 'Issue type (e.g. Bug, Task)')
    string(name: 'Assignee',    defaultValue: '',      description: 'Username to assign (leave blank to skip)')
    string(name: 'Reporter',    defaultValue: '',      description: 'Username of reporter (leave blank to use default)')
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Create Jira Ticket') {
      steps {
        script {
          def jiraUtil = load 'vars/JiraUtil.groovy'

          // sanity-check payload builder
          def sample = jiraUtil.createJiraPayload(
            '🔧 TEST Summary',
            '🔧 TEST Description',
            params.PROJECT,
            params.Issue_Type,
            params.Assignee,
            params.Reporter
          )
          echo "✔️ Payload OK: ${sample.take(100)}…"

          // actual ticket creation
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
    success { echo "✅ Pipeline completed successfully." }
    failure { echo "❌ Pipeline failed; check logs above." }
  }
}
