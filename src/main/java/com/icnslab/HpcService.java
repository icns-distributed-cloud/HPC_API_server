package com.icnslab;

import com.icnslab.database.PlatformDao;
import com.icnslab.message.*;
import com.spotify.docker.client.*;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import org.json.simple.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by alicek106 on 2017-08-03.
 */
@Repository
public class HpcService {

    @Autowired
    PlatformDao platformDao;

    private EtcdConnector etcdConnector = new EtcdConnector();

    public ContainerDeleteResponse deleteBuildContianer(com.icnslab.message.Container container){
        ContainerDeleteResponse containerDeleteResponse = new ContainerDeleteResponse();
        containerDeleteResponse.setUserUid(container.getUser());

        DockerClient builderDockerClient = new DefaultDockerClient(etcdConnector.getBuilderAddr());
        try {
            builderDockerClient.removeContainer(container.getUser() + "-" + container.getName(), DockerClient.RemoveContainerParam.forceKill());
            platformDao.deleteContainer(container.getName(), container.getUser());
            containerDeleteResponse.setResponseCode(0);
        }catch(Exception e) {
            e.printStackTrace();
            containerDeleteResponse.setResponseCode(-1);
        }

        return containerDeleteResponse;
    }

    public ContainerCreationResponse createBuildContainer(com.icnslab.message.Container container){
        ContainerCreationResponse containerCreationResponse = new ContainerCreationResponse();
        containerCreationResponse.setUserUid(container.getUser());

        DockerClient builderDockerClient = new DefaultDockerClient(etcdConnector.getBuilderAddr());
        String containerId = ContainerCreator.createContainer(builderDockerClient, container);

        if(containerId == null){
            containerCreationResponse.setResponseCode(-1);
        }else {
            String key = getPrivateKey(containerId, builderDockerClient);

            containerCreationResponse.setAccessUrl("163.180.117.219:50002");
            containerCreationResponse.setSecretAccessKey(key);
            containerCreationResponse.setResponseCode(0);

            container.setServer("163.180.117.219:50002");
            container.setCreated(getCurrentDate());
            platformDao.insertContainer(container);
        }

        builderDockerClient.close();
        return containerCreationResponse;
    }

    public ContainerCommitResponse commitContainer(String containerName, String imageName, String tag, String uid, String metadata, String baseimage){
        ContainerCommitResponse containerCommitResponse = new ContainerCommitResponse();
        DockerClient builderDockerClient = new DefaultDockerClient(etcdConnector.getBuilderAddr());
        //String containerId = platformDao.selectContainerId(uid);

        String fullImageName = commitContainer(uid + "-" + containerName, imageName, tag, uid, builderDockerClient);

        if(fullImageName == null){
            containerCommitResponse.setResponseCode(-1);
        }
        else {
            containerCommitResponse.setImageName(fullImageName);
            containerCommitResponse.setUserUid(uid);
            //deleteContainer(containerId, builderDockerClient);

            platformDao.updateContainerLastCommit(getCurrentDate(), uid);
            String mpilib = platformDao.selectContainerMpilib(uid, containerName);
            platformDao.insertImage(fullImageName, uid, getCurrentDate(), metadata, baseimage, mpilib);
        }

        builderDockerClient.close();
        return containerCommitResponse;
    }

    public ContainerCommitResponse distributeImage(String imageName, String uid){
        platformDao.updateImageStatus(imageName, uid, "distributing");
        ContainerCommitResponse containerCommitResponse = new ContainerCommitResponse();
        DockerClient builderDockerClient = new DefaultDockerClient(etcdConnector.getBuilderAddr());
        String fullImageName = etcdConnector.getRegistryAddr() + imageName;
        try {
            platformDao.updateImageStatus(imageName, uid, "distributing");
            pushImage(fullImageName, builderDockerClient);
            distributeImageToServer(fullImageName);
        }catch (Exception e){
            e.printStackTrace();
            containerCommitResponse.setResponseCode(-1);
            platformDao.updateImageStatus(imageName, uid, "error");
        }

        platformDao.updateImageStatus(imageName, uid, "ready");
        containerCommitResponse.setImageName(fullImageName);
        containerCommitResponse.setUserUid(uid);
        return containerCommitResponse;
    }

    public JSONObject getMetrics(String uid){
        JSONObject data = new JSONObject();
        List<JobMessage> list = platformDao.selectRunningJob(uid);

        // total_usage[0] : CPU, total_usage[1] : Memory
        float[] total_usage = new float[2];

        // 2개 이상이 될 일이 없음. 일단은 1개만 돌아간다고 가정.
        for(JobMessage jobMessage : list){
            if(jobMessage.getStatus().equals("running")){
                List<JobContainer> conList = platformDao.selectJobContainer(uid, jobMessage.getCreated());

                for(JobContainer jobContainer : conList) {
                    String url = String.format("https://%s:2375", jobContainer.getServer());
                    try {
                        DockerClient dc = DefaultDockerClient.builder()
                                .uri(url)
                                .dockerCertificates(new DockerCertificates(Paths.get("keys")))
                                .build();
                        float[] container_usage = getContainerUsage(dc, jobContainer.getName());
                        total_usage[0] = total_usage[0] + container_usage[0];
                        total_usage[1] = total_usage[1] + container_usage[1];

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            data.put("cpu_usage_sum", total_usage[0]);
            data.put("mem_usage_sum", total_usage[1]);
            data.put("blki", jobMessage.getBlki());
            data.put("blko", jobMessage.getBlko());
            data.put("neti", jobMessage.getNeti());
            data.put("neto", jobMessage.getNeto());
            data.put("mem_limit", jobMessage.getMem() * jobMessage.getCount());
            data.put("cpu_limit", jobMessage.getCpu() * jobMessage.getCount());
            data.put("count", jobMessage.getCount());
            data.put("image", jobMessage.getImage());
        }

        return data;
    }

    /*
    * Create Container
    * */

    private float[] getContainerUsage(DockerClient dc, String id) throws Exception{
        String usage = ExecuteCmd("ps -e -o pcpu,pmem", id, dc);
        String[] process_per_usage = usage.split("\n");
        float[] total_usage = new float[2];

        for(int i = 1; i < process_per_usage.length; i++){

            String[] process_per_usage_splited = process_per_usage[i].split(" ");
            boolean temp = false;

            for(int j = 0; j < process_per_usage_splited.length; j++){
                try{
                    float temp2 = Float.parseFloat(process_per_usage_splited[j]);
                    if(!temp){
                        total_usage[0] += temp2;
                        temp = true;
                    }

                    else{
                        total_usage[1] += temp2;
                    }
                }
                catch(NumberFormatException e){
                    continue;
                }
            }
        }

        return total_usage;
    }

    private String getPrivateKey(String containerId, DockerClient dc){
        return ExecuteCmd("cat /root/.ssh/id_rsa", containerId, dc);
    }

    private String ExecuteCmd(String command, String containerId, DockerClient dc) {
        String[] arr = command.split(" ");

        try {
            ExecCreation execCreation = dc.execCreate(containerId, arr, DockerClient.ExecCreateParam.attachStdout(),
                    DockerClient.ExecCreateParam.attachStderr());

            String execId = execCreation.id();
            LogStream stream = dc.execStart(execId);
            final String output = stream.readFully();
            stream.close();

            return output;
        }
        catch(Exception e){
            return null;
        }
    }

    /*
    * Commit Container
    * */
    private String commitContainer(String containerId, String imageName, String tag,
                                  String uid, DockerClient dc) {
        String newImageName =  etcdConnector.getRegistryAddr() + imageName;

        try {
            ContainerConfig config = dc.inspectContainer(containerId).config();

            dc.commitContainer(
                    containerId, newImageName, tag, config, getCurrentDate(), uid);
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }

        return imageName + ":" + tag;
    }

    private void deleteContainer(String containerId, DockerClient dc){
        try {
            dc.removeContainer(containerId, DockerClient.RemoveContainerParam.forceKill());
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    /*
    * Distribute Image
    * */
    private void pushImage(String imageName, DockerClient dc) throws Exception{
        dc.push(imageName, new ProgressHandler(){
            @Override
            public void progress(ProgressMessage message) throws DockerException {
                //System.out.println(message.toString());
            }
        });
    }

    private void distributeImageToServer(String imageName) throws Exception{
        List<String> list = etcdConnector.getWorkerAddr();

        for(String str : list){
            DockerClient worker = DefaultDockerClient.builder()
                    .uri(str)
                    .dockerCertificates(new DockerCertificates(Paths.get("keys")))
                    .build();
            worker.pull(imageName);
            worker.close();
        }
    }

    /*
    * etc
    * */
    private String getCurrentDate(){
        long time = System.currentTimeMillis();
        SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String str = dayTime.format(new Date(time));

        return str;
    }
}
