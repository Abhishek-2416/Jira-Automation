import groovy.json.JsonBuilder
import hudson.AbortException

/**
 * Build the JSON body for a new Jira issue, adding assignee & reporter if provided.
 */
def createJiraPayload(String summary,
                      String description,
                      String projectKey,
                      String issueType,
                      String assignee,
                      String reporter) {

  def fields = [
    project    : [ key: projectKey ],
    summary    : summary,
    description: description,
    issuetype  : [ name: issueType ]
  ]

  // only include assignee if non-blank
  if (assignee?.trim()) {
    fields.assignee = [ name: assignee.trim() ]
  }
  // only include reporter if non-blank
  if (reporter?.trim()) {
    fields.reporter = [ name: reporter.trim() ]
  }

  def payload = [ fields: fields ]
  return new JsonBuilder(payload).toPrettyString()
}

/**
 * Wrap httpRequest so we capture 400+ bodies instead of aborting immediately.
 */
def call(String method,
         String url,
         String authId = 'jira-api-token-id',
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
                     String assignee,
                     String reporter) {
  def jiraUrl = "https://abhishekalimchandani1624.atlassian.net/rest/api/2/issue"
  def body    = createJiraPayload(summary, description, projectKey, issueType, assignee, reporter)
  def resp    = call('POST', jiraUrl, env.JIRA_AUTH, body)
  echo "✅ Jira ticket created: ${resp.content}"
}

// ensure load() returns this script instance
return this
