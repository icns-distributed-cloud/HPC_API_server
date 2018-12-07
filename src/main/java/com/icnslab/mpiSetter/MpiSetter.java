package com.icnslab.mpiSetter;

import com.icnslab.message.Container;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerInfo;

import java.util.List;

/**
 * Created by alicek106 on 2017-08-06.
 */
public interface MpiSetter {
    public static final String MPI_LIB_BIND_PATH = null;
    public static final String MPI_EXAMPLE_BIND_PATH = null;
    public static final String MPI_EXE_BIND_PATH = "/mnt/lustre/scratch/%s/exe:/root/exe";
    public static final String MPI_OUTPUT_PATH = "/mnt/lustre/scratch/%s/output:/root/output";

    public abstract List<String> setMpiBind(Container jobMessage);
}
