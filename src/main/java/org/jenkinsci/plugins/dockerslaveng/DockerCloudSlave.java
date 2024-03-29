package org.jenkinsci.plugins.dockerslaveng;

import hudson.Launcher;
import hudson.model.Descriptor;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.AbstractCloudComputer;
import hudson.slaves.AbstractCloudSlave;
import hudson.slaves.CloudRetentionStrategy;
import hudson.slaves.EphemeralNode;
import hudson.slaves.NodeProperty;
import hudson.slaves.SlaveComputer;

import java.io.IOException;
import java.util.Collections;

public class DockerCloudSlave extends AbstractCloudSlave implements EphemeralNode {

    public final String imageName;

    DockerCloudSlave(String slaveName, Node.Mode mode, String labelString, String imageName) throws Descriptor.FormException, IOException {
        super(slaveName, "Mock Slave", "/home/jenkins", 1, mode, labelString, new DockerComputerLauncher(), new CloudRetentionStrategy(0), Collections.<NodeProperty<?>>emptyList());
        this.imageName = imageName;
    }

    @Override
    public AbstractCloudComputer createComputer() {
        return new DockerCloudComputer(this, imageName);
    }

    @Override
    protected void _terminate(TaskListener listener) throws IOException, InterruptedException {
        // NOOP: destroyed by docker
    }

    @Override
    public Launcher createLauncher(TaskListener listener) {
        SlaveComputer c = getComputer();
        if (c == null) {
            listener.error("Issue with creating launcher for slave " + name + ".");
            return new Launcher.DummyLauncher(listener);
        } else {
            DockerCloudComputer dc = (DockerCloudComputer) c;
            return new DockerLauncher(listener, c.getChannel(), c.isUnix(), getNodeName()).decorateFor(this);
        }
    }

    @Override
    public Node asNode() {
        return this;
    }
}
