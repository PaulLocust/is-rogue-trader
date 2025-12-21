package com.example.is_rogue_trader.repository;

import com.example.is_rogue_trader.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByPlanetId(Long planetId);
    
    @Query("SELECT e FROM Event e WHERE e.planet.id = :planetId AND e.resolved = false")
    List<Event> findActiveEventsByPlanetId(@Param("planetId") Long planetId);
}

