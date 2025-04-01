package com.philng.RBPSThursdays.repository;

import com.philng.RBPSThursdays.models.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
