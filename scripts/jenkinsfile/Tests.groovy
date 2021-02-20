import groovy.util.GroovyTestSuite
import junit.framework.Test
import junit.textui.TestRunner

class AllTests {
   static Test suite() {
      def allTests = new GroovyTestSuite()
      allTests.addTestSuite(JenkinsFileTest.class)
      return allTests
   }
}

testRun = TestRunner.run(AllTests.suite())

if (testRun.failureCount() > 0 || testRun.errorCount() > 0) {
    System.exit(1)
}