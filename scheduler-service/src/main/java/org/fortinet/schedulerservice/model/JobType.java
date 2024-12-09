package org.fortinet.schedulerservice.model;

public enum JobType {
    ARCHIVAL("archival-tasks"),
    DELETION("deletion-tasks");

    private final String type;

    JobType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}