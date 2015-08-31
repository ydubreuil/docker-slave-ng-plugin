package org.jenkinsci.plugins.dockerslaveng;

import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.ArgumentListBuilder;
import jenkins.model.Jenkins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Process launcher which uses docker exec instead of execve
 */
public class DockerLauncher extends Launcher.DecoratedLauncher {
    private static final Logger LOGGER = Logger.getLogger(DockerLauncher.class.getName());

    private final String dockerContainerName;

    private final Launcher localLauncher;

    public DockerLauncher(TaskListener listener, VirtualChannel channel, boolean isUnix, String dockerContainerName)  {
        super(new Launcher.RemoteLauncher(listener, channel, isUnix));
        this.dockerContainerName = dockerContainerName;
        this.localLauncher = new Launcher.LocalLauncher(listener);
    }

    @Override
    public Proc launch(ProcStarter starter) throws IOException {
        wrapProcStarter(starter);
        return localLauncher.launch(starter);
    }

    protected void wrapProcStarter(ProcStarter starter) {
        List<String> originalCmds = starter.cmds();

        ArgumentListBuilder args = new ArgumentListBuilder()
                .add("docker", "exec", "--tty")
                .add(dockerContainerName);

        boolean[] originalMask = starter.masks();
        for (int i = 0; i < originalCmds.size(); i++) {
            boolean masked = originalMask == null ? false : i < originalMask.length ? originalMask[i] : false;
            args.add(originalCmds.get(i), masked);
        }

        starter.cmds(args);
        starter.pwd(Jenkins.getInstance().getRootDir());
    }
}
