import hudson.plugins.git. * ;
import jenkins.model.Jenkins
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

	list.each {
		println it.name
		def flowDefinition = new CpsScmFlowDefinition(scm, jobsDir + it.name)
		def job = new WorkflowJob(parent, org.apache.commons.io.FilenameUtils.getBaseName(it.name))
		job.definition = flowDefinition
		job.save()
                job.scheduleBuild2(0, null, new ParametersAction(params))
	}
} finally {
        ["rm -rf", cloneDir].join(" ").execute()
	parent.reload()
}
