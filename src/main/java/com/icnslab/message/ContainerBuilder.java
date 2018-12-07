package com.icnslab.message;

public class ContainerBuilder {
    private String name;
    private String user;
    private String server;
    private String alive;
    private String lastcommit;
    private String created;
    private String baseImage;
    private String mpilib;

    public ContainerBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ContainerBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public ContainerBuilder setServer(String server) {
        this.server = server;
        return this;
    }

    public ContainerBuilder setAlive(String alive) {
        this.alive = alive;
        return this;
    }

    public ContainerBuilder setLastcommit(String lastcommit) {
        this.lastcommit = lastcommit;
        return this;
    }

    public ContainerBuilder setCreated(String created) {
        this.created = created;
        return this;
    }

    public ContainerBuilder setBaseImage(String baseImage) {
        this.baseImage = baseImage;
        return this;
    }

    public ContainerBuilder setMpilib(String mpilib) {
        this.mpilib = mpilib;
        return this;
    }

    public Container createContainer() {
        return new Container(name, user, server, alive, lastcommit, created, baseImage, mpilib);
    }
}