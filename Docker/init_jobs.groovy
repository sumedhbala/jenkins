import hudson.plugins.git.*;
import hudson.model.ParametersAction;
import hudson.model.Result;
import hudson.model.StringParameterValue;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.authorizeproject.AuthorizeProjectProperty;
import org.jenkinsci.plugins.authorizeproject.strategy.TriggeringUsersAuthorizationStrategy;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import static groovy.io.FileType.FILES;

def env = System.getenv()
def gitRepo = env.GIT_REPO ?: "git://github.com/sumedhbala/jenkins.git"
def gitBranch = env.GIT_BRANCH ?: "master"
def scm = new GitSCM(gitRepo)
scm.branches = [new BranchSpec(gitBranch)];
def cloneDir = "/tmp/clone/"
def jobsDir = env.JOBS_SUBDIR ?: "jenkins/jobs/"
def initJob = env.INIT_JOB ?: "jenkine/init/init_jobs.groovy"
def parent = Jenkins.instance

try {
	eachCommands = [["mkdir -p ", cloneDir], ["git", "clone", "--depth 1", gitRepo, "-b", gitBranch, cloneDir]]
	eachCommands.each {
		def sout = new StringBuilder(),
		serr = new StringBuilder()
		def proc = it.join(" ").execute()
		proc.consumeProcessOutput(sout, serr)
		proc.waitForOrKill(1000)
		println "out> $sout err> $serr"
		assert ! proc.exitValue()

	}

	def list = []
	def dir = new File(cloneDir + jobsDir)
	dir.eachFileRecurse(FILES) {
		file -> list << file
	}
        def params = []
        gitUrlParam = new StringParameterValue('git_url', gitRepo) 
        params.add(gitUrlParam)
        branchParam = new StringParameterValue('branch', gitBranch)
        params.add(branchParam)
        def flowDefinition = new CpsScmFlowDefinition(scm, initJob)
        def job = new WorkflowJob(parent, "init")
        job.definition = flowDefinition
        job.save()

	list.each {
                def scriptParam new StringParameterValue('scripts', jobsDir+it.name)
                job.addProperty(new AuthorizeProjectProperty(new TriggeringUsersAuthorizationStrategy()))
                def result = job.scheduleBuild2(0, null, new ParametersAction(params)).get().result
                assertEquals(Result.SUCCESS, result)
                
	}
} finally {
        ["rm -rf", cloneDir].join(" ").execute()
}
