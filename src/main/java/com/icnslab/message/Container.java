package com.icnslab.message;

import java.io.Serializable;

/**
 * Created by alicek106 on 2017-08-03.
 */
public class Container implements Serializable{
    private String name;
    private String user;
    private String server;
    private String alive;
    private String lastcommit;
    private String created;
    private String baseimage;
    private String mpilib;

    public Container(){}

    public Container(String name, String user, String server, String alive, String lastcommit, String created, String baseimage, String mpilib) {
        this.name = name;
        this.user = user;
        this.server = server;
        this.alive = alive;
        this.lastcommit = lastcommit;
        this.created = created;
        this.baseimage = baseimage;
        this.mpilib = mpilib;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getAlive() {
        return alive;
    }

    public void setAlive(String alive) {
        this.alive = alive;
    }

    public String getLastcommit() {
        return lastcommit;
    }

    public void setLastcommit(String lastcommit) {
        this.lastcommit = lastcommit;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getBaseimage() {
        return baseimage;
    }

    public void setBaseimage(String baseimage) {
        this.baseimage = baseimage;
    }

    public String getMpilib() {
        return mpilib;
    }

    public void setMpilib(String mpilib) {
        this.mpilib = mpilib;
    }
}
