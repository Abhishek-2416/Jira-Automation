import groovy.json.JsonSlurper

def call(String method,
         String url,
         String authId = (env.BB_AUTH ?: 'bitbucket-creds'),
         String body   = null) {
  def resp = httpRequest(
    ignoreSslErrors        : true,
    authentication         : authId,
    acceptType             : 'APPLICATION_JSON',
    contentType            : 'APPLICATION_JSON',
    httpMode               : method,
    requestBody            : body,
    url                    : url,
    validResponseCodes     : '100:599',
    consoleLogResponseBody : false
  )
  if ((resp.status as int) >= 400) {
    error "Bitbucket call failed: HTTP ${resp.status}\n${resp.content}"
  }
  return resp.content
}

/** Get latest 3 commits + committer names for each repo in a project. */
Map getLatestCommitters(String projectKey) {
  def base    = (env.BB_BASE_URL ?: 'https://bitbucket.example.com')
  def slurper = new JsonSlurper()
  def result  = [:]

  // repos (first page)
  def reposJson   = slurper.parseText(call('GET', "${base}/rest/api/1.0/projects/${projectKey}/repos?limit=1000"))
  def reposList   = reposJson['values'] ?: []

  reposList.each { repo ->
    String slug = repo['slug']?.toString()
    if (!slug) return

    def commitsJson = slurper.parseText(call('GET', "${base}/rest/api/1.0/projects/${projectKey}/repos/${slug}/commits?limit=3"))
    def commits     = commitsJson['values'] ?: []

    def names = commits.collect { c ->
        (c['author']?.get('displayName') ?: c['committer']?.get('displayName') ?: 'Unknown').toString()
    }


    result[slug] = names
  }
  return result
}

return this
