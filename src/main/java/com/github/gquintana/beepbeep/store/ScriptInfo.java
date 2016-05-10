package com.github.gquintana.beepbeep.store;

import java.time.Instant;

/**
 * Script data as stored in database
 * @param <ID> Identifier type
 */
public class ScriptInfo<ID> {
    private ID id;
    private Integer version;
    private String fullName;
    private long size;
    private String sha1;
    private Instant startDate;
    private Instant endDate;
    private ScriptStatus status;

    public ScriptInfo() {
    }

    public ScriptInfo(ID id, Integer version, String fullName, long size, String sha1, Instant startDate, Instant endDate, ScriptStatus status) {
        this.fullName = fullName;
        this.id = id;
        this.version = version;
        this.size = size;
        this.sha1 = sha1;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public ID getId() {
        return id;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public ScriptStatus getStatus() {
        return status;
    }

    public void setStatus(ScriptStatus status) {
        this.status = status;
    }
}
