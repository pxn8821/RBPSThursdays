package com.philng.RBPSThursdays.models;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Stage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    String name;

    int roundCount;

    @OneToMany
    @JoinColumn(name = "stage")
    List<StageShooter> shooters = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "match")
    private Match match;

    @Column( columnDefinition = "Decimal(10,4) default '0.0'")
    private double averagePoints;

    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public int getRoundCount() {
        return roundCount;
    }

    public void setRoundCount( int roundCount ) {
        this.roundCount = roundCount;
    }

    public List<StageShooter> getShooters() {
        return shooters;
    }

    public void setShooters( List<StageShooter> shooters ) {
        this.shooters = shooters;
    }

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch( Match match ) {
        this.match = match;
    }

    public double getAveragePoints() {
        return averagePoints;
    }

    public void setAveragePoints( double averagePoints ) {
        this.averagePoints = averagePoints;
    }
}
