package com.philng.RBPSThursdays.models;

import jakarta.persistence.*;

@Entity
public class MatchShooterSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "match")
    private Match match;

    @Column( columnDefinition = "Decimal(10,4) default '0.0'")
    double earnedPoints;

    @ManyToOne
    private Shooter shooter;

    private double startingHandicap;

    private double newHandicap;

    boolean isPresent;


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

    public double getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints( double earnedPoints ) {
        this.earnedPoints = earnedPoints;
    }

    public double getStartingHandicap() {
        return startingHandicap;
    }

    public void setStartingHandicap( double startingHandicap ) {
        this.startingHandicap = startingHandicap;
    }

    public double getNewHandicap() {
        return newHandicap;
    }

    public void setNewHandicap( double newHandicap ) {
        this.newHandicap = newHandicap;
    }

    public double getHandicapDiff()
    {
        return newHandicap - startingHandicap;
    }


    public boolean isPresent() {
        return isPresent;
    }

    public void setPresent( boolean present ) {
        isPresent = present;
    }

    public Shooter getShooter() {
        return shooter;
    }

    public void setShooter( Shooter shooter ) {
        this.shooter = shooter;
    }


    public String getStartingHandicapDisplay() {
        return String.format("%.4f", startingHandicap);
    }
    public String getNewHandicapDisplay() {
        return String.format("%.4f", newHandicap);
    }
    public String getHandicapDiffDisplay() {
        if( getHandicapDiff() < 0 )
        {
            return String.format("%.4f",  newHandicap - startingHandicap);
        } else {
            return "+" + String.format("%.4f",  newHandicap - startingHandicap);

        }
    }
}
