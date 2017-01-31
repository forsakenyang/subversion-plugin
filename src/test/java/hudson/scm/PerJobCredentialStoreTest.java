package hudson.scm;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.Proc;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.jvnet.hudson.test.Bug;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Kohsuke Kawaguchi
 */
public class PerJobCredentialStoreTest extends AbstractSubversionTest {
    /**
     * There was a bug that credentials stored in the remote call context was serialized wrongly.
     */
    @Bug(8061)
    public void testRemoteBuild() throws Exception {
        Proc p = runSvnServe(SubversionSCMTest.class.getResource("HUDSON-1379.zip"));
        try {
            SystemCredentialsProvider.getInstance().setDomainCredentialsMap(Collections.singletonMap(Domain.global(),
                    Arrays.<Credentials>asList(
                            new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, "1-alice", null, "alice", "alice")
                    )
            ));
            FreeStyleProject b = createFreeStyleProject();
            b.setScm(new SubversionSCM("svn://localhost/bob", "1-alice", "."));
            b.setAssignedNode(createSlave());

            FreeStyleBuild run = buildAndAssertSuccess(b);
            /* TODO runSvnServe not guaranteed to use port 3690; otherwise this works:
            assertLogContains(Messages.CredentialsSVNAuthenticationProviderImpl_sole_credentials("alice/******", "<svn://localhost:3690> 8a677b3a-1c61-4b23-9212-1bf3c3d713a7"), run);
            */
        } finally {
            p.kill();
        }
    }
}
