/*
 * The MIT License
 *
 * Copyright 2014 Jesse Glick
 * Copyright 2015 Yoann Dubreuil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.dockerslaveng;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.Node;
import hudson.slaves.Cloud;
import hudson.slaves.NodeProvisioner;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple cloud provisioner based on MockCloud
 *
 * @author Jesse Glick
 * @author Yoann Dubreuil
 */
public class DockerCloud extends Cloud {

    private static final Logger LOGGER = Logger.getLogger(DockerCloud.class.getName());

    static {
        // JENKINS-24752: make things happen more quickly so that we can test it interactively.
        NodeProvisioner.NodeProvisionerInvoker.INITIALDELAY = 1000;
        NodeProvisioner.NodeProvisionerInvoker.RECURRENCEPERIOD = 1000;
    }

    public final Node.Mode mode;
    public final String labelString;
    public final String dockerSlaveImageName;

    @DataBoundConstructor
    public DockerCloud(String name, Node.Mode mode, String labelString, String dockerSlaveImageName) {
        super(name);
        this.mode = mode;
        this.labelString = labelString;
        this.dockerSlaveImageName = dockerSlaveImageName;
    }

    @Override
    public boolean canProvision(Label label) {
        LOGGER.log(Level.FINE, "checking whether we can provision {0}", label);
        return label == null ? mode == Node.Mode.NORMAL : label.matches(Label.parse(labelString));
    }

    @Override
    public Collection<NodeProvisioner.PlannedNode> provision(Label label, int excessWorkload) {
        Collection<NodeProvisioner.PlannedNode> r = new ArrayList<NodeProvisioner.PlannedNode>();
        int cnt = 0;
        while (excessWorkload > 0) {
            final String name = "dockerng-slave-" + System.currentTimeMillis() + cnt++;
            r.add(new NodeProvisioner.PlannedNode(name, Computer.threadPoolForRemoting.submit(new Callable<Node>() {
                @Override
                public Node call() throws Exception {
                    return new DockerCloudSlave(name, mode, labelString, dockerSlaveImageName);
                }
            }), 1));
            excessWorkload -= 1;
        }
        LOGGER.log(Level.FINE, "planning to provision {0} slaves", r.size());
        return r;
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Cloud> {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Docker Slave NG";
        }
    }
}
