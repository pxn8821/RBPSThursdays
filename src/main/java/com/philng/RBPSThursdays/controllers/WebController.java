package com.philng.RBPSThursdays.controllers;

import com.philng.RBPSThursdays.models.*;
import com.philng.RBPSThursdays.repository.MatchRepository;
import com.philng.RBPSThursdays.repository.ShooterRepository;
import com.philng.RBPSThursdays.repository.StageRepository;
import com.philng.RBPSThursdays.repository.StageShooterRepository;
import com.philng.RBPSThursdays.utils.Calculator;
import com.philng.RBPSThursdays.utils.PractiscoreScraper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Autowired
    MatchRepository matchRepository;
    @Autowired
    ShooterRepository shooterRepository;
    @Autowired
    StageRepository stageRepository;
    @Autowired
    StageShooterRepository stageShooterRepository;

    @Autowired
    Calculator calculator;

    @RequestMapping(value = "/queueMatch", method = RequestMethod.GET)
    public String inputPractiscoreURL()
    {
        return "inputMatchURL_results";
    }

    @RequestMapping(value = "/queueMatch", method = RequestMethod.POST)
    public String startScrape( @RequestParam("url") String url, Model model )
    {
        Set<String> unmappedNames = new HashSet<>();
        Set<String> existingNames = shooterRepository.findAll().stream().map( c -> c.getName().toLowerCase().trim() ).collect( Collectors.toSet());
        Match match = PractiscoreScraper.scrape(url);
        for( Stage stage : match.getStages() )
        {
            for(StageShooter stageShooter : stage.getShooters() )
            {
                if( !existingNames.contains( stageShooter.getShooter().getName().toLowerCase() ) )
                {
                    unmappedNames.add( stageShooter.getShooter().getName() );
                }
            }
        }

        List<String> existingShooters = new ArrayList<>( shooterRepository.findAll().stream().map( c -> c.getName() ).toList() );
        Collections.sort(existingShooters);

        model.addAttribute( "existingShooters", existingShooters);
        model.addAttribute( "unmappedNames", unmappedNames );
        model.addAttribute( "match", match );
        return "inputMatchURL_results";
    }

    @RequestMapping(value = "/processMatch", method = RequestMethod.POST)
    public String processMatch( @RequestParam("url") String url, Model model, HttpServletRequest request )
    {
        Map<String, String> nameMap = new HashMap<>();

        Map<String, String[]> paramMap = request.getParameterMap();
        for( Map.Entry<String, String[]> entry : paramMap.entrySet())
        {
            if( entry.getKey().contains( "unmappedNames[" ) )
            {
                String name = entry.getKey().replace( "unmappedNames[", "" ).replace( "]", "" ).trim();
                String value = entry.getValue()[0];

                if( !value.equals( "New Shooter" ))
                {
                    nameMap.put( name, value );
                }
            }
        }


        Match match = PractiscoreScraper.scrape( url );
        for( Stage stage : match.getStages() )
        {
            for( StageShooter stageShooter : stage.getShooters() )
            {
                // Override the shooter
                if( nameMap.containsKey( stageShooter.getShooter().getName() ) )
                {
                    stageShooter.getShooter().setName( nameMap.get( stageShooter.getShooter().getName() ) );
                }

                Optional<Shooter> existingShooter = shooterRepository.findAll().stream().filter( s -> s.getName().equalsIgnoreCase( stageShooter.getShooter().getName() ) ).findFirst();
                if( existingShooter.isPresent() )
                {
                    stageShooter.setShooter( existingShooter.get() );
                }
                else
                {
                    Shooter newShooter = stageShooter.getShooter();
                    shooterRepository.save( newShooter );
                }
            }
            stageShooterRepository.saveAll( stage.getShooters() );
        }
        stageRepository.saveAll( match.getStages() );
        matchRepository.save(match);

        calculator.startRecalculate();

        return "inputMatchURL_results";
    }

    @RequestMapping(value="/match/{id}", method = RequestMethod.GET)
    public String viewMatch( @PathVariable("id") String id, Model model)
    {
        Optional<Match> match = matchRepository.findById( Long.parseLong( id ) );

        List<StageShooter> summary = Calculator.generateSummaryFromMatch( match.get() );

        model.addAttribute( "match", match.get() );
        model.addAttribute( "summary", summary );
        return "match";
    }

    @RequestMapping(value="/matchSummary/{id}", method = RequestMethod.GET)
    public String viewMatchSummary( @PathVariable("id") String id, Model model)
    {
        Optional<Match> match = matchRepository.findById( Long.parseLong( id ) );

        List<MatchShooterSummary> summary = match.get().getSummary();

        model.addAttribute( "match", match.get() );
        model.addAttribute( "summary", summary );
        return "shooterSummary";
    }

    @RequestMapping(value="/standings", method = RequestMethod.GET)
    public String standings( Model model )
    {
        List<Shooter> shooters = shooterRepository.findAll();
        model.addAttribute( "shooters", shooters );
        return "standings";
    }

    @RequestMapping(value="/", method = RequestMethod.GET)
    public String index( Model model )
    {
        List<Match> matches = matchRepository.findAll().reversed();

        model.addAttribute( "matches", matches );
        return "index";
    }
}
