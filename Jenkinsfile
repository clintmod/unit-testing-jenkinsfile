main()

def main() {
  timeout(time: 45, unit: 'MINUTES') {
    try {
      node( 'macos') {
        setupProperties()
        runPipeline()
      }
    } catch (err) {
      currentBuild.result = 'FAILURE'
      throw err
    } finally {
      postBuildActions()
    }
  }
}

def setupProperties() {
  // set the TERM env var so colors show up
  env.TERM = 'xterm'
  properties([
    buildDiscarder(logRotator(daysToKeepStr: '30')),
    disableConcurrentBuilds(),
  ])
}

def runPipeline() {
    githubNotify status: 'PENDING', context: 'Pipeline'
    stage('Checkout') { checkout scm }
    def step_defs = [
      [name:'Prereqs',  command:'make ensure-prerequisites'],
      [name:'Setup',    command:'make setup'],
      [name:'Jenkinsfile',  command:'make test-jenkinsfile'],
      [name:'Lint',     command:'make lint'],
      [name:'Build',    command:'make build'],
      [name:'Test',     command:'make test'],
      [name:'E2E',      command:'make test-e2e'],
    ]
    step_defs.each { step_def ->
      stage(step_def.name) {
        step = makeStep(step_def)
        step()
      }
    }
    upload()
}

// returns a closure to be invoked
def makeStep(step_def) {
  return {
    try {
      githubNotify status: 'PENDING', context: step_def.name
      sh step_def.command
      githubNotify status: 'SUCCESS', context: step_def.name
    } catch (err) {
      githubNotify status: 'FAILURE', context: step_def.name
      currentBuild.result = 'FAILURE'
      throw err
    }
  }
}

def postBuildActions() {
  echo "Running post build actions"
  try {
    def currentResult = currentBuild.result ?: 'SUCCESS'
    if (currentResult == 'FAILURE') {
      githubNotify status: 'FAILURE', context: 'Pipeline'
    } else {
      githubNotify status: 'SUCCESS', context: 'Pipeline'
    }
  } catch (err) {
    githubNotify status: 'FAILURE', context: 'Pipeline'
    throw err
  } finally {
    // deleteDir()
  }
}


def upload() {
  stage('Upload') { 
    try {
      githubNotify status: 'PENDING', context: "E2E"
      sh 'make upload'
      gitBranch = sh("scripts/get-git-branch.sh")
      branch = sh("scripts/get-safe-branch.sh ${gitBranch}")
      msg = "The reivew app is available at: \
https://cool.repo/repository/maven-general-temp/\
repository/maven-general-temp/com/tillful/app/till-native/\
${branch}/till-native-${branch}.app"
      pullRequest.comment(msg)
    githubNotify status: 'SUCCESS', context: "Upload"
    } catch (err) {
      githubNotify status: 'FAILURE', context: "Upload"
      currentBuild.result = 'FAILURE'
      throw err
    }
  }
}