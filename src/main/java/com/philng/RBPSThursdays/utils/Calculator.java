package com.philng.RBPSThursdays.utils;

import com.philng.RBPSThursdays.models.*;
import com.philng.RBPSThursdays.repository.*;
import jakarta.annotation.PostConstruct;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope("singleton")
@Component
public class Calculator {
    private static final Map<Integer, Double> handicapFactors = new HashMap<>();

    private static final double NOT_PRESENT_SCORING_FACTOR = 0.7;
    static {
        handicapFactors.put( 32, 4.46031029);
        handicapFactors.put( 31, 4.531679787);
        handicapFactors.put( 30, 4.60658866);
        handicapFactors.put( 29, 4.685339469);
        handicapFactors.put( 28, 4.76827227);
        handicapFactors.put( 27, 4.855770803);
        handicapFactors.put( 26, 4.948269987);
        handicapFactors.put( 25, 5.046265044);
        handicapFactors.put( 24, 5.150322694);
        handicapFactors.put( 23, 5.261094994);
        handicapFactors.put( 22, 5.379336612);
        handicapFactors.put( 21, 5.505926557);
        handicapFactors.put( 20, 5.641895835);
        handicapFactors.put( 19, 5.788462992);
        handicapFactors.put( 18, 5.947080387);
        handicapFactors.put( 17, 6.119495233);
        handicapFactors.put( 16, 6.307831305);
        handicapFactors.put( 15, 6.514700159);
        handicapFactors.put( 14, 6.743355313);
        handicapFactors.put( 13, 6.997910525);
        handicapFactors.put( 12, 7.283656204);
        handicapFactors.put( 11, 7.607530793);
        handicapFactors.put( 10, 7.978845608);
        handicapFactors.put( 9,  8.41044174);
        handicapFactors.put( 8,  8.920620581);
    }

    private Map<Long, List<Double>> shooterToHandicapMap = new HashMap<>();

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    StageRepository stageRepository;
    @Autowired
    StageShooterRepository stageShooterRepository;
    @Autowired
    ShooterRepository shooterRepository;
    @Autowired
    MatchShooterSummaryRepository matchShooterSummaryRepository;

    public static List<StageShooter> generateSummaryFromMatch( Match match ) {
        Map<Long, StageShooter> shooterMap = new HashMap<>();

        double highMatchPoints = Double.MIN_VALUE;
        for( Stage stage : match.getStages() )
        {
            for( StageShooter shooter : stage.getShooters() )
            {
                long shooterId = shooter.getShooter().getId();
                if( !shooterMap.containsKey( shooterId ) )
                {
                    StageShooter shooterSummary = new StageShooter();
                    shooterSummary.setShooter( shooter.getShooter() );
                    shooterSummary.setStartingHandicap( shooter.getStartingHandicap() );
                    shooterSummary.setNewHandicap( shooter.getNewHandicap() );
                    shooterMap.put( shooterId, shooterSummary );
                }

                StageShooter summary = shooterMap.get( shooterId );
                summary.setHandicapMatchPoints( summary.getHandicapMatchPoints() + shooter.getHandicapMatchPoints() );
                summary.setRawMatchPoints( summary.getRawMatchPoints() + shooter.getRawMatchPoints());

                if( summary.getHandicapMatchPoints() > highMatchPoints)
                {
                    highMatchPoints = summary.getHandicapMatchPoints();
                }
            }
        }

        // Calculate the % for each shooter
        for( StageShooter shooter : shooterMap.values() )
        {
            shooter.setHandicapPercentage( 100 * shooter.getHandicapMatchPoints() / highMatchPoints );
        }

        return shooterMap.values().stream().toList();
    }

    @PostConstruct
    public void init()
    {
        startRecalculate();
    }

    public void startRecalculate()
    {
        Thread t = new Thread( new Runnable() {
            @Override
            public void run() {
                recalculate();
            }
        } );

        t.start();
    }

    public void recalculate()
    {
        long time = System.currentTimeMillis();
        System.out.println("Running Full Recalculate");

        //
        // Count how many stages and matches someone shoots
        //
        long time1 = System.currentTimeMillis();
        countStagesForShooters();
        System.out.println("countStagesForShooters(): " + (System.currentTimeMillis() - time1) + " ms");

        //
        // Calculate a person's Handicap for each stage they shot
        //
        time1 = System.currentTimeMillis();
        calculateShootersHandicapForStages();
        System.out.println("calculateShootersHandicapForStages(): " + (System.currentTimeMillis() - time1) + " ms");

        //
        // Calculate the overall match points with the new handicap data
        //
        time1 = System.currentTimeMillis();
        calculateOverallMatchPointsWithHandicapData();
        System.out.println("calculateOverallMatchPointsWithHandicapData(): " + (System.currentTimeMillis() - time1) + " ms");

        //
        // For each shooter, if they have missed a match, then give them 70% of the average match points
        //
        time1 = System.currentTimeMillis();
        addPointsForMissedMatches();
        System.out.println("addPointsForMissedMatches(): " + (System.currentTimeMillis() - time1) + " ms");

        //
        // Generate the match summary pages for all the matches
        //
        time1 = System.currentTimeMillis();
        generateMatchSummaryForAllMatches();
        System.out.println("generateMatchSummaryForAllMatches(): " + (System.currentTimeMillis() - time1) + " ms");

        System.out.println("Full Recalculate Finished. Time took: " + (System.currentTimeMillis() - time) + " ms");

    }


    /**
     * For each shooter in the database:
     *  Go through each match, and if the shooter shows up in any of the stages, then add 1 to their match count
     *  Go through each stage, and if the shooter shows up, AND match points were greater than 0, then add 1 to their stage count
     */
    private void countStagesForShooters()
    {
        for( Shooter shooter : shooterRepository.findAll() )
        {
            int numStages = 0;
            int numMatches = 0;
            for( Match match : matchRepository.findAll() )
            {
                boolean wasInMatch = false;

                for( Stage stage : match.getStages() )
                {
                    if( stage.getShooters().stream().anyMatch( s -> s.getShooter().getId() == shooter.getId() && s.getRawMatchPoints() > 0 ) )
                    {
                        if( !wasInMatch )
                        {
                            numMatches += 1;
                            wasInMatch = true;
                        }

                        numStages += 1;
                    }
                }
            }
            shooter.setCurrentPoints( 0 );
            shooter.setMatchesShot( numMatches );
            shooter.setStagesShot( numStages );
            shooterRepository.saveAndFlush( shooter );
        }
    }

    /**
     * For every match, find all the stages that the shooter has shot. Then apply the handicap calculation to
     * each stage. The calculation is as follows:
     *  baseHandicapHitFactor = lookup table with round count
     *  handicap = ( baseHandicapHitFactor - shooter's hit factor on that stage ) / round count
     * <p>
     *  The handicap is averaged between all the stages they shot that day. This is the calculated handicap. For example,
     *  if the shooter shoots 3 stages in the day, their handicap will be: (handicap_stage_1 + handicap_stage_2 + handicap_stage_3 ) / 3
     * <p>
     *  The person's new handicap is the average of all the matches they shot during the season
     * <p>
     *  This method also sets the shooter's hit factor for their stages using the following calculation:
     *      handicapHitFactor = raw hit factor + handicap * round count
     */
    private void calculateShootersHandicapForStages()
    {
        for( Shooter shooter : shooterRepository.findAll() )
        {
            for( Match match : matchRepository.findAll() )
            {
                List<StageShooter> shooterStages = new ArrayList<>();
                for(Stage stage : match.getStages() )
                {
                    shooterStages.addAll(
                            stage.getShooters().stream().filter( s -> s.getShooter().getId() == shooter.getId() ).toList()
                    );
                }

                double handicap = 0;
                for( StageShooter stageShooter : shooterStages )
                {
                    int roundCount = stageShooter.getStage().getRoundCount();
                    double baseHandicapHitFactor = handicapFactors.get( roundCount );

                    if( stageShooter.getRawHitFactor() > 0 ) // Don't calculate handicap if they got a 0
                        handicap = handicap + (baseHandicapHitFactor - stageShooter.getRawHitFactor()) / roundCount;
                }

                handicap = handicap / shooterStages.size();
                if( shooterStages.size() == 0) {
                    handicap = 0;
                }

                double startingHandicap = getHandicapForShooter( shooter );

                if( handicap != 0)
                {
                    addHandicapForShooter( shooter, handicap );
                }

                handicap = getHandicapForShooter( shooter );


                for( StageShooter stageShooter : shooterStages )
                {
                    int roundCount = stageShooter.getStage().getRoundCount();
                    stageShooter.setStartingHandicap( startingHandicap );
                    stageShooter.setNewHandicap( handicap );

                    stageShooter.setHandicapHitFactor(  stageShooter.getRawHitFactor() + stageShooter.getNewHandicap() * roundCount);
                    stageShooterRepository.saveAndFlush( stageShooter );
                }
            }
        }
    }

    /**
     * Once every shooter's handicap, and handicap hit factor is calculated for every stage:
     * <p>
     * For each stage, find the highest handicap hit factor, and find the average amount of stage points
     * <p>
     * Then, for every shooter in that stage, calculate their percentage using:
     *  percentage = 100 * shooter's handicap hit factor / highest handicap hit factor
     * <p>
     * Also calculate their handicap match points:
     *  handicap match points = percentage * stage round count * 5
     * <p>
     *  If the shooter gets a zero for raw match points (didn't shoot it, shot it really badly, etc.)
     *    handicap match points = average amount of stage points * 0.70
     *
     */
    private void calculateOverallMatchPointsWithHandicapData()
    {
        for( Match match : matchRepository.findAll() )
        {
            for( Stage stage : match.getStages() )
            {
                double hhf = Double.MIN_VALUE;
                int numShooters = 0;
                double sumPoints = 0.0;
                for( StageShooter shooter : stage.getShooters())
                {
                    numShooters++;
                    sumPoints += shooter.getHandicapMatchPoints();
                    if( shooter.getHandicapHitFactor() > hhf && shooter.getRawMatchPoints() > 0 )
                    {
                        hhf = shooter.getHandicapHitFactor();
                    }
                }
                if( numShooters > 0 )
                {
                    stage.setAveragePoints( sumPoints / numShooters );
                    stageRepository.saveAndFlush( stage );
                }

                // Update everyone else's percentage
                for( StageShooter shooter : stage.getShooters() )
                {
                    if( shooter.getRawMatchPoints() <= 0 )
                    {
                        shooter.setHandicapHitFactor( 0 );
                    }

                    double percentage = shooter.getHandicapHitFactor() / hhf;
                    double matchPoints = percentage * stage.getRoundCount() * 5;

                    percentage = percentage * 100;
                    shooter.setHandicapPercentage( percentage );

                    if( shooter.getRawMatchPoints() > 0 )
                    {
                        if( matchPoints < 0 )
                            matchPoints = 0;
                        shooter.setHandicapMatchPoints( matchPoints );
                    }
                    else
                    {
                        shooter.setHandicapMatchPoints( stage.getAveragePoints() * NOT_PRESENT_SCORING_FACTOR ); // If a shooter didn't shoot this stage or gets a 0, their match points will be the stage's average points
                    }

                    shooter.getShooter().setCurrentHandicap( shooter.getNewHandicap() );
                    shooter.getShooter().setCurrentPoints( shooter.getShooter().getCurrentPoints() + matchPoints );
                    shooterRepository.saveAndFlush( shooter.getShooter() );
                    stageShooterRepository.saveAndFlush( shooter );
                }
            }
        }
    }

    /**
     * Go through every shooter in the database, find the matches that they did not participate in.
     * For each stage of those matches, add 70% of the average stage scores to their current points
     */
    private void addPointsForMissedMatches()
    {
        for( Shooter shooter : shooterRepository.findAll() )
        {
            for( Match match : matchRepository.findAll() )
            {
                boolean didShootMatch = false;
                for( Stage stage : match.getStages() )
                {
                    for( StageShooter stageShooter : stage.getShooters() )
                    {
                        if( stageShooter.getShooter().getId() == shooter.getId() )
                        {
                            didShootMatch = true;
                        }
                    }
                }

                if( !didShootMatch )
                {
                    double currentPoints = shooter.getCurrentPoints();
                    for( Stage stage : match.getStages() )
                    {
                        currentPoints += stage.getAveragePoints() * NOT_PRESENT_SCORING_FACTOR;
                    }

                    shooter.setCurrentPoints( currentPoints );
                    shooterRepository.saveAndFlush( shooter );
                }
            }
        }
    }

    /**
     * For every user in the database, regardless if they participated or not in a match:
     * <p>
     * For every match, save the shooter's new handicap for that match (zero handicap if they did not participate)
     * <p>
     * For every match, get their total earned points for the match and save it, if they participated. If they
     * did not participate, then they will earn 70% of the average match points for that match
     */
    private void generateMatchSummaryForAllMatches()
    {
        matchShooterSummaryRepository.deleteAll();
        for( Match match : matchRepository.findAll() )
        {
            Map<Long, MatchShooterSummary> summaryMap = new HashMap<>();
            List<StageShooter> shooters = generateSummaryFromMatch( match );
            for( StageShooter shooter : shooters )
            {
                MatchShooterSummary summary = new MatchShooterSummary();
                summary.setShooter( shooter.getShooter() );
                summary.setPresent( true );
                summary.setNewHandicap( shooter.getNewHandicap() );
                summary.setStartingHandicap( shooter.getStartingHandicap() );
                summary.setEarnedPoints( shooter.getHandicapMatchPoints() );
                summary.setMatch( match );
                summaryMap.put( shooter.getShooter().getId(), summary );
            }

            for( Shooter shooter : shooterRepository.findAll() )
            {
                if( !summaryMap.containsKey( shooter.getId() ) )
                {
                    double matchPoints = 0;
                    for( Stage stage : match.getStages() )
                    {
                        matchPoints += stage.getAveragePoints() * NOT_PRESENT_SCORING_FACTOR;
                    }

                    MatchShooterSummary summary = new MatchShooterSummary();
                    summary.setShooter( shooter );
                    summary.setPresent( false );
                    summary.setEarnedPoints( matchPoints );
                    summary.setMatch( match );
                    summaryMap.put( shooter.getId(), summary );
                }
            }

            matchShooterSummaryRepository.saveAll( summaryMap.values() );
        }
    }


    /**
     * Add a new handicap for a specific shooter. This is used to calculate the average later
     */
    private void addHandicapForShooter( Shooter shooter, double newHandicap )
    {

        if( !shooterToHandicapMap.containsKey( shooter.getId() ) )
        {
            shooterToHandicapMap.put( shooter.getId(), new ArrayList<>() );
        }

        shooterToHandicapMap.get( shooter.getId() ).add( newHandicap );
    }

    /**
     * Get the rolling handicap average for a specific shooter.
     */
    private double getHandicapForShooter( Shooter shooter )
    {

        List<Double> list = shooterToHandicapMap.get( shooter.getId() );
        if( list == null || list.isEmpty() )
            return 0.0;

        return list.stream().mapToDouble( a->a ).average().getAsDouble();
    }
}
