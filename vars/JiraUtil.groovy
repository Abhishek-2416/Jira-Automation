import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import java.net.URLEncoder

// Step 1: Function to build Jira issue payload
def createJiraPayload(String summary, String description, String projectKey, String issueType) {
    def payload = [
        fields: [
            project     : [ key: projectKey ],
            summary     : summary,
            description : description,
            issuetype   : [ name: issueType ]
            //assignee    : [ name: assignee ],
            //reporter    : [ name: reporter ]
            // Removed: customfield_10008 (Epic Link)
        ]
    ]
    return new JsonBuilder(payload).toPrettyString()
}

// Step 2: HTTP request wrapper
def call(httpRequestType, url, authentication = 'jira-api-token-id', body = null) {
    try {
        def response = httpRequest(
            ignoreSslErrors: true,
            authentication: authentication,
            acceptType: 'APPLICATION_JSON',
            contentType: 'APPLICATION_JSON',
            httpMode: httpRequestType,
            requestBody: body,
            url: url
        )
        return response
    } catch (hudson.AbortException err) {
         // print the HTTP status and the raw body Jira sent back
        echo ">>> Jira HTTP error: ${err.getMessage()}"
        if (err.response?.content) {
        echo ">>> Jira payload error:\n${err.response.content}"
        }
        error "Aborting: Jira returned ${err.response?.status}"
    }
}

// Step 3: Send the Jira ticket
def createJiraTicket(String summary, String description, String projectKey, String issueType, String assignee, String reporter) {
    def jiraUrl = "https://abhishekalimchandani1624.atlassian.net/rest/api/2/issue"
    def payload = createJiraPayload(summary, description, projectKey, issueType, assignee, reporter)

    def response = call('POST', jiraUrl, 'jira-api-token-id', payload)

    echo "JIRA ticket created successfully: ${response.content}"
}

// Example usage in Jenkins Pipeline
node {
    stage('Create Jira Ticket') {
        createJiraTicket(
            params.Summary,
            params.Description,
            params.PROJECT,      // This should be the Jira project key (e.g., "STPE")
            params.Issue_Type
            // params.Assignee,
            // params.Reporter
        )
    }
}
