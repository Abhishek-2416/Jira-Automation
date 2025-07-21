pipeline {
  agent any

  environment {
    // Jenkins credential ID for a user/API-token with rights to create issues
    JIRA_AUTH = 'jira-api-token-id'
  }

  parameters {
    string(name: 'Summary',     defaultValue: '',       description: 'Jira issue summary')
    text(  name: 'Description', defaultValue: '',       description: 'Jira issue description')
    // ← set a sane default here so you don’t get a blank key on replay
    string(name: 'PROJECT',     defaultValue: 'TEST',   description: 'Jira project key (e.g. STPE)')
    string(name: 'Issue_Type',  defaultValue: 'Task',   description: 'Issue type (e.g. Bug, Task)')
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Create Jira Ticket') {
      steps {
        script {
          def jiraUtil = load 'vars/JiraUtil.groovy'

          // sanity-check payload builder:
          def sample = jiraUtil.createJiraPayload(
            '🔧 TEST Summary',
            '🔧 TEST Description',
            params.PROJECT,
            params.Issue_Type
          )
          echo "✔️ Payload OK: ${sample.take(80)}…"

          // actual ticket creation
          jiraUtil.createJiraTicket(
            params.Summary,
            params.Description,
            params.PROJECT,
            params.Issue_Type
          )
        }
      }
    }
  }

  post {
    success { echo "✅ Pipeline completed successfully." }
    failure { echo "❌ Pipeline failed; check the JSON error above." }
  }
}
