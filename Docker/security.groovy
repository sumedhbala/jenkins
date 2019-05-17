#!groovy

import jenkins.model.*
import hudson.security.*
import jenkins.security.s2m.AdminWhitelistRule
import org.jenkinsci.plugins.scriptsecurity.scripts.*

def instance = Jenkins.getInstance()
def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def env = System.getenv()
hudsonRealm.createAccount(System.getenv('JENKINS_USERNAME'), System.getenv('JENKINS_PASSWORD'))
instance.setSecurityRealm(hudsonRealm)
def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
instance.setAuthorizationStrategy(strategy)
instance.save()
Jenkins.instance.getInjector().getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false)
def scriptApproval = ScriptApproval.get()
scriptApproval.approveSignature('method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object')
scriptApproval.save()
