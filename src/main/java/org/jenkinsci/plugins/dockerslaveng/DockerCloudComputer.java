package org.jenkinsci.plugins.dockerslaveng;

import hudson.slaves.AbstractCloudComputer;

/**
 * Docker computer.
 *
 * @author Yoann Dubreuil
 */
public class DockerCloudComputer extends AbstractCloudComputer<DockerCloudSlave> {

    private final String containerName;

    private final String imageName;

    public DockerCloudComputer(DockerCloudSlave slave, String imageName) {
        super(slave);
        this.imageName = imageName;
        this.containerName = slave.getNodeName();
    }

    public String getContainerName() {
        return containerName;
    }

    public String getImageName() {
        return imageName;
    }
}
