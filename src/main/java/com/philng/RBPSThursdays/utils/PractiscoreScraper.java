package com.philng.RBPSThursdays.utils;

import com.philng.RBPSThursdays.models.Match;
import com.philng.RBPSThursdays.models.Shooter;
import com.philng.RBPSThursdays.models.Stage;
import com.philng.RBPSThursdays.models.StageShooter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PractiscoreScraper {
    private static Map<String, Match> scrapedMatches = new HashMap<>();


    public static void main( String [] args ) throws Exception
    {
        Match match = new PractiscoreScraper().scrape( "https://practiscore.com/results/new/fe0dc582-ef33-4f45-9110-4e31d63f4766" );
    }

    public static Match scrape( String url )
    {
        if( scrapedMatches.containsKey( url ) )
        {
            return scrapedMatches.get( url );
        }
        ChromeOptions options = new ChromeOptions();
        WebDriver driver = null;
        try {
            driver = new RemoteWebDriver(new URL("http://10.223.182.175:4444"), options);
        } catch( MalformedURLException e ) {
            throw new RuntimeException( e );
        }

        driver.get( url );

        WebElement selector = driver.findElement( By.id( "resultLevel" ) );
        List<WebElement> stages = selector.findElements( By.tagName( "option" ) );

        Match match = new Match();
        match.setName( driver.getTitle().replace( " | PractiScore", "" ).trim() );
        match.setDateImported( Timestamp.from( Instant.now() ) );
        match.setPractiscoreURL( url );

        for( int i = 1; i < stages.size(); i++ ) {
            stages.get( i ).click();
            try {
                Thread.sleep(1000);
            } catch( InterruptedException e ) {
                throw new RuntimeException( e );
            }
            WebElement table = driver.findElement( By.id("mainResultsTable") );
            List<WebElement> elements = table.findElement( By.tagName( "tbody" ) ).findElements( By.tagName( "tr" ) );

            String stageName = stages.get( i ).getText();
            int alphas = Integer.parseInt( elements.getFirst().findElements( By.tagName( "td" ) ).get( 11 ).getText() );
            int charlies = Integer.parseInt( elements.getFirst().findElements( By.tagName( "td" ) ).get( 12 ).getText() );
            int deltas = Integer.parseInt( elements.getFirst().findElements( By.tagName( "td" ) ).get( 13 ).getText() );
            int mikes = Integer.parseInt( elements.getFirst().findElements( By.tagName( "td" ) ).get( 14 ).getText() );

            Stage stage = new Stage();
            stage.setRoundCount( alphas + charlies + deltas + mikes );
            stage.setName( stageName );
            match.getStages().add( stage );

            for( WebElement element : elements )
            {
                List<WebElement> columns = element.findElements( By.tagName( "td" ) );
                String shooterName = columns.get( 0 ).getText().split( "-" )[1].replaceAll( "[0-9]", "" ).trim();
                double HF = Double.parseDouble( columns.get( 4 ).getText() );
                double rawPercentage = Double.parseDouble( columns.get( 1 ).getText() );
                double rawPoints = Double.parseDouble( columns.get( 2 ).getText() );
                Shooter shooter = new Shooter();
                shooter.setName( shooterName );

                StageShooter stageShooter = new StageShooter();
                stageShooter.setShooter( shooter );
                stageShooter.setRawHitFactor( HF );
                stageShooter.setRawPercentage( rawPercentage );
                stageShooter.setRawMatchPoints( rawPoints );

                stage.getShooters().add( stageShooter );

            }

        }
        driver.quit();

        scrapedMatches.put( url, match );
        return match;
    }
}
