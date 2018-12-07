package com.icnslab;

import com.icnslab.message.Container;
import com.icnslab.mpiSetter.MpichSetter;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alicek106 on 2017-08-17.
 */
public class ContainerCreator {
    // private static final String CENTOS_IMAGE_NAME = "601kecila/ssh-server-centos:0.0-devel-tools";
    private static final String CENTOS_IMAGE_NAME = "601kecila/ssh-server-centos7:0.1-tcconfig";
    private static final String UBUNTU_IMAGE_NAME = "601kecila/ssh-server-ubuntu14-04:0.0-tcconfig";
    private static final String[] AVAILABLE_PORT = {"50002","50003","50004"};

    public static String createContainer(DockerClient dc, Container container){
        // temp : MPICH
        List<String> binds = null;
        if(container.getMpilib().contains("mpich")){
            MpichSetter mpichSetter = new MpichSetter();
            binds = mpichSetter.setMpiBind(container);
        }

        final String[] ports = {"22"};
        final Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) {
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.create("0.0.0.0", AVAILABLE_PORT[0]));//randomPort("0.0.0.0"));
            portBindings.put(port, hostPorts);
        }

        final HostConfig hostConfig = HostConfig.builder().
                portBindings(portBindings).
                binds(binds).
                build();

        String image = (container.getBaseimage().equals("centos")?CENTOS_IMAGE_NAME:UBUNTU_IMAGE_NAME);

        // Create container with exposed ports
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image(image)
                .hostname(container.getName())
                .tty(true)
                .openStdin(true).exposedPorts(ports)
                .build();

        try {
            final ContainerCreation creation = dc.createContainer(containerConfig, container.getUser() + "-" + container.getName());
            final String id = creation.id();

            // Start container
            dc.startContainer(id);
            return id;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }
}
