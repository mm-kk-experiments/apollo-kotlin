#!/usr/bin/env kotlin

@file:DependsOn("com.squareup.okio:okio:3.2.0")
@file:DependsOn("com.google.cloud:google-cloud-storage:2.8.1")
@file:DependsOn("net.mbonnin.bare-graphql:bare-graphql:0.0.2")

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import net.mbonnin.bare.graphql.asList
import net.mbonnin.bare.graphql.asMap
import net.mbonnin.bare.graphql.cast
import net.mbonnin.bare.graphql.graphQL
import okio.Buffer
import okio.buffer
import okio.source
import java.io.File
import java.util.Date


/**
 * This script expects:
 *
 * - `gcloud` in the path
 * - A Google Cloud Project with "Google Cloud Testing API" and "Cloud Tool Results API" enabled
 * - GOOGLE_SERVICES_JSON env variable: a service account json with Owner permissions (could certainly be less but haven't found a way yet)
 * - GITHUB_REPOSITORY env variable: the repository to create the issue into in the form `owner/name`
 * - GITHUB_TOKEN env variable: a GitHub token that can create issues on the repo
 *
 * This script must be run from the repo root
 */

/**
 * Executes the given command and returns stdout as a String
 * Throws if the exit code is not 0
 */
fun executeCommand(vararg command: String): CommandResult {
  val process = ProcessBuilder()
      .command(*command)
      .redirectError(ProcessBuilder.Redirect.INHERIT)
      .start()

  val output = process.inputStream.source().buffer().readUtf8()

  val exitCode = process.waitFor()
  return CommandResult(exitCode, output)
}

class CommandResult(val code: Int, val output: String)


/**
 * Authenticates the local 'gcloud' and a new [Storage] instance
 * Throws on error
 */
fun authenticate(): Storage {
  val googleServicesJson = System.getenv("GOOGLE_SERVICES_JSON") ?: error("GOOGLE_SERVICES_JSON is missing")

  val tmpFile: File = File.createTempFile("google", "json")
  val credentials: GoogleCredentials
  val storage: Storage
  try {
    tmpFile.writeText(googleServicesJson)
    val result = executeCommand("gcloud", "auth", "activate-service-account", "--key-file=${tmpFile.absoluteFile}")
    if (result.code != 0) {
      error("Cannot authenticate: ${result.output}")
    }
    credentials = GoogleCredentials.fromStream(tmpFile.inputStream())
        .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
    storage = StorageOptions.newBuilder().setCredentials(credentials).build().service
  } finally {
    tmpFile.delete()
  }
  return storage
}

fun runTest(): String {
  val result = executeCommand(
      "gcloud",
      "firebase",
      "test",
      "android",
      "run",
      "--type=instrumentation",
      "--device",
      "model=redfin,locale=en,orientation=portrait",
      "--test=benchmark/build/outputs/apk/androidTest/release/benchmark-release-androidTest.apk",
      "--app=benchmark/build/outputs/apk/release/benchmark-release.apk"
  )

  check(result.code == 0) {
    "Test failed: ${result.output}"
  }

  println(result.output)
  return result.output
}

/**
 * Parses the 'gcloud firebase test android run' output and download the instrumentation
 * results from Google Cloud Storage
 *
 * @return the [TestResult]
 */
fun getTestResult(output: String, storage: Storage): TestResult {
  val gsUrl = output.lines().mapNotNull {
    val matchResult = Regex(".*\\[https://console.developers.google.com/storage/browser/([^\\]]*).*").matchEntire(it)
    matchResult?.groupValues?.get(1)
  }.single()
      .split("/")
      .filter { it.isNotBlank() }
  val bucket = gsUrl[0]
  val blobName = "${gsUrl[1]}/redfin-30-en-portrait/instrumentation.results"

  println("Downloading $bucket/$blobName")
  val buffer = Buffer()
  storage.get(bucket, blobName).downloadTo(buffer.outputStream())

  val cases = buffer.readUtf8().lines().mapNotNull {
    val matchResult = Regex(".*android.studio.display.benchmark= *([0-9,]*) *ns *([0-9]*) *allocs *([^ ]*)").matchEntire(it)
    matchResult?.groupValues
  }.map {
    Case(it.get(3), it.get(1).replace(",", "").toLong(), it.get(2).toLong())
  }

  val firebaseUrl = output.lines().mapNotNull {
    val matchResult = Regex("Test results will be streamed to \\[(.*)\\].").matchEntire(it)
    matchResult?.groupValues?.get(1)
  }.single()

  return TestResult(firebaseUrl, cases)
}

data class TestResult(
    val firebaseUrl: String,
    val cases: List<Case>,
)

data class Case(
    val name: String,
    val nanos: Long,
    val allocs: Long,
)

fun issueBody(testResult: TestResult): String {
  return buildString {
    appendLine("${Date()}")
    appendLine("[${testResult.firebaseUrl}](${testResult.firebaseUrl})")
    appendLine()
    appendLine("Test Cases:")
    appendLine("| Test Case | Nanos | Allocs |")
    appendLine("|-----------|-------|--------|")
    testResult.cases.forEach {
      appendLine("|${it.name}|${it.nanos}|${it.allocs}|")
    }
  }
}

val issueTitle = "Benchmarks dashboard"
fun updateOrCreateGithubIssue(testResult: TestResult) {
  val ghRepo = System.getenv("GITHUB_REPOSITORY") ?: error("GITHUB_REPOSITORY env variable is missing")
  val ghRepositoryOwner = ghRepo.split("/")[0]
  val ghRepositoryName = ghRepo.split("/")[1]

  val query = """
{
  search(query: "$issueTitle repo:$ghRepo", type: ISSUE, first: 100) {
    edges {
      node {
        ... on Issue {
          title
          id
        }
      }
    }
  }
  repository(owner: "$ghRepositoryOwner", name: "$ghRepositoryName") {
    id
  }
}
""".trimIndent()


  val response = ghGraphQL(query)
  val existingIssues = response.get("search").asMap.get("edges").asList

  val mutation: String
  val variables: Map<String, String>
  if (existingIssues.isEmpty()) {
    mutation = """
mutation createIssue(${'$'}repositoryId: ID!, ${'$'}title: String!, ${'$'}body: String!) {
  createIssue(input: {repositoryId: ${'$'}repositoryId, title: ${'$'}title, body: ${'$'}body} ){
    clientMutationId
  }
}
    """.trimIndent()
    variables = mapOf(
        "title" to issueTitle,
        "body" to issueBody(testResult),
        "repositoryId" to response.get("repository").asMap["id"].cast<String>()
    )
    println("creating issue")
  } else {
    mutation = """
mutation updateIssue(${'$'}id: ID!, ${'$'}body: String!) {
  updateIssue(input: {id: ${'$'}id, body: ${'$'}body} ){
    clientMutationId
  }
}
    """.trimIndent()
    variables = mapOf("id" to existingIssues.first().asMap["node"].asMap["id"].cast<String>(), "body" to issueBody(testResult))
    println("updating issue")
  }
  ghGraphQL(mutation, variables)
}

fun ghGraphQL(operation: String, variables: Map<String, String> = emptyMap()): Map<String, Any?> {
  val ghToken = System.getenv("GITHUB_TOKEN") ?: error("GITHUB_TOKEN env variable is missing")
  val headers = mapOf("Authorization" to "bearer $ghToken")
  return graphQL(
      url = "https://api.github.com/graphql",
      operation = operation,
      headers = headers,
      variables = variables
  ).also { println(it) }.get("data").asMap
}

val fakeOutput = """
Activated service account credentials for: [github-actions@apollo-kotlin.iam.gserviceaccount.com]

Have questions, feedback, or issues? Get support by visiting:
  https://firebase.google.com/support/

Uploading [benchmark/build/outputs/apk/release/benchmark-release.apk] to Firebase Test Lab...
Uploading [benchmark/build/outputs/apk/androidTest/release/benchmark-release-androidTest.apk] to Firebase Test Lab...
Raw results will be stored in your GCS bucket at [https://console.developers.google.com/storage/browser/test-lab-nit79qa24t3bm-ki1nv07n9y4q0/2022-06-28_10:46:04.816175_jZBX/]

Test [matrix-2b7jcawyfiov6] has been created in the Google Cloud.
Firebase Test Lab will execute your instrumentation test on 1 device(s).
Creating individual test executions...
...............done.

Test results will be streamed to [https://console.firebase.google.com/project/apollo-kotlin/testlab/histories/bh.5e285b5fb36db2c3/matrices/8660028568169287485].
10:46:11 Test is Pending
10:46:35 Starting attempt 1.
10:46:35 Test is Running
10:46:47 Started logcat recording.
10:46:47 Started crash monitoring.
10:46:47 Preparing device.
10:47:00 Logging in to Google account on device.
10:47:00 Installing apps.
10:47:24 Retrieving Performance Environment information from the device.
10:47:24 Setting up Android test.
10:47:24 Started crash detection.
10:47:24 Started Out of memory detection
10:47:24 Started performance monitoring.
10:47:24 Starting Android test.
11:02:39 Completed Android test.
11:02:39 Stopped performance monitoring.
11:02:39 Tearing down Android test.
11:02:52 Logging out of Google account on device.
11:02:52 Stopped crash monitoring.
11:02:52 Stopped logcat recording.
11:02:52 Done. Test time = 906 (secs)
11:02:52 Starting results processing. Attempt: 1
11:02:52 Completed results processing. Time taken = 8 (secs)
11:02:52 Test is Finished

Instrumentation testing complete.

More details are available at [https://console.firebase.google.com/project/apollo-kotlin/testlab/histories/bh.5e285b5fb36db2c3/matrices/8660028568169287485].
""".trimIndent()

fun main() {
  val storage = authenticate()
  //val testOutput = runTest()
  val testOutput = fakeOutput
  val testResult = getTestResult(testOutput, storage)
  updateOrCreateGithubIssue(testResult)
  println(testResult)
}

main()