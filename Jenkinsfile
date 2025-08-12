pipeline {
  agent {
    kubernetes {
      inheritFrom 'alpine'
    }
  }

  environment {
    JIRA_AUTH = 'jira-creds-abhishek'
  }

  parameters {
    string(
      name: 'Summary',
      defaultValue: '',
      description: 'Jira issue summary'
    )
    text(
      name: 'Description',
      defaultValue: '''Root:

Resolution:

category : 

Master:
Project:
Repository:

Kubernetes Node:
Build Node: N/A
Developer Name:
Owner: Abhishek Alimchandani
Date: 07/29/2025
Release: N/A

Comments:''',
      description: 'Jira issue description'
    )
    string(
      name: 'PROJECT',
      defaultValue: 'STPE',
      description: 'Jira project key (e.g. STPE)'
    )
    string(
      name: 'Issue_Type',
      defaultValue: 'Story',
      description: 'Issue type (e.g. Bug, Task)'
    )
    string(
      name: 'Reporter',
      defaultValue: 'TAMREID',
      description: 'Reporter Jira username'
    )
    choice(
      name: 'Assignee',
      choices: ['aalimchandani','KVISHWAKANTH','SKARRE','VBANDA','RPOLENENI'],
      description: 'Choose Assignee username'
    )
    choice(
      name: 'Component',
      choices: ['Jenkins'],
      description: 'Select Jira Component'
    )
    choice(
      name: 'Epic_Link',
      choices: [
        'STPE-3136 : Jenkins Stabilization & Performance Enhancements'
      ],
      description: 'Select Epic to associate with this ticket'
    )
    string(
      name: 'TIME_SPENT',
      defaultValue: '',
      description: 'Time spent on this issue (e.g. 1h, 30m, 2h 15m)'
    )
    string(
      name: 'WORKLOG_COMMENT',
      defaultValue: '',
      description: 'Comment for the time log entry (optional)'
    )
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
          def jiraUtil = load 'vars/JiraUtil.groovy'

          def epicKey = params.Epic_Link.split(':')[0].trim()

          def sample = jiraUtil.createJiraPayload(
            'TEST Summary',
            'TEST Description',
            params.PROJECT,
            params.Issue_Type,
            params.Reporter,
            params.Assignee,
            params.Component,
            epicKey
          )
          echo "Payload OK: ${sample.take(80)}..."

          jiraUtil.createJiraTicket(
            params.Summary,
            params.Description,
            params.PROJECT,
            params.Issue_Type,
            params.Reporter,
            params.Assignee,
            params.Component,
            epicKey
          )
        }
      }
    }
  }

  post {
    success {
      echo "Pipeline completed successfully."
    }
    failure {
      echo "Pipeline failed; check the JSON error above."
    }
  }
}