private String getDisplayName(String username) {
  def url  = "https://jira.com/rest/api/2/user?username=${URLEncoder.encode(username,'UTF-8')}"
  def resp = call('GET', url, env.JIRA_AUTH)
  def user = new JsonSlurper().parseText(resp.content)

  // debug
  echo "Lookup user='${username}' status=${resp.status} displayName='${user?.displayName}'"

  return user?.displayName ?: username
}
