import groovy.json.JsonBuilder
import hudson.AbortException
import groovy.json.JsonSlurper

/**
 * Build the JSON body for a new Jira issue.
 */
def createJiraPayload(String summary,
                      String description,
                      String projectKey,
                      String issueType,
                      String reporterUsername,
                      String assigneeUsername,
                      String componentName,
                      String epicLinkKey) {
  def payload = [
    fields: [
      project    : [ key: projectKey ],
      summary    : summary,
      description: description,
      issuetype  : [ name: issueType ],
      reporter   : [ name: reporterUsername ],
      assignee   : [ name: assigneeUsername ],
      components : [[ name: componentName ]],
      customfield_10006: epicLinkKey  // Epic Link
    ]
  ]

  return new JsonBuilder(payload).toPrettyString()
}

/**
 * Wrap httpRequest so we capture 400+ bodies instead of aborting immediately.
 */
def call(String method,
         String url,
         String authId = 'jira-creds-abhishek',
         String body   = null) {
  def resp = httpRequest(
    ignoreSslErrors     : true,
    authentication      : authId,
    acceptType          : 'APPLICATION_JSON',
    contentType         : 'APPLICATION_JSON',
    httpMode            : method,
    requestBody         : body,
    url                 : url,
    validResponseCodes  : '100:599'
  )

  if (resp.status.toInteger() >= 400) {
    echo ">>> Jira returned HTTP ${resp.status}\n${resp.content}"
    error "Aborting: Jira call failed with status ${resp.status}"
  }
  return resp
}

/**
 * Create a Jira ticket via REST and echo the response.
 */
def createJiraTicket(String summary,
                     String description,
                     String projectKey,
                     String issueType,
                     String reporterUsername,
                     String assigneeUsername,
                     String componentName,
                     String epicLinkKey) {
  def jiraUrl = "https://jira.davita.com/rest/api/2/issue"
  def body    = createJiraPayload(summary, description, projectKey, issueType, reporterUsername, assigneeUsername, componentName, epicLinkKey)
  def resp    = call('POST', jiraUrl, env.JIRA_AUTH, body)

  def json = new JsonSlurper().parseText(resp.content)
  def issueKey = json.key
  def issueUrl = "https://jira.davita.com/browse/${issueKey}"

  echo "Jira ticket created: ${issueKey}"
  echo "View it at: ${issueUrl}"

  // Add Jira link to Jenkins UI
  try {
    currentBuild.description = "${issueUrl}"
  } catch (MissingPropertyException ignored) {
    echo "Not running inside Jenkins or currentBuild not available."
  }

  // Log time if TIME_SPENT is passed
  if (env.TIME_SPENT?.trim()) {
    def comment = env.WORKLOG_COMMENT ?: "Logged from Jenkins"
    logWorkTime(issueKey, env.TIME_SPENT.trim(), comment)
  }

  return issueUrl
}

/**
 * Log time against a Jira issue.
 */
def logWorkTime(String issueKey, String timeSpent, String comment = "") {
  def worklogUrl = "https://jira.com/rest/api/2/issue/${issueKey}/worklog"
  def worklogBody = new JsonBuilder([
    comment   : comment,
    timeSpent : timeSpent
  ]).toPrettyString()

  def resp = call('POST', worklogUrl, env.JIRA_AUTH, worklogBody)
  echo "Logged ${timeSpent} to ${issueKey}"
}

return this