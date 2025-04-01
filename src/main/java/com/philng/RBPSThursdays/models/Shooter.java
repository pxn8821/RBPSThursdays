package com.philng.RBPSThursdays.models;

import jakarta.persistence.*;

@Entity
public class Shooter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    String name;

    @Column( columnDefinition = "integer default '0'")
    int stagesShot;

    @Column( columnDefinition = "integer default '0'")
    int matchesShot;

    @Column( columnDefinition = "Decimal(10,4) default '0.0'")
    double currentHandicap;

    @Column( columnDefinition = "Decimal(10,4) default '0.0'")
    double currentPoints;

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

    public int getStagesShot() {
        return stagesShot;
    }

    public void setStagesShot( int stagesShot ) {
        this.stagesShot = stagesShot;
    }

    public int getMatchesShot() {
        return matchesShot;
    }

    public void setMatchesShot( int matchesShot ) {
        this.matchesShot = matchesShot;
    }

    public double getCurrentHandicap() {
        return currentHandicap;
    }

    public void setCurrentHandicap( double currentHandicap ) {
        this.currentHandicap = currentHandicap;
    }

    public double getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints( double currentPoints ) {
        this.currentPoints = currentPoints;
    }

    public String getCurrentHandicapDisplay()
    {
        return String.format("%.4f", getCurrentHandicap());
    }
}
