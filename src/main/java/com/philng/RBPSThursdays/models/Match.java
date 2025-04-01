package com.philng.RBPSThursdays.models;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private Timestamp dateImported;

    private String practiscoreURL;

    @OneToMany
    @JoinColumn(name = "match")
    List<Stage> stages = new ArrayList<>();

    @OneToMany
    @JoinColumn(name = "match")
    List<MatchShooterSummary> summary = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Timestamp getDateImported() {
        return dateImported;
    }

    public void setDateImported( Timestamp dateImported ) {
        this.dateImported = dateImported;
    }

    public String getPractiscoreURL() {
        return practiscoreURL;
    }

    public void setPractiscoreURL( String practiscoreURL ) {
        this.practiscoreURL = practiscoreURL;
    }

    public List<Stage> getStages() {
        return stages;
    }

    public void setStages( List<Stage> stages ) {
        this.stages = stages;
    }

    public List<MatchShooterSummary> getSummary() {
        return summary;
    }

    public void setSummary( List<MatchShooterSummary> summary ) {
        this.summary = summary;
    }
}
