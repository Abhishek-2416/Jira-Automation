import groovy.json.JsonBuilder
import hudson.AbortException

/**
 * Build the JSON body for a new Jira issue.
 */
def createJiraPayload(String summary,
                      String description,
                      String projectKey,
                      String issueType) {
  def payload = [
    fields: [
      project    : [ key: projectKey ],
      summary    : summary,
      description: description,
      issuetype  : [ name: issueType ]
    ]
  ]
  new JsonBuilder(payload).toPrettyString()
}

/**
 * Wrap httpRequest so we capture 400+ bodies instead of aborting immediately.
 */
def call(String method,
         String url,
         String authId = 'jira-api-token-id',
         String body   = null) {
  // allow all status codes through
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
    // dump the error payload
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
                     String issueType) {
  def jiraUrl = "https://abhishekalimchandani1624.atlassian.net/rest/api/2/issue"
  def body    = createJiraPayload(summary, description, projectKey, issueType)
  def resp    = call('POST', jiraUrl, env.JIRA_AUTH, body)

  echo "✅ Jira ticket created: ${resp.content}"
}

// IMPORTANT: make sure load() returns this script instance
return this
