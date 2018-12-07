package com.icnslab.database;

import com.icnslab.message.Container;
import com.icnslab.message.Image;
import com.icnslab.message.JobContainer;
import com.icnslab.message.JobMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by alicek106 on 2017-08-03.
 */
@Repository
public class PlatformDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertContainer(Container container){
        String query = String.format("insert into build_container values('%s','%s','%s','%s','%s','%s','%s','%s');",
            container.getName(), container.getUser(), container.getServer(), "true", "none", container.getCreated(),
                container.getBaseimage(), container.getMpilib());
        jdbcTemplate.update(query);
    }

    public String selectContainerId(String user){
        String query = String.format("SELECT name FROM build_container where user = '%s' and alive = 'true'", user);
        return jdbcTemplate.queryForObject(query, String.class);
    }

    public List<Container> selectContainer(String user){
        String query = String.format("SELECT * FROM build_container where user = '%s'", user);
        List<Container> container = jdbcTemplate.query(query, new BeanPropertyRowMapper(Container.class));
        return container;
    }

    public String selectContainerMpilib(String user, String container){
        String query = String.format("SELECT mpilib FROM build_container where user = '%s' and name = '%s'", user, container);
        String mpilib = jdbcTemplate.queryForObject(query, String.class);
        return mpilib;
    }

    public void updateContainerLastCommit(String date, String user){
        String query = String.format("update build_container set lastcommit='%s' where user = '%s' and alive = 'true'", date, user);
        jdbcTemplate.update(query);
    }

    public void deleteContainer(String container, String user){
        String query = String.format("delete from build_container where user = '%s' and name = '%s'", user, container);
        jdbcTemplate.update(query);
    }

    public void insertImage(String name, String user, String created, String metadata, String baseimage, String mpilib){
        String query = String.format("insert into image values('%s','%s','%s','%s','%s', '%s', '%s');",
                name, user, created, "not ready", metadata, baseimage, mpilib);
        jdbcTemplate.update(query);
    }

    public void updateImageStatus(String name, String user, String status){
        String query = String.format("update image set status='%s' where name = '%s' and user = '%s'", status, name, user);
        jdbcTemplate.update(query);
    }

    public List<Image> selectImage(String user) {
        String query = String.format("SELECT * FROM image where user = '%s'", user);
        List<Image> image = jdbcTemplate.query(query, new BeanPropertyRowMapper(Image.class));
        return image;
    }

    public List<JobContainer> selectJobContainer(String user){
        String query = String.format("SELECT * FROM job_container where user = '%s'", user);
        List<JobContainer> jobContainers = jdbcTemplate.query(query, new BeanPropertyRowMapper(JobContainer.class));
        return jobContainers;
    }

    public List<JobMessage> selectRunningJob(String uid){
        String query = String.format("SELECT * FROM job where user = '%s' and status = 'running'", uid);
        List<JobMessage> jobs = jdbcTemplate.query(query, new BeanPropertyRowMapper(JobMessage.class));
        return jobs;
    }

    public List<JobContainer> selectJobContainer(String uid, String created){
        String query = String.format("SELECT * FROM job_container where user = '%s' and created = '%s'", uid, created);
        List<JobContainer> jobs = jdbcTemplate.query(query, new BeanPropertyRowMapper(JobContainer.class));
        return jobs;
    }
}
