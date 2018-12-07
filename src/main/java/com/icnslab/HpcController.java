package com.icnslab;

import com.icnslab.database.PlatformDao;
import com.icnslab.message.*;
import com.icnslab.message.Container;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by alicek106 on 2017-08-03.
 */
@CrossOrigin(origins = "*")
@RestController
public class HpcController {

    @Autowired
    PlatformDao platformDao;

    @Autowired
    HpcService hpcService;

    @RequestMapping(value = "/api/build/container", method = RequestMethod.POST)
    @ResponseBody
    public ContainerCreationResponse createBuildContainer(@RequestParam(value = "uid", required = true) String uid,
                                                          @RequestParam(value = "containerName", required = true) String containerName,
                                                          @RequestParam(value = "mpiVersion", required = true) String mpiVersion,
                                                          @RequestParam(value = "baseImage", required = true) String baseImage) {
        Container container = new ContainerBuilder().
                setMpilib(mpiVersion).
                setName(containerName).
                setUser(uid).
                setBaseImage(baseImage).
                createContainer();
        return hpcService.createBuildContainer(container);
    }

    @RequestMapping(value = "/api/build/container", method = RequestMethod.DELETE)
    @ResponseBody
    public ContainerDeleteResponse deleteBuildContainer(@RequestParam(value = "uid", required = true) String uid,
                                                          @RequestParam(value = "containerName", required = true) String containerName){
        Container container = new ContainerBuilder().
                setName(containerName).
                setUser(uid).
                createContainer();

        return hpcService.deleteBuildContianer(container);
    }

    @RequestMapping(value = "/api/build/container", method = RequestMethod.GET)
    @ResponseBody
    public List<Container> getContainer(
            @RequestParam(value = "uid", required = true) String uid){
        return platformDao.selectContainer(uid);
    }

    @RequestMapping(value = "/api/build/commit", method = RequestMethod.POST)
    @ResponseBody
    public ContainerCommitResponse commitBuildContainer(
            @RequestParam(value = "containerName", required = true) String containerName,
            @RequestParam(value = "uid", required = true) String uid,
            @RequestParam(value = "imageName", required = true) String imageName,
            @RequestParam(value = "tag", required = true) String tag,
            @RequestParam(value = "metadata", required = true) String metadata,
            @RequestParam(value = "baseimage", required = true) String baseimage){
        return hpcService.commitContainer(containerName, imageName, tag, uid, metadata, baseimage);
    }

    @RequestMapping(value = "/api/build/distribute", method = RequestMethod.POST)
    @ResponseBody
    public ContainerCommitResponse distributeImage(
            @RequestParam(value = "imageName", required = true) String imageName,
            @RequestParam(value = "uid", required = true) String uid){
        System.out.println("starting distribute.. : " + uid + " " + imageName);
        return hpcService.distributeImage(imageName, uid);
    }

    @RequestMapping(value = "/api/build/image", method = RequestMethod.GET)
    @ResponseBody
    public List<com.icnslab.message.Image> getImages(
            @RequestParam(value = "uid", required = true) String uid){
        return platformDao.selectImage(uid);
    }

    @RequestMapping(value = "/api/job/container", method = RequestMethod.GET)
    @ResponseBody
    public List<com.icnslab.message.JobContainer> getJobContainer(
            @RequestParam(value = "uid", required = true) String uid){
        return platformDao.selectJobContainer(uid);
    }

    @RequestMapping(value = "/api/job/metrics", method = RequestMethod.GET)
    @ResponseBody
    public String getJobMetrics(
            @RequestParam(value = "uid", required = true) String uid){

        JSONObject data = hpcService.getMetrics(uid);
        return data.toJSONString();
    }
}
