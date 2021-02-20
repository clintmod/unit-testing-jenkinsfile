class JenkinsFileTest extends GroovyTestCase {

    void testSuccess() {
        def jenkinsPipeline = getJenkinsPipeline()
        captureStdOut() {  buffer ->
            jenkinsPipeline.main()
            def actual = buffer.toString()
            assert expected == actual
        }
    }

    void testFailure() {
        def jenkinsPipeline = getJenkinsPipeline()
        jenkinsPipeline.metaClass.currentBuild = [result: "FAILURE"]
        captureStdOut() { buffer ->
            jenkinsPipeline.main()
            def actual = buffer.toString()
            assert expectedFailure == actual
        }
    }

    void testException() {
        def jenkinsPipeline = getJenkinsPipeline()
        jenkinsPipeline.metaClass.sh = { command -> 
            println("Running sh command: ${command}")
            throw new Exception("asdf")
        }
        jenkinsPipeline.metaClass.currentBuild = [result: "FAILURE"]
        captureStdOut() { buffer ->
            shouldFail Exception, {
                jenkinsPipeline.main()
            }
            def actual = buffer.toString()
            assert expectedError == actual
        }
    }

    def getJenkinsPipeline() {
        def shell = new GroovyShell()
        def jenkinsPipeline = shell.parse(new File('Jenkinsfile'))
        stubJenkinsApi(jenkinsPipeline)
        return jenkinsPipeline
    }

    def captureStdOut(func) {
        def oldOut = System.out
        def buffer = new ByteArrayOutputStream()
        def newOut = new PrintStream(buffer)
        System.out = newOut
        func(buffer)
        System.out = oldOut
    }

    def stubJenkinsApi(jenkinsPipeline) {
        jenkinsPipeline.metaClass.buildDiscarder = { args -> }
        jenkinsPipeline.metaClass.checkout = { args -> }
        jenkinsPipeline.metaClass.cron = {}
        jenkinsPipeline.metaClass.currentBuild = [result: "SUCCESS"]
        jenkinsPipeline.metaClass.disableConcurrentBuilds = {}
        jenkinsPipeline.metaClass.echo = { message -> println(message) }
        jenkinsPipeline.metaClass.env = [
            JOB_NAME:"asdf",
            BUILD_NUMBER:"1",
            RUN_DISPLAY_URL:"asdf2",
        ]
        jenkinsPipeline.metaClass.githubNotify = { args ->
            println "githubNotify ${args}"
        }
        jenkinsPipeline.metaClass.logRotator = { args -> 
            println("Setting log rotate to ${args.daysToKeepStr} days") 
        }
        jenkinsPipeline.metaClass.node = { name , func -> 
            println("Running on node ${name}"); func() 
        }
        jenkinsPipeline.metaClass.pipelineTriggers = {}
        jenkinsPipeline.metaClass.pipelineTriggers = {}
        jenkinsPipeline.metaClass.properties = {}
        jenkinsPipeline.metaClass.pullRequest = [
            comment: { msg ->
                println "pullRequest.comment ${msg}"
            }
        ]
        jenkinsPipeline.metaClass.readFile = { path ->
            return new File(path).getText().trim()
        }
        jenkinsPipeline.metaClass.scm = [:]
        jenkinsPipeline.metaClass.sh = { command -> 
            println("Running sh command: ${command}")
        }
        jenkinsPipeline.metaClass.slackSend = { args ->
            println ("slackSend channel:${args.channel} " 
             + "message:${args.message} color:${args.color}")
        }
        jenkinsPipeline.metaClass.stage = { name , func -> 
            println("Running stage: ${name}"); func() 
        }
        jenkinsPipeline.metaClass.timeout = { args, func -> 
            println("Setting timeout to ${args.time} ${args.unit}"); func() 
        }
        jenkinsPipeline.metaClass.withPyenv = {verison, func -> func()}
    }

    def expected = """\
        Setting timeout to 45 MINUTES
        Running on node macos
        Setting log rotate to 30 days
        githubNotify [status:PENDING, context:Pipeline]
        Running stage: Checkout
        Running stage: Prereqs
        githubNotify [status:PENDING, context:Prereqs]
        Running sh command: make ensure-prerequisites
        githubNotify [status:SUCCESS, context:Prereqs]
        Running stage: Setup
        githubNotify [status:PENDING, context:Setup]
        Running sh command: make setup
        githubNotify [status:SUCCESS, context:Setup]
        Running stage: Jenkinsfile
        githubNotify [status:PENDING, context:Jenkinsfile]
        Running sh command: make test-jenkinsfile
        githubNotify [status:SUCCESS, context:Jenkinsfile]
        Running stage: Lint
        githubNotify [status:PENDING, context:Lint]
        Running sh command: make lint
        githubNotify [status:SUCCESS, context:Lint]
        Running stage: Build
        githubNotify [status:PENDING, context:Build]
        Running sh command: make build
        githubNotify [status:SUCCESS, context:Build]
        Running stage: Test
        githubNotify [status:PENDING, context:Test]
        Running sh command: make test
        githubNotify [status:SUCCESS, context:Test]
        Running stage: E2E
        githubNotify [status:PENDING, context:E2E]
        Running sh command: make test-e2e
        githubNotify [status:SUCCESS, context:E2E]
        Running stage: Upload
        githubNotify [status:PENDING, context:E2E]
        Running sh command: make upload
        Running sh command: scripts/get-git-branch.sh
        Running sh command: scripts/get-safe-branch.sh null
        pullRequest.comment The reivew app is available at: https://cool.repo/repository/maven-general-temp/repository/maven-general-temp/com/tillful/app/till-native/null/till-native-null.app
        githubNotify [status:SUCCESS, context:Upload]
        Running post build actions
        githubNotify [status:SUCCESS, context:Pipeline]
        """.stripIndent()

    def expectedFailure = """\
        Setting timeout to 45 MINUTES
        Running on node macos
        Setting log rotate to 30 days
        githubNotify [status:PENDING, context:Pipeline]
        Running stage: Checkout
        Running stage: Prereqs
        githubNotify [status:PENDING, context:Prereqs]
        Running sh command: make ensure-prerequisites
        githubNotify [status:SUCCESS, context:Prereqs]
        Running stage: Setup
        githubNotify [status:PENDING, context:Setup]
        Running sh command: make setup
        githubNotify [status:SUCCESS, context:Setup]
        Running stage: Jenkinsfile
        githubNotify [status:PENDING, context:Jenkinsfile]
        Running sh command: make test-jenkinsfile
        githubNotify [status:SUCCESS, context:Jenkinsfile]
        Running stage: Lint
        githubNotify [status:PENDING, context:Lint]
        Running sh command: make lint
        githubNotify [status:SUCCESS, context:Lint]
        Running stage: Build
        githubNotify [status:PENDING, context:Build]
        Running sh command: make build
        githubNotify [status:SUCCESS, context:Build]
        Running stage: Test
        githubNotify [status:PENDING, context:Test]
        Running sh command: make test
        githubNotify [status:SUCCESS, context:Test]
        Running stage: E2E
        githubNotify [status:PENDING, context:E2E]
        Running sh command: make test-e2e
        githubNotify [status:SUCCESS, context:E2E]
        Running stage: Upload
        githubNotify [status:PENDING, context:E2E]
        Running sh command: make upload
        Running sh command: scripts/get-git-branch.sh
        Running sh command: scripts/get-safe-branch.sh null
        pullRequest.comment The reivew app is available at: https://cool.repo/repository/maven-general-temp/repository/maven-general-temp/com/tillful/app/till-native/null/till-native-null.app
        githubNotify [status:SUCCESS, context:Upload]
        Running post build actions
        githubNotify [status:FAILURE, context:Pipeline]
        """.stripIndent()

    def expectedError = """\
        Setting timeout to 45 MINUTES
        Running on node macos
        Setting log rotate to 30 days
        githubNotify [status:PENDING, context:Pipeline]
        Running stage: Checkout
        Running stage: Prereqs
        githubNotify [status:PENDING, context:Prereqs]
        Running sh command: make ensure-prerequisites
        githubNotify [status:FAILURE, context:Prereqs]
        Running post build actions
        githubNotify [status:FAILURE, context:Pipeline]
        """.stripIndent()
}
