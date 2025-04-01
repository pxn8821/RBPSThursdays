package com.philng.RBPSThursdays.models;

import jakarta.persistence.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;

@Entity
public class StageShooter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "stage")
    private Stage stage;

    @ManyToOne
    private Shooter shooter;
    private double startingHandicap;
    private double rawMatchPoints;
    private double rawPercentage;
    private double rawHitFactor;
    private double newHandicap;
    private double handicapHitFactor;
    private double handicapMatchPoints;
    private double handicapPercentage;

    @Transient
    private static DecimalFormat df = new DecimalFormat("0.0000");
    static {
        df.setRoundingMode( RoundingMode.HALF_UP);
        df.setMaximumFractionDigits(8);
    }

    public long getId() {
        return id;
    }

    public void setId( long id ) {
        this.id = id;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage( Stage stage ) {
        this.stage = stage;
    }

    public Shooter getShooter() {
        return shooter;
    }

    public void setShooter( Shooter shooter ) {
        this.shooter = shooter;
    }

    public double getStartingHandicap() {
        return startingHandicap;
    }

    public void setStartingHandicap( double startingHandicap ) {
        this.startingHandicap = startingHandicap;
    }

    public double getRawMatchPoints() {
        return rawMatchPoints;
    }

    public void setRawMatchPoints( double rawMatchPoints ) {
        this.rawMatchPoints = rawMatchPoints;
    }

    public double getRawHitFactor() {
        return rawHitFactor;
    }

    public void setRawHitFactor( double rawHitFactor ) {
        this.rawHitFactor = rawHitFactor;
    }

    public double getNewHandicap() {
        return newHandicap;
    }

    public void setNewHandicap( double newHandicap ) {
        this.newHandicap = newHandicap;
    }

    public double getHandicapHitFactor() {
        return handicapHitFactor;
    }

    public void setHandicapHitFactor( double handicapHitFactor ) {
        this.handicapHitFactor = handicapHitFactor;
    }

    public double getHandicapMatchPoints() {
        return handicapMatchPoints;
    }

    public void setHandicapMatchPoints( double handicapMatchPoints ) {
        this.handicapMatchPoints = handicapMatchPoints;
    }

    public double getHandicapPercentage() {
        return handicapPercentage;
    }

    public void setHandicapPercentage( double handicapPercentage ) {
        this.handicapPercentage = handicapPercentage;
    }

    public double getRawPercentage() {
        return rawPercentage;
    }

    public void setRawPercentage( double rawPercentage ) {
        this.rawPercentage = rawPercentage;
    }

    public double getHandicapDiff()
    {
        return newHandicap - startingHandicap;
    }



    public String getStartingHandicapDisplay() {
        return String.format("%.4f", startingHandicap);
    }
    public String getRawMatchPointsDisplay() {
        return String.format("%.4f", rawMatchPoints);
    }
    public String getRawHitFactorDisplay() {
        return String.format("%.4f", rawHitFactor);
    }
    public String getNewHandicapDisplay() {
        return String.format("%.4f", newHandicap);
    }
    public String getHandicapHitFactorDisplay() {
        return String.format("%.4f", handicapHitFactor);
    }
    public String getHandicapMatchPointsDisplay() {
        return String.format("%.4f", handicapMatchPoints);
    }
    public String getHandicapPercentageDisplay() {
        return String.format("%.4f", handicapPercentage);
    }
    public String getRawPercentageDisplay() {
        return String.format("%.4f", rawPercentage);
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
