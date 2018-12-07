package com.icnslab.mpiSetter;

import com.icnslab.message.Container;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alicek106 on 2017-08-06.
 */
public class MpichSetter implements MpiSetter {
    public static final String MPI_LIB_BIND_PATH = "/mnt/lustre/app/mpich/%s/lib/:/home/app/mpich/mpich-libs";
    public static final String MPI_EXAMPLE_BIND_PATH = "/mnt/lustre/app/mpich/examples/:/home/app/mpich/example";

    public List<String> setMpiBind(Container jobMessage){
        // MPI Library Specific ... Settings
        List<String> binds = new ArrayList<String>();
        binds.add(String.format(MPI_LIB_BIND_PATH, jobMessage.getMpilib().replace("mpich", "")));
        binds.add(MPI_EXAMPLE_BIND_PATH);

        // default
        binds.add(String.format(MPI_EXE_BIND_PATH, jobMessage.getUser()));
        return binds;
    }
}
